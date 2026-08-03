package com.xiaoyv.bangumi.features.pixiv.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Login
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.OpenInBrowser
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.VpnKey
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.xiaoyv.bangumi.core_resource.resources.Res
import com.xiaoyv.bangumi.core_resource.resources.pixiv_account_info
import com.xiaoyv.bangumi.core_resource.resources.pixiv_account_not_login
import com.xiaoyv.bangumi.core_resource.resources.pixiv_account_not_login_desc
import com.xiaoyv.bangumi.core_resource.resources.pixiv_birthday
import com.xiaoyv.bangumi.core_resource.resources.pixiv_country
import com.xiaoyv.bangumi.core_resource.resources.pixiv_homepage
import com.xiaoyv.bangumi.core_resource.resources.pixiv_is_premium
import com.xiaoyv.bangumi.core_resource.resources.pixiv_job
import com.xiaoyv.bangumi.core_resource.resources.pixiv_login_browser
import com.xiaoyv.bangumi.core_resource.resources.pixiv_login_guide
import com.xiaoyv.bangumi.core_resource.resources.pixiv_login_success
import com.xiaoyv.bangumi.core_resource.resources.pixiv_login_token
import com.xiaoyv.bangumi.core_resource.resources.pixiv_login_webview
import com.xiaoyv.bangumi.core_resource.resources.pixiv_logout
import com.xiaoyv.bangumi.core_resource.resources.pixiv_logout_confirm
import com.xiaoyv.bangumi.core_resource.resources.pixiv_logout_desc
import com.xiaoyv.bangumi.core_resource.resources.pixiv_manage_login
import com.xiaoyv.bangumi.core_resource.resources.pixiv_no
import com.xiaoyv.bangumi.core_resource.resources.pixiv_not_set
import com.xiaoyv.bangumi.core_resource.resources.pixiv_open_homepage
import com.xiaoyv.bangumi.core_resource.resources.pixiv_pawoo_link
import com.xiaoyv.bangumi.core_resource.resources.pixiv_personal_profile
import com.xiaoyv.bangumi.core_resource.resources.pixiv_re_login
import com.xiaoyv.bangumi.core_resource.resources.pixiv_region
import com.xiaoyv.bangumi.core_resource.resources.pixiv_token_dialog_help
import com.xiaoyv.bangumi.core_resource.resources.pixiv_token_dialog_hint
import com.xiaoyv.bangumi.core_resource.resources.pixiv_token_dialog_title
import com.xiaoyv.bangumi.core_resource.resources.pixiv_twitter_account
import com.xiaoyv.bangumi.core_resource.resources.pixiv_twitter_link
import com.xiaoyv.bangumi.core_resource.resources.pixiv_user_id
import com.xiaoyv.bangumi.core_resource.resources.pixiv_username
import com.xiaoyv.bangumi.core_resource.resources.pixiv_yes
import com.xiaoyv.bangumi.features.pixiv.login.business.PixivLoginEvent
import com.xiaoyv.bangumi.features.pixiv.login.business.PixivLoginSideEffect
import com.xiaoyv.bangumi.features.pixiv.login.business.PixivLoginState
import com.xiaoyv.bangumi.features.pixiv.login.business.PixivLoginViewModel
import com.xiaoyv.bangumi.shared.core.mvi.BaseState
import com.xiaoyv.bangumi.shared.core.utils.debugLog
import com.xiaoyv.bangumi.shared.data.repository.PixivRepository
import com.xiaoyv.bangumi.shared.ui.component.bar.BgmTopAppBar
import com.xiaoyv.bangumi.shared.ui.component.dialog.alert.BgmAlertDialog
import com.xiaoyv.bangumi.shared.ui.component.dialog.alert.rememberAlertDialogState
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
                title = stringResource(Res.string.pixiv_manage_login),
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
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
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
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = BgmIcons.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(Res.string.pixiv_login_success),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }
        }

        if (state.isLoggedIn) {
            // 已登录状态 — 账号管理
            PixivLoggedInContent(state, onActionEvent, uriHandler)
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
    // 未登录状态提示
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp),
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
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp),
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = stringResource(Res.string.pixiv_account_not_login),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(Res.string.pixiv_account_not_login_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    // 登录向导说明
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Text(
            modifier = Modifier.padding(20.dp),
            text = stringResource(Res.string.pixiv_login_guide),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    // 应用内 WebView 登录按钮
    Button(
        modifier = Modifier.fillMaxWidth(),
        enabled = !state.isLoggingIn,
        shape = RoundedCornerShape(14.dp),
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

    Spacer(modifier = Modifier.height(4.dp))

    // 外部浏览器登录按钮
    OutlinedButton(
        modifier = Modifier.fillMaxWidth(),
        enabled = !state.isLoggingIn,
        shape = RoundedCornerShape(14.dp),
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

    // Token 登录 —— 底部文字按钮
    OutlinedButton(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
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
    uriHandler: androidx.compose.ui.platform.UriHandler,
) {
    val logoutConfirmDialog = rememberAlertDialogState()

    BgmAlertDialog(
        state = logoutConfirmDialog,
        title = stringResource(Res.string.pixiv_logout_confirm),
        text = stringResource(Res.string.pixiv_logout_desc),
        onConfirm = { onActionEvent(PixivLoginEvent.Action.OnLogout) },
    )

    // 用户信息头部卡片 — 头像、名称、账号
    ProfileHeaderCard(state = state)

    // 账户信息
    state.currentUser?.let { user ->
        SectionCard(title = stringResource(Res.string.pixiv_account_info)) {
            ProfileInfoRow(
                label = stringResource(Res.string.pixiv_username),
                value = user.name.orEmpty().ifBlank { "—" },
                icon = BgmIcons.Person,
                divider = true,
            )
            ProfileInfoRow(
                label = stringResource(Res.string.pixiv_user_id),
                value = user.id.toString(),
                icon = BgmIcons.Badge,
            )
        }
    }

    // 个人资料
    SectionCard(title = stringResource(Res.string.pixiv_personal_profile)) {
        val profile = state.userProfile
        val notSet = stringResource(Res.string.pixiv_not_set)
        ProfileInfoRow(
            label = stringResource(Res.string.pixiv_birthday),
            value = profile?.birth.orEmpty().ifBlank { notSet },
            icon = BgmIcons.Cake,
            divider = true,
        )
        ProfileInfoRow(
            label = stringResource(Res.string.pixiv_job),
            value = profile?.job.orEmpty().ifBlank { notSet },
            icon = BgmIcons.Work,
            divider = true,
        )
        ProfileInfoRow(
            label = stringResource(Res.string.pixiv_region),
            value = profile?.region.orEmpty().ifBlank { notSet },
            icon = BgmIcons.Place,
            divider = true,
        )
        ProfileInfoRow(
            label = stringResource(Res.string.pixiv_country),
            value = profile?.country.orEmpty().ifBlank { notSet },
            icon = BgmIcons.Public,
            divider = true,
        )
        ProfileInfoRow(
            label = stringResource(Res.string.pixiv_homepage),
            value = profile?.webpage.orEmpty().ifBlank { notSet },
            icon = BgmIcons.Language,
            divider = true,
            onClick = profile?.webpage
                ?.takeIf { it.isNotBlank() && it != "http://" && it != "https://" }
                ?.let { url -> { uriHandler.openUri(url) } },
        )
        ProfileInfoRow(
            label = stringResource(Res.string.pixiv_twitter_account),
            value = profile?.twitterAccount.orEmpty().ifBlank { notSet },
            icon = BgmIcons.AlternateEmail,
            divider = true,
            onClick = profile?.twitterAccount
                ?.takeIf { it.isNotBlank() }
                ?.let { account -> { uriHandler.openUri("https://twitter.com/$account") } },
        )
        ProfileInfoRow(
            label = stringResource(Res.string.pixiv_twitter_link),
            value = profile?.twitterUrl.orEmpty().ifBlank { notSet },
            icon = BgmIcons.Link,
            divider = true,
            onClick = profile?.twitterUrl
                ?.takeIf { it.isNotBlank() && it.startsWith("http") }
                ?.let { url -> { uriHandler.openUri(url) } },
        )
        ProfileInfoRow(
            label = stringResource(Res.string.pixiv_pawoo_link),
            value = profile?.pawooUrl.orEmpty().ifBlank { notSet },
            icon = BgmIcons.Link,
            divider = true,
            onClick = profile?.pawooUrl
                ?.takeIf { it.isNotBlank() && it.startsWith("http") }
                ?.let { url -> { uriHandler.openUri(url) } },
        )
        ProfileInfoRow(
            label = stringResource(Res.string.pixiv_is_premium),
            value = if (profile?.isPremium == true) stringResource(Res.string.pixiv_yes)
            else stringResource(Res.string.pixiv_no),
            icon = BgmIcons.Verified,
        )
    }

    // 打开我的 Pixiv 主页
    Button(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        onClick = {
            state.currentUser?.id?.takeIf { it > 0 }?.let { id ->
                uriHandler.openUri("https://www.pixiv.net/users/$id")
            }
        }
    ) {
        Icon(
            imageVector = BgmIcons.OpenInBrowser,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(stringResource(Res.string.pixiv_open_homepage))
    }

    // 使用 Token 重新登录
    OutlinedButton(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        onClick = { onActionEvent(PixivLoginEvent.Action.OnShowTokenDialog) }
    ) {
        Icon(
            imageVector = BgmIcons.VpnKey,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(stringResource(Res.string.pixiv_re_login))
    }

    // 退出登录
    OutlinedButton(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        onClick = { logoutConfirmDialog.show() }
    ) {
        Icon(
            imageVector = BgmIcons.Logout,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(Res.string.pixiv_logout),
            color = MaterialTheme.colorScheme.error,
        )
    }
}

/**
 * 用户信息头部卡片 — 渐变背景 + 大头像
 */
@Composable
private fun ProfileHeaderCard(state: PixivLoginState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.surfaceVariant,
                        )
                    )
                )
                .padding(20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 头像
                AsyncImage(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface),
                    contentScale = ContentScale.Crop,
                    model = state.pixivUserAvatar.ifBlank { null },
                    contentDescription = state.pixivUserName,
                )
                Spacer(modifier = Modifier.width(16.dp))
                // 名称 + 账号
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = BgmIcons.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = state.pixivUserName.ifBlank { "Pixiv User" },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (state.currentUser?.isPremium == true) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = BgmIcons.Verified,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                    state.currentUser?.account?.let { account ->
                        if (account.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "@$account",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    state.currentUser?.comment?.let { comment ->
                        if (comment.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = comment,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 分区卡片：标题 + 内容
 */
@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            content()
        }
    }
}

/**
 * 资料信息行：图标 + 标签 + 值，可选点击（跳转链接）
 */
@Composable
private fun ColumnScope.ProfileInfoRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    divider: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null, onClick = { onClick?.invoke() })
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            modifier = Modifier.width(96.dp),
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            modifier = Modifier.weight(1.2f),
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (onClick != null) FontWeight.Medium else FontWeight.Normal,
            color = if (onClick != null) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
    if (divider) {
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
        )
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
