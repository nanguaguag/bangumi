package com.xiaoyv.bangumi.shared.ui.component.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.ui.unit.IntOffset
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import kotlin.reflect.KClass

private const val DURATION = AnimationConstants.DefaultDurationMillis

private val IosTransitionEasing = CubicBezierEasing(0.2833f, 0.99f, 0.31833f, 0.99f)
private val SharedArtworkPageEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
private val SPEC_OFFSET = tween<IntOffset>(DURATION, easing = IosTransitionEasing)
private val SPEC_FLOAT = tween<Float>(DURATION, easing = IosTransitionEasing)
private val SHARED_ARTWORK_PAGE_ENTER = tween<Float>(
    durationMillis = 260,
    delayMillis = 70,
    easing = SharedArtworkPageEasing,
)
private val SHARED_ARTWORK_PAGE_EXIT = tween<Float>(
    durationMillis = 180,
    easing = SharedArtworkPageEasing,
)
private val SHARED_ARTWORK_PAGE_RETURN = tween<Float>(
    durationMillis = 220,
    easing = SharedArtworkPageEasing,
)

private fun AnimatedContentTransitionScope<Scene<NavKey>>.initialStateIsNavKey(screen: KClass<*>): Boolean {
    return initialState.key.toString().startsWith(screen.simpleName.orEmpty())
}

private fun AnimatedContentTransitionScope<Scene<NavKey>>.targetStateIsNavKey(screen: KClass<*>): Boolean {
    return targetState.key.toString().startsWith(screen.simpleName.orEmpty())
}

private fun AnimatedContentTransitionScope<Scene<NavKey>>.targetStateIsSharedArtworkDetail(): Boolean {
    return (targetState.key as? Screen.Gallery)?.transitionImage?.isNotBlank() == true
}

private fun AnimatedContentTransitionScope<Scene<NavKey>>.initialStateIsSharedArtworkDetail(): Boolean {
    return (initialState.key as? Screen.Gallery)?.transitionImage?.isNotBlank() == true
}

object EmptyNavTransitions {
    val transitionSpec: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = {
        ContentTransform(EnterTransition.None, ExitTransition.None)
    }
    val popTransitionSpec: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = {
        ContentTransform(EnterTransition.None, ExitTransition.None)
    }
}

object DefaultNavTransitions {
    val transitionSpec: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = {
        when {
            targetStateIsSharedArtworkDetail() -> {
                ContentTransform(
                    targetContentEnter = fadeIn(SHARED_ARTWORK_PAGE_ENTER),
                    initialContentExit = fadeOut(SHARED_ARTWORK_PAGE_EXIT),
                )
            }
            targetStateIsNavKey(Screen.PreviewMain::class) -> {
                ContentTransform(fadeIn(SPEC_FLOAT), fadeOut(SPEC_FLOAT))
            }
            else -> {
                ContentTransform(
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = SPEC_OFFSET,
                    ),
                    fadeOut(SPEC_FLOAT) + slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = SPEC_OFFSET,
                        targetOffset = { fullOffset -> (fullOffset * 0.3f).toInt() },
                    ),
                )
            }
        }
    }

    val popTransitionSpec: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = {
        when {
            initialStateIsSharedArtworkDetail() -> {
                ContentTransform(
                    targetContentEnter = fadeIn(SHARED_ARTWORK_PAGE_RETURN),
                    initialContentExit = fadeOut(SHARED_ARTWORK_PAGE_EXIT),
                )
            }
            initialStateIsNavKey(Screen.PreviewMain::class) -> {
                ContentTransform(fadeIn(SPEC_FLOAT), fadeOut(SPEC_FLOAT))
            }
            else -> {
                ContentTransform(
                    fadeIn(SPEC_FLOAT) + slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.End,
                        animationSpec = SPEC_OFFSET,
                        initialOffset = { fullOffset -> (fullOffset * 0.3f).toInt() },
                    ),
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.End,
                        animationSpec = SPEC_OFFSET,
                    ),
                )
            }
        }
    }
}

object FadeNavTransitions {
    val transitionSpec: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = {
        if (targetStateIsSharedArtworkDetail()) {
            ContentTransform(
                targetContentEnter = fadeIn(SHARED_ARTWORK_PAGE_ENTER),
                initialContentExit = fadeOut(SHARED_ARTWORK_PAGE_EXIT),
            )
        } else {
            ContentTransform(fadeIn(SPEC_FLOAT), fadeOut(SPEC_FLOAT))
        }
    }

    val popTransitionSpec: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = {
        if (initialStateIsSharedArtworkDetail()) {
            ContentTransform(
                targetContentEnter = fadeIn(SHARED_ARTWORK_PAGE_RETURN),
                initialContentExit = fadeOut(SHARED_ARTWORK_PAGE_EXIT),
            )
        } else {
            ContentTransform(fadeIn(SPEC_FLOAT), fadeOut(SPEC_FLOAT))
        }
    }
}
