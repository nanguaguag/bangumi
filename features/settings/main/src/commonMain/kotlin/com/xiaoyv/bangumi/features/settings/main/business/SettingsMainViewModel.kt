package com.xiaoyv.bangumi.features.settings.main.business

import androidx.lifecycle.SavedStateHandle
import com.xiaoyv.bangumi.core_resource.resources.Res
import com.xiaoyv.bangumi.core_resource.resources.pixiv_download_dir_invalid
import com.xiaoyv.bangumi.core_resource.resources.pixiv_download_dir_updated
import com.xiaoyv.bangumi.core_resource.resources.settings_clean_cache_success
import com.xiaoyv.bangumi.shared.System
import com.xiaoyv.bangumi.shared.core.mvi.BaseSyntax
import com.xiaoyv.bangumi.shared.core.mvi.BaseViewModel
import com.xiaoyv.bangumi.shared.core.utils.debugLog
import com.xiaoyv.bangumi.shared.data.manager.app.PreferenceStore
import com.xiaoyv.bangumi.shared.data.manager.app.UserManager
import com.xiaoyv.bangumi.shared.data.model.response.pixiv.ComposePixivCurrentUser
import com.xiaoyv.bangumi.shared.data.usecase.PixivRepoUseCase
import org.jetbrains.compose.resources.getString

/**
 * [SettingsMainViewModel]
 *
 * @author why
 * @since 2025/1/12
 */
class SettingsMainViewModel(
    savedStateHandle: SavedStateHandle,
    private val userManager: UserManager,
    private val pixivRepoUseCase: PixivRepoUseCase,
    private val preferenceStore: PreferenceStore,
) : BaseViewModel<SettingsMainState, SettingsMainSideEffect, SettingsMainEvent.Action>(savedStateHandle) {

    override fun initSate(onCreate: Boolean): SettingsMainState {
        val token = preferenceStore.pixivToken
        val isLoggedIn = token.accessToken.isNotBlank()
        debugLog { "SettingsMain initSate: accessToken=${token.accessToken.take(8)}, isBlank=${token.accessToken.isBlank()}, pixivLoggedIn=$isLoggedIn" }
        return SettingsMainState(
            pixivLoggedIn = isLoggedIn,
            pixivDownloadDir = userManager.pixivDownloadDir,
        )
    }

    override fun onEvent(event: SettingsMainEvent.Action) {
        when (event) {
            is SettingsMainEvent.Action.OnRefresh -> refresh(event.loading)
            SettingsMainEvent.Action.OnLogout -> onLogout()
            SettingsMainEvent.Action.OnCleanCache -> onCleanCache()
            is SettingsMainEvent.Action.OnUpdatePixivDownloadDir -> onUpdatePixivDownloadDir(event.dir)
        }
    }

    override suspend fun BaseSyntax<SettingsMainState, SettingsMainSideEffect>.refreshSync() {
        val token = preferenceStore.pixivToken
        val isLoggedIn = token.accessToken.isNotBlank()
        debugLog { "SettingsMain refreshSync: pixivLoggedIn=$isLoggedIn" }

        var pixivUser: ComposePixivCurrentUser? = null
        if (isLoggedIn) {
            pixivRepoUseCase.fetchCurrentUser()
                .onSuccess {
                    debugLog { "SettingsMain refreshSync: user=${it.name}, id=${it.id}" }
                    pixivUser = it
                }
                .onFailure {
                    debugLog { "SettingsMain refreshSync: fetchCurrentUser FAILED=${it.message}" }
                }
        }

        reduceContent {
            state.copy(
                pixivLoggedIn = isLoggedIn,
                pixivUser = pixivUser,
                pixivDownloadDir = userManager.pixivDownloadDir,
            )
        }
    }

    private fun onLogout() = action {
        withActionLoading { userManager.logout() }
    }

    private fun onCleanCache() = action {
        withActionLoading { System.cleanCache() }
            .onSuccess {
                postToast { getString(Res.string.settings_clean_cache_success) }
            }
    }

    private fun onUpdatePixivDownloadDir(dir: String) = action {
        val newDir = dir.trim().trim('/')
        if (newDir.isBlank()) {
            postToast { getString(Res.string.pixiv_download_dir_invalid) }
            return@action
        }
        userManager.pixivDownloadDir = newDir
        reduceContent { state.copy(pixivDownloadDir = newDir) }
        postToast { getString(Res.string.pixiv_download_dir_updated) }
    }
}
