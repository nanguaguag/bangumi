package com.xiaoyv.bangumi.features.sign.sign_in.business

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import com.xiaoyv.bangumi.shared.core.mvi.BaseSyntax
import com.xiaoyv.bangumi.shared.core.mvi.BaseViewModel
import com.xiaoyv.bangumi.shared.core.types.LoadingState
import com.xiaoyv.bangumi.shared.core.utils.debugLog
import com.xiaoyv.bangumi.shared.core.utils.errMsg
import com.xiaoyv.bangumi.shared.data.manager.app.UserManager
import com.xiaoyv.bangumi.shared.data.model.request.LoginParam
import com.xiaoyv.bangumi.shared.data.model.response.bgm.ComposeAuthToken
import com.xiaoyv.bangumi.shared.data.model.response.bgm.ComposeLoginResult
import com.xiaoyv.bangumi.shared.data.repository.SignRepository
import com.xiaoyv.bangumi.shared.data.repository.UserRepository
import kotlinx.collections.immutable.toImmutableMap

/**
 * [SignInViewModel]
 *
 * @author why
 * @since 2025/1/12
 */
class SignInViewModel(
    private val signRepo: SignRepository,
    private val userManager: UserManager,
    private val userRepository: UserRepository,
    stateHandle: SavedStateHandle,
) : BaseViewModel<SignInState, SignInSideEffect, SignInEvent.Action>(stateHandle) {

    override fun initSate(onCreate: Boolean) = SignInState()

    override suspend fun BaseSyntax<SignInState, SignInSideEffect>.refreshSync() {
        onRefreshVerifyCodeImage()
    }

    override fun onEvent(event: SignInEvent.Action) {
        when (event) {
            is SignInEvent.Action.OnEmailChange -> onEmailChange(event.email)
            is SignInEvent.Action.OnPasswordChange -> onPasswordChange(event.password)
            is SignInEvent.Action.OnCodeChange -> onCodeChange(event.code)
            is SignInEvent.Action.OnSignIn -> onSignIn()
            is SignInEvent.Action.OnRefreshVerifyCode -> onRefreshVerifyCodeImage()
            is SignInEvent.Action.OnTokenLogin -> onTokenLogin()
            is SignInEvent.Action.OnCookieLogin -> onCookieLogin()
            is SignInEvent.Action.OnTokenInputChange -> onTokenInputChange(event.token)
            is SignInEvent.Action.OnCookieInputChange -> onCookieInputChange(event.cookie)
        }
    }

    private fun onCodeChange(value: TextFieldValue) = action {
        reduceContent { state.copy(code = value) }
    }

    private fun onPasswordChange(value: TextFieldValue) = action {
        reduceContent { state.copy(password = value) }
    }

    private fun onEmailChange(value: TextFieldValue) = action {
        reduceContent { state.copy(email = value) }
    }

    private fun onTokenInputChange(value: TextFieldValue) = action {
        reduceContent { state.copy(tokenInput = value) }
    }

    private fun onCookieInputChange(value: TextFieldValue) = action {
        reduceContent { state.copy(cookieInput = value) }
    }

    private fun onRefreshVerifyCodeImage() = action {
        reduceContent {
            state.copy(
                codeState = LoadingState.Loading,
                codeImage = byteArrayOf(),
                code = TextFieldValue()
            )
        }

        // 拉取登录表单
        signRepo.fetchLoginForm()
            .onFailure {
                postToast { it.errMsg }

                reduceContent {
                    state.copy(
                        codeState = LoadingState.Error(it),
                        codeImage = byteArrayOf(),
                        code = TextFieldValue()
                    )
                }
            }
            .onSuccess { loginForm ->
                // 当前用户已经登录成功，无需再登录
                if (loginForm.hasLogin && loginForm.loginInfo != ComposeLoginResult.Empty && loginForm.loginInfo.success) {
                    reduceContent { state.copy(codeState = LoadingState.NotLoading) }

                    onSaveUser(loginForm.loginInfo)
                } else {
                    signRepo.fetchVerifyCodeImage()
                        .onFailure {
                            reduceContent {
                                state.copy(
                                    codeState = LoadingState.Error(it),
                                    codeImage = byteArrayOf(),
                                    code = TextFieldValue()
                                )
                            }
                        }
                        .onSuccess {
                            reduceContent {
                                state.copy(
                                    codeState = LoadingState.NotLoading,
                                    codeImage = it,
                                    code = TextFieldValue(),
                                    otherForms = loginForm.forms.toImmutableMap()
                                )
                            }
                        }
                }
            }
    }

    private fun onSignIn() = action {
        reduceContent { state.copy(loggingRunning = true) }

        signRepo.sendLogin(
            param = state.content.let {
                LoginParam(
                    email = it.email.text,
                    password = it.password.text,
                    code = it.code.text,
                    otherForms = it.otherForms
                )
            }
        ).onFailure {
            postToast { it.errMsg }

            reduceContent {
                state.copy(
                    loggingRunning = false,
                    loginResult = ComposeLoginResult.Empty,
                    code = TextFieldValue(),
                    codeImage = byteArrayOf()
                )
            }

            // 刷新验证码
            onRefreshVerifyCodeImage()
        }.onSuccess {
            // 登录成功保存用户
            if (it.success) onSaveUser(it) else {
                // 刷新验证码
                onRefreshVerifyCodeImage()

                // 登录失败了
                reduceContent {
                    state.copy(
                        loggingRunning = false,
                        code = TextFieldValue(),
                        codeImage = byteArrayOf(),
                        loginResult = it
                    )
                }

                postEffect { SignInSideEffect.OnLoginResult(it) }
            }
        }
    }

    /**
     * Token 直接登录
     *
     * 用户粘贴 Access Token，直接验证并保存
     */
    private fun onTokenLogin() = action {
        val token = state.content.tokenInput.text.trim()
        if (token.isBlank()) {
            postToast { "请输入 Access Token" }
            return@action
        }

        val previousUser = userManager.userInfo
        val previousToken = userManager.userToken
        reduceContent { state.copy(altLoginRunning = true) }

        runCatching {
            // 1. 构造 Token 对象并保存
            val authToken = ComposeAuthToken(
                accessToken = token,
                // 开发者页面提供的 Access Token 没有 refresh token，按长期 token 保存。
                expiresIn = 0,
                tokenType = "Bearer",
                saveAt = com.xiaoyv.bangumi.shared.System.currentTimeMillis()
            )
            userManager.setToken(authToken)

            // 2. 验证 Token（调用 v0/me）
            val apiUser = userRepository.validateToken().getOrThrow()

            // 3. 获取完整用户信息
            val fullUser = userRepository.fetchUserInfo(apiUser.username).getOrThrow()
            val userInfo = fullUser.copy(id = apiUser.id, group = apiUser.group)

            // 4. 清理旧网页会话，避免 Token 与旧 Cookie 属于不同账号
            userManager.logout().getOrThrow()

            // 5. 保存用户信息
            userManager.login(userInfo, authToken.copy(userId = apiUser.id))

            debugLog { "Token 登录成功: $userInfo" }

            userInfo to authToken
        }.onFailure {
            // Token 验证失败时恢复登录前状态，避免留下半成品认证状态。
            userManager.login(previousUser, previousToken)
            reduceContent { state.copy(altLoginRunning = false) }
            postToast { "Token 无效，请检查后重试" }
            debugLog { "Token 登录失败: ${it.message}" }
        }.onSuccess {
            reduceContent {
                state.copy(
                    altLoginRunning = false,
                    loginResult = ComposeLoginResult(
                        success = true,
                        message = "Token 登录成功",
                        composeUser = it.first
                    )
                )
            }
            postEffect {
                SignInSideEffect.OnLoginResult(
                    ComposeLoginResult(success = true, message = "Token 登录成功")
                )
            }
        }
    }

    /**
     * Cookie 辅助登录
     *
     * 用户粘贴浏览器 Cookie，通过 OAuth 流程获取 Token
     */
    private fun onCookieLogin() = action {
        val cookieString = state.content.cookieInput.text.trim()
        if (cookieString.isBlank()) {
            postToast { "请粘贴 Cookie" }
            return@action
        }

        if (!cookieString.contains("chii_auth")) {
            postToast { "Cookie 中未找到 chii_auth，请确保已登录 bgm.tv" }
            return@action
        }

        reduceContent { state.copy(altLoginRunning = true) }

        try {
            // 先清理旧会话，避免 Cookie、Token 和用户信息属于不同账号。
            userManager.logout().getOrThrow()

            // 1. 保存 Cookie 到存储
            userRepository.saveCookie(cookieString).getOrThrow()

            // 2. 获取登录表单（验证 Cookie 是否有效）
            val loginForm = signRepo.fetchLoginForm().getOrThrow()
            require(loginForm.hasLogin && loginForm.loginInfo.success) {
                "Cookie 无效或已过期，请检查后重试"
            }

            val composeUser = loginForm.loginInfo.composeUser
            val formHash = composeUser.formHash
            require(formHash.isNotBlank()) { "无法获取授权表单码" }

            // 3. 通过 formHash 创建 OAuth Token
            val token = userRepository.submitRequestToken(formHash).getOrThrow()

            // 4. 获取完整用户信息
            val fullUser = userRepository.fetchUserInfo(composeUser.username).getOrThrow()
            val userInfo = fullUser.copy(
                id = composeUser.id,
                group = composeUser.group,
                formHash = formHash
            )

            // 5. 保存用户信息和 Token
            userManager.login(userInfo, token)

            debugLog { "Cookie 登录成功: $userInfo" }

            reduceContent { state.copy(altLoginRunning = false, loginResult = loginForm.loginInfo) }
            postEffect { SignInSideEffect.OnLoginResult(loginForm.loginInfo) }
        } catch (e: Throwable) {
            // Cookie 已写入持久存储时也要清理，避免后续请求继续使用无效或错误账号。
            userManager.logout()
            reduceContent { state.copy(altLoginRunning = false) }
            postToast { e.message ?: "Cookie 登录失败" }
            debugLog { "Cookie 登录失败: ${e.message}" }
        }
    }

    /**
     * 获取授权，保存用户信息
     */
    private fun onSaveUser(loginInfo: ComposeLoginResult) = action {
        runCatching {
            val user = loginInfo.composeUser
            val token = userRepository.submitRequestToken(user.formHash).getOrThrow()
            val info = userRepository.fetchUserInfo(user.username).getOrThrow()
            user.copy(id = info.id, group = info.group, sign = info.sign) to token
        }.onFailure {
            reduceContent { state.copy(loggingRunning = false, loginResult = ComposeLoginResult.Empty) }
            postToast { it.errMsg }
        }.onSuccess {
            val (userInfo, userToken) = it

            // 保存用户信息
            userManager.login(userInfo, userToken)

            reduceContent { state.copy(loggingRunning = false, loginResult = loginInfo) }

            postEffect { SignInSideEffect.OnLoginResult(loginInfo) }

            debugLog { "登录用户: $userInfo, token: $userToken" }
        }
    }
}
