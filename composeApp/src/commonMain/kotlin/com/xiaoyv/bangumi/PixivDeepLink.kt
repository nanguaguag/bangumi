package com.xiaoyv.bangumi

import com.xiaoyv.bangumi.shared.System
import com.xiaoyv.bangumi.shared.core.utils.debugLog
import com.xiaoyv.bangumi.shared.core.utils.errMsg
import com.xiaoyv.bangumi.shared.data.manager.app.PreferenceStore
import com.xiaoyv.bangumi.shared.data.usecase.PixivRepoUseCase
import io.ktor.http.Url
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform

/**
 * 处理 Pixiv 外部浏览器登录完成后的自定义协议回调。
 *
 * Android 和 iOS 都转发到这里，确保 code_verifier 仍从共享 PixivRepository 读取，
 * 不在平台层重复实现 OAuth 兑换逻辑。
 */
fun handlePixivDeepLink(url: String) {
    val parsed = runCatching { Url(url) }.getOrNull() ?: return
    if (parsed.protocol.name.lowercase() != "pixiv" ||
        parsed.host.lowercase() != "account" ||
        !parsed.encodedPath.lowercase().contains("login")
    ) {
        return
    }

    val code = parsed.parameters["code"].orEmpty()
    if (code.isBlank()) {
        debugLog { "Pixiv deep link did not contain an authorization code" }
        return
    }

    CoroutineScope(SupervisorJob() + System.uiDispatcher).launch {
        val result = KoinPlatform.getKoin().get<PixivRepoUseCase>().sendAuthToken(code)
        val preferenceStore = KoinPlatform.getKoin().get<PreferenceStore>()
        result
            .onSuccess {
                preferenceStore.pixivAuthError = null
            }
            .onFailure { error ->
                val message = error.errMsg.ifBlank { "Pixiv 登录失败，请重试" }
                preferenceStore.pixivAuthError = message
                debugLog { "Pixiv deep link token exchange failed" }
            }
    }
}
