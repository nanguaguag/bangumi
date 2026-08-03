package com.xiaoyv.bangumi.features.pixiv.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Login
import androidx.compose.material.icons.rounded.OpenInBrowser
import androidx.compose.material.icons.rounded.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import com.xiaoyv.bangumi.core_resource.resources.Res
import com.xiaoyv.bangumi.core_resource.resources.pixiv_account_not_login
import com.xiaoyv.bangumi.core_resource.resources.pixiv_account_not_login_desc
import com.xiaoyv.bangumi.core_resource.resources.pixiv_login_browser
import com.xiaoyv.bangumi.core_resource.resources.pixiv_login_guide
import com.xiaoyv.bangumi.core_resource.resources.pixiv_login_success
import com.xiaoyv.bangumi.core_resource.resources.pixiv_login_title
import com.xiaoyv.bangumi.core_resource.resources.pixiv_login_token
import com.xiaoyv.bangumi.core_resource.resources.pixiv_login_webview
import com.xiaoyv.bangumi.core_resource.resources.pixiv_token_dialog_help
import com.xiaoyv.bangumi.core_resource.resources.pixiv_token_dialog_hint
import com.xiaoyv.bangumi.core_resource.resources.pixiv_token_dialog_title
import com.xiaoyv.bangumi.features.pixiv.login.business.PixivLoginEvent
import com.xiaoyv.bangumi.features.pixiv.login.business.PixivLoginSideEffect
import com.xiaoyv.bangumi.features.pixiv.login.business.PixivLoginState
import com.xiaoyv.bangumi.features.pixiv.login.business.PixivLoginViewModel
import com.xiaoyv.bangumi.shared.core.mvi.BaseState
import com.xiaoyv.bangumi.shared.core.utils.debugLog
import com.xiaoyv.bangumi.shared.data.manager.app.PreferenceStore
import com.xiaoyv.bangumi.shared.data.repository.PixivRepository
import com.xiaoyv.bangumi.shared.ui.component.bar.BgmTopAppBar
import com.xiaoyv.bangumi.shared.ui.component.layout.state.StateLayout
import com.xiaoyv.bangumi.shared.ui.component.navigation.Screen
import com.xiaoyv.bangumi.shared.ui.kts.collectBaseSideEffect
import com.xiaoyv.bangumi.shared.ui.theme.BgmIcons
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState

@Composable
fun PixivLoginRoute(
    viewModel: PixivLoginViewModel,
    onNavUp: () -> Unit,
    onNavScreen: (Screen) -> Unit,
) {
    val baseState by viewModel.collectAsState()

    // 每次进入页面时检查登录状态（处理浏览器登录后返回的情况）
    LaunchedEffect(Unit) {
        viewModel.onEvent(PixivLoginEvent.Action.OnRefresh(false))
    }

    viewModel.collectBaseSideEffect {
        when (it) {
            is PixivLoginSideEffect.OnNavUp -> onNavUp()
            is PixivLoginSideEffect.OnToast -> {
                // Toast handled by BaseViewModel
            }
        }
    }

    PixivLoginScreen(
        baseState = baseState,
        onActionEvent = viewModel::onEvent,
        onUiEvent = {
            when (it) {
                is PixivLoginEvent.UI.OnNavUp -> onNavUp()
                is PixivLoginEvent.UI.OnNavScreen -> onNavScreen(it.screen)
            }
        },
    )
}

@Composable
private fun PixivLoginScreen(
    baseState: BaseState<PixivLoginState>,
    onUiEvent: (PixivLoginEvent.UI) -> Unit,
    onActionEvent: (PixivLoginEvent.Action) -> Unit,
) {

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            BgmTopAppBar(
                title = stringResource(Res.string.pixiv_login_title),
                onNavigationClick = { onUiEvent(PixivLoginEvent.UI.OnNavUp) }
            )
        }
    ) {
        StateLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            onRefresh = { onActionEvent(PixivLoginEvent.Action.OnRefresh(it)) },
            baseState = baseState,
        ) { state ->
            PixivLoginScreenContent(state, onUiEvent, onActionEvent)
        }
    }
}


@Composable
private fun PixivLoginScreenContent(
    state: PixivLoginState,
    onUiEvent: (PixivLoginEvent.UI) -> Unit,
    onActionEvent: (PixivLoginEvent.Action) -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val repo = koinInject<PixivRepository>()
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 登录成功图标动画
        AnimatedVisibility(
            visible = state.loginSuccess,
            enter = fadeIn() + scaleIn(),
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = BgmIcons.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(Res.string.pixiv_login_success),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (state.isLoggedIn) {
            // 已登录状态
            PixivLoggedInContent(state, onActionEvent)
        } else {
            // 未登录状态 - 显示登录向导
            PixivLoginGuideContent(state, onUiEvent, onActionEvent, repo, uriHandler, scope)
        }
    }

    // Token 登录对话框
    if (state.showTokenDialog) {
        PixivTokenLoginDialog(state, onActionEvent, uriHandler)
    }
}

