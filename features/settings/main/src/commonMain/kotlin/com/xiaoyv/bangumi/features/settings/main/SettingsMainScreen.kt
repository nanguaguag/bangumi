package com.xiaoyv.bangumi.features.settings.main

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Cached
import androidx.compose.material.icons.rounded.DisplaySettings
import androidx.compose.material.icons.rounded.Feedback
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.ManageAccounts
import androidx.compose.material.icons.rounded.Money
import androidx.compose.material.icons.rounded.NetworkCheck
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Source
import androidx.compose.material.icons.rounded.TableBar
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.xiaoyv.bangumi.core_resource.resources.Res
import com.xiaoyv.bangumi.core_resource.resources.global_avatar
import com.xiaoyv.bangumi.core_resource.resources.pixiv_account_not_login
import com.xiaoyv.bangumi.core_resource.resources.pixiv_account_not_login_desc
import com.xiaoyv.bangumi.core_resource.resources.pixiv_account_section
import com.xiaoyv.bangumi.core_resource.resources.pixiv_go_login
import com.xiaoyv.bangumi.core_resource.resources.pixiv_logout
import com.xiaoyv.bangumi.core_resource.resources.pixiv_logout_confirm
import com.xiaoyv.bangumi.core_resource.resources.pixiv_logout_desc
import com.xiaoyv.bangumi.core_resource.resources.settings_about
import com.xiaoyv.bangumi.core_resource.resources.settings_about_app
import com.xiaoyv.bangumi.core_resource.resources.settings_about_author
import com.xiaoyv.bangumi.core_resource.resources.settings_account
import com.xiaoyv.bangumi.core_resource.resources.settings_account_info
import com.xiaoyv.bangumi.core_resource.resources.settings_bar
import com.xiaoyv.bangumi.core_resource.resources.settings_block_user
import com.xiaoyv.bangumi.core_resource.resources.settings_clean_cache
import com.xiaoyv.bangumi.core_resource.resources.settings_common
import com.xiaoyv.bangumi.core_resource.resources.settings_donate
import com.xiaoyv.bangumi.core_resource.resources.settings_feedback
import com.xiaoyv.bangumi.core_resource.resources.settings_live2d
import com.xiaoyv.bangumi.core_resource.resources.settings_logout
import com.xiaoyv.bangumi.core_resource.resources.settings_logout_desc
import com.xiaoyv.bangumi.core_resource.resources.settings_network
import com.xiaoyv.bangumi.core_resource.resources.settings_privacy
import com.xiaoyv.bangumi.core_resource.resources.settings_qq_group
import com.xiaoyv.bangumi.core_resource.resources.settings_relate
import com.xiaoyv.bangumi.core_resource.resources.settings_source
import com.xiaoyv.bangumi.core_resource.resources.settings_title
import com.xiaoyv.bangumi.core_resource.resources.settings_ui
import com.xiaoyv.bangumi.core_resource.resources.settings_user_argument
import com.xiaoyv.bangumi.core_resource.resources.settings_user_privacy
import com.xiaoyv.bangumi.features.settings.main.business.SettingsMainEvent
import com.xiaoyv.bangumi.features.settings.main.business.SettingsMainState
import com.xiaoyv.bangumi.features.settings.main.business.SettingsMainViewModel
import com.xiaoyv.bangumi.shared.core.mvi.BaseState
import com.xiaoyv.bangumi.shared.data.manager.app.PreferenceStore
import com.xiaoyv.bangumi.shared.data.manager.shared.LocalSharedState
import com.xiaoyv.bangumi.shared.data.usecase.PixivRepoUseCase
import com.xiaoyv.bangumi.shared.ui.component.action.LocalActionHandler
import com.xiaoyv.bangumi.shared.ui.component.bar.BgmLargeTopAppBar
import com.xiaoyv.bangumi.shared.ui.component.dialog.alert.BgmAlertDialog
import com.xiaoyv.bangumi.shared.ui.component.dialog.alert.rememberAlertDialogState
import com.xiaoyv.bangumi.shared.ui.component.layout.state.StateLayout
import com.xiaoyv.bangumi.shared.ui.component.navigation.Screen
import com.xiaoyv.bangumi.shared.ui.component.settings.SettingContainer
import com.xiaoyv.bangumi.shared.ui.component.settings.SettingItem
import com.xiaoyv.bangumi.features.settings.main.component.BangumiStatusTopBarAction
import com.xiaoyv.bangumi.shared.ui.kts.collectBaseSideEffect
import com.xiaoyv.bangumi.shared.ui.theme.BgmIcons
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import kotlin.random.Random

@Composable
fun SettingsMainRoute(
    viewModel: SettingsMainViewModel,
    onNavUp: () -> Unit,
    onNavScreen: (Screen) -> Unit,
) {
    val baseState by viewModel.collectAsState()

    viewModel.collectBaseSideEffect {

    }

    SettingsMainScreen(
        baseState = baseState,
        onActionEvent = viewModel::onEvent,
        onUiEvent = {
            when (it) {
                is SettingsMainEvent.UI.OnNavUp -> onNavUp()
                is SettingsMainEvent.UI.OnNavScreen -> onNavScreen(it.screen)
            }
        },
    )
}

