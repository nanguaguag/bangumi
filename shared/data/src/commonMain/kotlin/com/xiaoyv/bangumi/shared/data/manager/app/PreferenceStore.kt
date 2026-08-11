package com.xiaoyv.bangumi.shared.data.manager.app

import com.xiaoyv.bangumi.shared.data.constant.SpKey
import com.xiaoyv.bangumi.shared.data.model.response.bgm.ComposeAuthToken
import com.xiaoyv.bangumi.shared.data.model.response.bgm.ComposeSetting
import com.xiaoyv.bangumi.shared.data.model.response.bgm.user.ComposeUser
import com.xiaoyv.bangumi.shared.data.model.response.pixiv.ComposePixivToken
import com.xiaoyv.bangumi.shared.data.repository.CacheRepository
import com.xiaoyv.bangumi.shared.data.repository.DatabaseRepository
import com.xiaoyv.bangumi.shared.data.repository.boolean
import com.xiaoyv.bangumi.shared.data.repository.serializable
import com.xiaoyv.bangumi.shared.data.repository.string
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 本地SP数据缓存，禁止UI模块直接调用，需要在 common层委托读写
 */
class PreferenceStore(
    cacheRepository: CacheRepository,
    private val databaseRepository: DatabaseRepository,
) {
    var userInfo: ComposeUser
        get() = databaseRepository.fetchCurrentUser()
        set(value) = databaseRepository.sendSaveUser(value)

    val isLogin get() = userToken != ComposeAuthToken.Empty && userInfo != ComposeUser.Empty

    /**
     * 用户是否同意隐私政策
     */
    internal var isAgreePrivacy by cacheRepository.boolean(SpKey.KEY_USER_AGREE_PRIVACY)

    /**
     * 用户是首次使用
     */
    internal var isUserFirstUse by cacheRepository.boolean(SpKey.KEY_USER_FIRST_USE, default = true)

    /**
     * 用户登录的 Bangumi-Token 数据
     */
    internal var userToken by cacheRepository.serializable(SpKey.KEY_USER_BGM_TOKEN, ComposeAuthToken.Empty)

    /**
     * 用户登录的 Pixiv-Token 数据
     */
    private var storedPixivToken by cacheRepository.serializable(
        SpKey.KEY_USER_PIXIV_TOKEN,
        ComposePixivToken.Empty,
    )
    private val _pixivToken = MutableStateFlow(storedPixivToken)
    val pixivTokenFlow = _pixivToken.asStateFlow()

    /** 外部 OAuth 回调的短生命周期错误，不写入持久化存储。 */
    private val _pixivAuthError = MutableStateFlow<String?>(null)
    val pixivAuthErrorFlow = _pixivAuthError.asStateFlow()

    var pixivAuthError: String?
        get() = _pixivAuthError.value
        set(value) {
            _pixivAuthError.value = value
        }

    var pixivToken: ComposePixivToken
        get() = storedPixivToken
        set(value) {
            storedPixivToken = value
            _pixivToken.value = value
        }

    /**
     * APP 设置
     */
    internal var settings by cacheRepository.serializable(SpKey.KEY_SETTING, ComposeSetting.Default)

    /**
     * Pixiv 图片下载目录（Downloads 下的子目录名）
     */
    var pixivDownloadDir by cacheRepository.string(SpKey.KEY_PIXIV_DOWNLOAD_DIR, default = "bangumi")
}