@Composable
private fun PixivLoginGuideContent(
    state: PixivLoginState,
    onUiEvent: (PixivLoginEvent.UI) -> Unit,
    onActionEvent: (PixivLoginEvent.Action) -> Unit,
    repo: PixivRepository,
    uriHandler: androidx.compose.ui.platform.UriHandler,
    scope: kotlinx.coroutines.CoroutineScope,
) {
    // 登录向导说明
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
    ) {
        Text(
            modifier = Modifier.padding(20.dp),
            text = stringResource(Res.string.pixiv_login_guide),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    // 未登录状态提示
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = BgmIcons.Login,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(Res.string.pixiv_account_not_login),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stringResource(Res.string.pixiv_account_not_login_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(32.dp))

    // 应用内 WebView 登录按钮
    Button(
        modifier = Modifier.fillMaxWidth(),
        enabled = !state.isLoggingIn,
        onClick = {
            scope.launch {
                val login = repo.fetchLoginChallenge().getOrThrow()
                val challenge = login.codeChallenge
                val loginUrl = "https://app-api.pixiv.net/web/v1/login?code_challenge=" +
                    challenge +
                    "&code_challenge_method=S256&client=pixiv-android&source=pixiv-android"
                debugLog { "PixivLoginUrl: $loginUrl" }
                onUiEvent(PixivLoginEvent.UI.OnNavScreen(Screen.Web(loginUrl)))
            }
        }
    ) {
        if (state.isLoggingIn) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        } else {
            Icon(
                imageVector = BgmIcons.Login,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(stringResource(Res.string.pixiv_login_webview))
    }

    Spacer(modifier = Modifier.height(12.dp))

    // 外部浏览器登录按钮
    OutlinedButton(
        modifier = Modifier.fillMaxWidth(),
        enabled = !state.isLoggingIn,
        onClick = {
            scope.launch {
                val login = repo.fetchLoginChallenge().getOrThrow()
                val challenge = login.codeChallenge
                val loginUrl = "https://app-api.pixiv.net/web/v1/login?code_challenge=" +
                    challenge +
                    "&code_challenge_method=S256&client=pixiv-android&source=pixiv-android"
                uriHandler.openUri(loginUrl)
            }
        }
    ) {
        Icon(
            imageVector = BgmIcons.OpenInBrowser,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(stringResource(Res.string.pixiv_login_browser))
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Token 登录 —— 底部文字按钮
    OutlinedButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onActionEvent(PixivLoginEvent.Action.OnShowTokenDialog) }
    ) {
        Icon(
            imageVector = BgmIcons.VpnKey,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(stringResource(Res.string.pixiv_login_token))
    }
}

@Composable
private fun PixivLoggedInContent(
    state: PixivLoginState,
    onActionEvent: (PixivLoginEvent.Action) -> Unit,
) {
    // 用户信息卡片 — 头像、名称、账号
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 左侧：头像
            AsyncImage(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                model = state.pixivUserAvatar,
                contentDescription = state.pixivUserName,
            )
            Spacer(modifier = Modifier.width(16.dp))
            // 右侧：用户名 + 状态
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = BgmIcons.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = state.pixivUserName.ifBlank { "已登录 Pixiv" },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                if (state.pixivUserName.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Pixiv 账号已连接",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    )
                }
                // 用户简介
                state.currentUser?.comment?.let { comment ->
                    if (comment.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = comment,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            maxLines = 2,
                        )
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 重新登录按钮
    Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onActionEvent(PixivLoginEvent.Action.OnShowTokenDialog) }
    ) {
        Icon(
            imageVector = BgmIcons.VpnKey,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("使用 Token 重新登录")
    }

    Spacer(modifier = Modifier.height(12.dp))

    // 退出登录
    OutlinedButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onActionEvent(PixivLoginEvent.Action.OnLogout) }
    ) {
        Text("退出登录")
    }
}

@Composable
private fun PixivTokenLoginDialog(
    state: PixivLoginState,
    onActionEvent: (PixivLoginEvent.Action) -> Unit,
    uriHandler: androidx.compose.ui.platform.UriHandler,
) {
    AlertDialog(
        onDismissRequest = {
            if (!state.isLoggingIn) {
                onActionEvent(PixivLoginEvent.Action.OnDismissTokenDialog)
            }
        },
        title = {
            Text(stringResource(Res.string.pixiv_token_dialog_title))
        },
        text = {
            Column {
                OutlinedTextField(
                    value = state.tokenInput,
                    onValueChange = { onActionEvent(PixivLoginEvent.Action.OnTokenInput(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(Res.string.pixiv_token_dialog_hint)) },
                    singleLine = true,
                    isError = state.tokenError != null,
                    supportingText = if (state.tokenError != null) {
                        { Text(state.tokenError!!) }
                    } else null,
                    enabled = !state.isLoggingIn,
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = { uriHandler.openUri("https://pmf.kagg886.top/docs/main/login.html") }
                ) {
                    Text(stringResource(Res.string.pixiv_token_dialog_help))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onActionEvent(PixivLoginEvent.Action.OnSubmitToken) },
                enabled = !state.isLoggingIn,
            ) {
                if (state.isLoggingIn) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("登录")
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onActionEvent(PixivLoginEvent.Action.OnDismissTokenDialog) },
                enabled = !state.isLoggingIn,
            ) {
                Text("取消")
            }
        },
    )
}