@Composable
private fun SettingsMainScreen(
    baseState: BaseState<SettingsMainState>,
    onUiEvent: (SettingsMainEvent.UI) -> Unit,
    onActionEvent: (SettingsMainEvent.Action) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        snapAnimationSpec = spring(stiffness = Spring.StiffnessHigh)
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            BgmLargeTopAppBar(
                title = stringResource(Res.string.settings_title),
                scrollBehavior = scrollBehavior,
                actions = {
                    BangumiStatusTopBarAction()
                },
                onNavigationClick = { onUiEvent(SettingsMainEvent.UI.OnNavUp) }
            )
        }
    ) {
        StateLayout(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(it),
            baseState = baseState,
        ) { state ->
            SettingsMainScreenContent(state, onUiEvent, onActionEvent)
        }
    }
}


@Composable
private fun SettingsMainScreenContent(
    state: SettingsMainState,
    onUiEvent: (SettingsMainEvent.UI) -> Unit,
    onActionEvent: (SettingsMainEvent.Action) -> Unit,
) {
    val actionHandler = LocalActionHandler.current
    val preferenceStore = koinInject<PreferenceStore>()
    val pixivRepoUseCase = koinInject<PixivRepoUseCase>()
    val scope = rememberCoroutineScope()

    val pixivUser = state.pixivUser
    val pixivLoggedIn = state.pixivLoggedIn

    Column(modifier = Modifier.padding(vertical = 24.dp)) {
        // Bangumi 账号
        SettingContainer(label = { Text(text = stringResource(Res.string.settings_account)) }) {
            SettingItem(
                title = stringResource(Res.string.settings_account_info),
                icon = BgmIcons.ManageAccounts,
                onClick = { onUiEvent(SettingsMainEvent.UI.OnNavScreen(Screen.SettingsAccount)) }
            )
            SettingItem(
                title = stringResource(Res.string.settings_privacy),
                icon = BgmIcons.PrivacyTip,
                onClick = { onUiEvent(SettingsMainEvent.UI.OnNavScreen(Screen.SettingsPrivacy)) }
            )
            SettingItem(
                title = stringResource(Res.string.settings_block_user),
                icon = BgmIcons.Block,
                onClick = { onUiEvent(SettingsMainEvent.UI.OnNavScreen(Screen.SettingsBlock)) }
            )
        }

        // Pixiv 账号
        SettingContainer(label = { Text(text = stringResource(Res.string.pixiv_account_section)) }) {
            if (pixivLoggedIn) {
                // 已登录 — 显示 Pixiv 用户资料
                if (pixivUser != null) {
                    PixivProfileCard(
                        user = pixivUser,
                        onLogout = {
                            scope.launch {
                                pixivRepoUseCase.logout()
                                onActionEvent(SettingsMainEvent.Action.OnRefresh(true))
                            }
                        },
                        onGoToLogin = {
                            onUiEvent(SettingsMainEvent.UI.OnNavScreen(Screen.PixivLogin))
                        },
                    )
                } else {
                    // token 有效但用户数据还在加载中
                    SettingItem(
                        title = stringResource(Res.string.pixiv_go_login),
                        icon = BgmIcons.AccountCircle,
                        supportingContent = {
                            Text(text = "Pixiv 已登录，正在加载用户信息...")
                        },
                        divider = false,
                        onClick = { onUiEvent(SettingsMainEvent.UI.OnNavScreen(Screen.PixivLogin)) }
                    )
                }
            } else {
                // 未登录 — 显示登录入口
                SettingItem(
                    title = stringResource(Res.string.pixiv_account_not_login),
                    icon = BgmIcons.AccountCircle,
                    supportingContent = {
                        Text(text = stringResource(Res.string.pixiv_account_not_login_desc))
                    },
                    divider = false,
                    onClick = { onUiEvent(SettingsMainEvent.UI.OnNavScreen(Screen.PixivLogin)) }
                )
            }
        }

        SettingContainer(label = { Text(text = stringResource(Res.string.settings_common)) }) {
            SettingItem(
                title = stringResource(Res.string.settings_live2d),
                icon = BgmIcons.Person,
                onClick = { onUiEvent(SettingsMainEvent.UI.OnNavScreen(Screen.SettingsLive2d)) }
            )
            SettingItem(
                title = stringResource(Res.string.settings_ui),
                icon = BgmIcons.DisplaySettings,
                onClick = { onUiEvent(SettingsMainEvent.UI.OnNavScreen(Screen.SettingsUi)) }
            )
            SettingItem(
                title = stringResource(Res.string.settings_bar),
                icon = BgmIcons.TableBar,
                onClick = { onUiEvent(SettingsMainEvent.UI.OnNavScreen(Screen.SettingsBar)) }
            )
            SettingItem(
                title = stringResource(Res.string.settings_network),
                icon = BgmIcons.NetworkCheck,
                onClick = { onUiEvent(SettingsMainEvent.UI.OnNavScreen(Screen.SettingsNetwork)) }
            )
            SettingItem(
                title = stringResource(Res.string.settings_clean_cache),
                icon = BgmIcons.Cached,
                trailingContent = null,
                onClick = {
                    onActionEvent(SettingsMainEvent.Action.OnCleanCache)
                }
            )
        }

        SettingContainer(label = { Text(text = stringResource(Res.string.settings_relate)) }) {
            SettingItem(
                title = stringResource(Res.string.settings_feedback),
                icon = BgmIcons.Feedback,
                trailingContent = null,
                onClick = { actionHandler.openInBrowser("https://github.com/xiaoyvyv/bangumi/issues") }
            )
            SettingItem(
                title = stringResource(Res.string.settings_donate),
                icon = BgmIcons.Money,
                trailingContent = null,
                onClick = { actionHandler.openInBrowser("https://lain.bgm.tv/pic/photo/l/47/7e/837364_do644.jpg") }
            )
            SettingItem(
                title = stringResource(Res.string.settings_qq_group),
                icon = BgmIcons.Groups,
                trailingContent = null,
                onClick = { actionHandler.openInBrowser("https://qm.qq.com/q/YomiSMeyUs") }
            )
            SettingItem(
                title = stringResource(Res.string.settings_source),
                icon = BgmIcons.Source,
                trailingContent = null,
                onClick = { actionHandler.openInBrowser("https://github.com/xiaoyvyv/bangumi") }
            )
        }

        SettingContainer(label = { Text(text = stringResource(Res.string.settings_about)) }) {
            SettingItem(
                title = stringResource(Res.string.settings_user_argument),
                icon = BgmIcons.Security,
                trailingContent = null,
                onClick = { actionHandler.openInBrowser("https://xiaoyvyv.github.io/bangumi/lib-doc/build/argument.html?_=${Random.nextLong()}") }
            )
            SettingItem(
                title = stringResource(Res.string.settings_user_privacy),
                icon = BgmIcons.PrivacyTip,
                trailingContent = null,
                onClick = { actionHandler.openInBrowser("https://xiaoyvyv.github.io/bangumi/lib-doc/build/starter.html?_=${Random.nextLong()}") }
            )
            SettingItem(
                title = stringResource(Res.string.settings_about_author),
                icon = BgmIcons.Info,
                trailingContent = null,
                onClick = { actionHandler.openInBrowser("https://github.com/xiaoyvyv") }
            )
            SettingItem(
                title = stringResource(Res.string.settings_about_app),
                icon = BgmIcons.Apps,
                trailingContent = null,
                onClick = { actionHandler.openInBrowser("https://github.com/xiaoyvyv/bangumi") }
            )
        }


        if (LocalSharedState.current.isLogin) {
            val confirmLogoutDialog = rememberAlertDialogState()
            BgmAlertDialog(
                state = confirmLogoutDialog,
                title = stringResource(Res.string.settings_logout),
                text = stringResource(Res.string.settings_logout_desc),
                onConfirm = {
                    onActionEvent(SettingsMainEvent.Action.OnLogout)
                }
            )

            Spacer(Modifier.height(24.dp))
            SettingItem(
                title = stringResource(Res.string.settings_logout),
                divider = false,
                trailingContent = null,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                ),
                onClick = { confirmLogoutDialog.show() }
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

/**
 * Pixiv 用户资料卡片 — 直接在设置页展示账号信息
 */
@Composable
private fun PixivProfileCard(
    user: com.xiaoyv.bangumi.shared.data.model.response.pixiv.ComposePixivCurrentUser,
    onLogout: () -> Unit,
    onGoToLogin: () -> Unit,
) {
    val logoutConfirmDialog = rememberAlertDialogState()

    BgmAlertDialog(
        state = logoutConfirmDialog,
        title = stringResource(Res.string.pixiv_logout),
        text = stringResource(Res.string.pixiv_logout_desc),
        onConfirm = onLogout,
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // 第一行：头像 + 名称 + 退出按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 头像
            AsyncImage(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                model = user.profileImageUrls?.medium,
                contentDescription = stringResource(Res.string.global_avatar),
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 名称和账号
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.name ?: "Pixiv User",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (user.isPremium) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = BgmIcons.Verified,
                            contentDescription = "Premium",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                if (user.account != null) {
                    Text(
                        text = "@${user.account}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // 退出登录按钮
            IconButton(onClick = { logoutConfirmDialog.show() }) {
                Icon(
                    imageVector = BgmIcons.Logout,
                    contentDescription = stringResource(Res.string.pixiv_logout),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }

        // 简介
        if (!user.comment.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = user.comment!!,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
            )
        }

        // 账号信息
        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            ProfileInfoItem(label = "Pixiv ID", value = user.id.toString())
            if (user.account != null) {
                ProfileInfoItem(label = "账号", value = user.account!!)
            }
        }

        // 重新登录入口
        Spacer(modifier = Modifier.height(12.dp))
        SettingItem(
            title = "管理 Pixiv 登录",
            icon = BgmIcons.AccountCircle,
            divider = false,
            onClick = onGoToLogin,
        )
    }
}

@Composable
private fun ProfileInfoItem(
    label: String,
    value: String,
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
