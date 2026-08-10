package com.xiaoyv.bangumi.shared.ui.component.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.navigation3.ui.LocalNavAnimatedContentScope

/**
 * 为跨 NavDisplay destination 的作品图提供同一个共享转场作用域。
 */
val LocalScreenSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

private val ArtworkEnterEasing = CubicBezierEasing(0.16f, 0.82f, 0.24f, 1.0f)
private val ArtworkReturnEasing = CubicBezierEasing(0.22f, 0.0f, 0.18f, 1.0f)

/**
 * 作品图的尺寸和位置全程由同一条缓动曲线驱动，放大与上升同步发生，
 * 不设置中间关键帧或越界回弹，避免形成可感知的二段动画。
 */
private fun pixivArtworkBoundsTransform(
    initialBounds: Rect,
    targetBounds: Rect,
): FiniteAnimationSpec<Rect> {
    val isExpanding = targetBounds.width * targetBounds.height > initialBounds.width * initialBounds.height
    return if (isExpanding) {
        tween(durationMillis = 460, easing = ArtworkEnterEasing)
    } else {
        tween(durationMillis = 300, easing = ArtworkReturnEasing)
    }
}

/**
 * 让 Pixiv 瀑布流缩略图与作品页首图在导航期间共享同一份内容状态。
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.pixivArtworkSharedElement(illustId: String): Modifier {
    val sharedTransitionScope = LocalScreenSharedTransitionScope.current ?: return this
    val animatedVisibilityScope = LocalNavAnimatedContentScope.current

    return with(sharedTransitionScope) {
        this@pixivArtworkSharedElement.sharedElement(
            sharedContentState = rememberSharedContentState(key = "pixiv-artwork-$illustId"),
            animatedVisibilityScope = animatedVisibilityScope,
            boundsTransform = ::pixivArtworkBoundsTransform,
        )
    }
}
