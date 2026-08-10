package com.xiaoyv.bangumi.features.pixiv.login.business

import androidx.lifecycle.SavedStateHandle
import com.xiaoyv.bangumi.shared.core.mvi.BaseSyntax
import com.xiaoyv.bangumi.shared.core.mvi.BaseViewModel
import com.xiaoyv.bangumi.shared.core.utils.debugLog
import com.xiaoyv.bangumi.shared.core.utils.errMsg
import com.xiaoyv.bangumi.shared.data.manager.app.PreferenceStore
import com.xiaoyv.bangumi.shared.data.model.response.pixiv.ComposePixivToken
import com.xiaoyv.bangumi.shared.data.usecase.PixivRepoUseCase

/**
 * [PixivLoginViewModel]
 *
 * @author why
 * @since 2025/1/12
 */
class PixivLoginViewModel(
    savedStateHandle: SavedStateHandle,
    private val pixivRepoUseCase: PixivRepoUseCase,
    private val preferenceStore: PreferenceStore,
) : BaseViewModel<PixivLoginState, PixivLoginSideEffect, PixivLoginEvent.Action>(savedStateHandle) {

    /**
     * 同步检测 token 避免闪白：initSate 直接读取 preferenceStore 设置 isLoggedIn
     */
    override fun initSate(onCreate: Boolean): PixivLoginState {
        val token = preferenceStore.pixivToken
        val isLoggedIn = token.accessToken.isNotBlank()
        debugLog { "PixivLogin initSate: accessToken=${token.accessToken.take(8)}, isBlank=${token.accessToken.isBlank()}, isLoggedIn=$isLoggedIn" }
        return PixivLoginState(isLoggedIn = isLoggedIn)
    }

    override suspend fun BaseSyntax<PixivLoginState, PixivLoginSideEffect>.refreshSync() {
        val token = preferenceStore.pixivToken
        val isLoggedIn = token.accessToken.isNotBlank()
        debugLog { "PixivLogin refreshSync: isLoggedIn=$isLoggedIn" }

        if (isLoggedIn) {
            pixivRepoUseCase.fetchCurrentUser()
                .onSuccess { user ->
                    debugLog { "PixivLogin refreshSync: user=${user.name}, avatar=${user.profileImageUrls?.medium?.take(20)}" }
                    reduceContent {
                        state.copy(
                            isLoggedIn = true,
                            currentUser = user,
                            pixivUserName = user.name.orEmpty(),
                            pixivUserAvatar = user.profileImageUrls?.medium.orEmpty(),
                            loginSuccess = false,
                            isLoggingIn = false,
                        )
                    }
                    loadUserProfile(user.id)
                }
                .onFailure {
                    debugLog { "PixivLogin refreshSync: fetchCurrentUser FAILED=${it.message}" }
                    reduceContent { state.copy(isLoggedIn = true, isLoggingIn = false) }
                }
        } else {
            debugLog { "PixivLogin refreshSync: not logged in" }
            reduceContent { state.copy(isLoggedIn = false, isLoggingIn = false, currentUser = null, userProfile = null) }
        }
    }

    /**
     * 加载用户详细资料（生日、职业、地区、国家、主页、社交链接等）
     */
    private suspend fun BaseSyntax<PixivLoginState, PixivLoginSideEffect>.loadUserProfile(userId: Long) {
        pixivRepoUseCase.fetchUserDetail(userId)
            .onSuccess { detail ->
                debugLog { "PixivLogin loadUserProfile: birth=${detail.profile?.birth}, region=${detail.profile?.region}" }
                reduceContent { state.copy(userProfile = detail.profile) }
            }
            .onFailure {
                debugLog { "PixivLogin loadUserProfile FAILED=${it.message}" }
            }
    }

    override fun onEvent(event: PixivLoginEvent.Action) {
        when (event) {
            is PixivLoginEvent.Action.OnRefresh -> refresh(loading = event.loading)
            is PixivLoginEvent.Action.OnWebViewLogin -> onWebViewLogin()
            is PixivLoginEvent.Action.OnBrowserLogin -> onBrowserLogin()
            is PixivLoginEvent.Action.OnShowTokenDialog -> onShowTokenDialog()
            is PixivLoginEvent.Action.OnDismissTokenDialog -> onDismissTokenDialog()
            is PixivLoginEvent.Action.OnTokenInput -> onTokenInput(event.token)
            is PixivLoginEvent.Action.OnSubmitToken -> onSubmitToken()
            is PixivLoginEvent.Action.OnDismissLoginSuccess -> onDismissLoginSuccess()
            is PixivLoginEvent.Action.OnLogout -> onLogout()
        }
    }

    private fun onWebViewLogin() = action {
        reduceContent { state.copy(isLoggingIn = true) }
        reduceContent { state.copy(isLoggingIn = false) }
    }

    private fun onBrowserLogin() = action {
        // Opens in external browser
    }

    private fun onShowTokenDialog() = action {
        reduceContent { state.copy(showTokenDialog = true, tokenError = null, tokenInput = "") }
    }

    private fun onDismissTokenDialog() = action {
        reduceContent { state.copy(showTokenDialog = false) }
    }

    private fun onTokenInput(token: String) = action {
        reduceContent { state.copy(tokenInput = token, tokenError = null) }
    }

    private fun onSubmitToken() = action {
        val token = stateRaw.tokenInput.trim()
        if (token.isBlank()) {
            reduceContent { state.copy(tokenError = "请输入 refresh_token") }
            return@action
        }

        reduceContent { state.copy(isLoggingIn = true, tokenError = null) }

        val result = pixivRepoUseCase.loginWithRefreshToken(token)
        result.onSuccess {
            debugLog { "Token login success" }
            // 登录成功后获取用户信息
            pixivRepoUseCase.fetchCurrentUser()
                .onSuccess { user ->
                    reduceContent {
                        state.copy(
                            isLoggedIn = true,
                            isLoggingIn = false,
                            currentUser = user,
                            pixivUserName = user.name.orEmpty(),
                            pixivUserAvatar = user.profileImageUrls?.medium.orEmpty(),
                            loginSuccess = true,
                            showTokenDialog = false,
                            tokenInput = "",
                        )
                    }
                    loadUserProfile(user.id)
                }
                .onFailure {
                    reduceContent {
                        state.copy(
                            isLoggedIn = true,
                            isLoggingIn = false,
                            loginSuccess = true,
                            showTokenDialog = false,
                            tokenInput = "",
                        )
                    }
                }
        }.onFailure {
            debugLog { "Token login failed: ${it.message}" }
            reduceContent {
                state.copy(
                    isLoggingIn = false,
                    tokenError = it.errMsg.ifBlank { "登录失败，请检查 refresh_token 是否正确" },
                )
            }
        }
    }

    private fun onDismissLoginSuccess() = action {
        reduceContent { state.copy(loginSuccess = false) }
    }

    private fun onLogout() = action {
        pixivRepoUseCase.logout()
        reduceContent { PixivLoginState() }
        postEffect { PixivLoginSideEffect.OnToast("已退出 Pixiv 登录") }
    }
}
