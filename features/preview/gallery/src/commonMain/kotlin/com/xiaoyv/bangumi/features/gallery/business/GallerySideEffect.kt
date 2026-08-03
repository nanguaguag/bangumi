package com.xiaoyv.bangumi.features.gallery.business

/**
 * [GallerySideEffect]
 *
 * @author why
 * @since 2025/1/12
 */
sealed class GallerySideEffect {
    data class OpenInBrowser(val url: String) : GallerySideEffect()
    data class NavigateToTagSearch(val tag: String) : GallerySideEffect()
}
