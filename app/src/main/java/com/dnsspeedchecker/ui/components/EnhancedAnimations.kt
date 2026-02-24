package com.dnsspeedchecker.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Enhanced Animation Specs
object AnimationSpecs {
    val Fast = tween<Float>(300, easing = EaseOutCubic)
    val Medium = tween<Float>(500, easing = EaseInOutCubic)
    val Slow = tween<Float>(800, easing = EaseInOutCubic)
    
    val SpringBounce = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
    
    val SpringGentle = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow
    )
    
    val InfinitePulse = infiniteRepeatable<Float>(
        animation = tween(1000, easing = EaseInOutCubic),
        repeatMode = RepeatMode.Reverse
    )
    
    val InfiniteRotation = infiniteRepeatable<Float>(
        animation = tween(2000, easing = LinearEasing),
        repeatMode = RepeatMode.Restart
    )
}

// Enhanced Animation Modifiers
fun Modifier.fadeInAnimation(
    visible: Boolean,
    animationSpec: FiniteAnimationSpec<Float> = AnimationSpecs.Medium
): Modifier = composed {
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = animationSpec,
        label = "fadeIn"
    )
    this.alpha(alpha)
}

fun Modifier.slideInAnimation(
    visible: Boolean,
    animationSpec: FiniteAnimationSpec<Float> = AnimationSpecs.Medium
): Modifier = composed {
    val slideOffset by animateFloatAsState(
        targetValue = if (visible) 0f else 100f,
        animationSpec = animationSpec,
        label = "slideIn"
    )
    this.translateY(slideOffset.dp)
}

fun Modifier.scaleInAnimation(
    visible: Boolean,
    animationSpec: FiniteAnimationSpec<Float> = AnimationSpecs.SpringBounce
): Modifier = composed {
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = animationSpec,
        label = "scaleIn"
    )
    this.scale(scale)
}

fun Modifier.rotateAnimation(
    rotating: Boolean,
    animationSpec: InfiniteRepeatableSpec<Float> = AnimationSpecs.InfiniteRotation
): Modifier = composed {
    val rotation by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = animationSpec,
        label = "rotate"
    )
    this.rotate(if (rotating) rotation else 0f)
}

fun Modifier.pulseAnimation(
    pulsing: Boolean,
    animationSpec: InfiniteRepeatableSpec<Float> = AnimationSpecs.InfinitePulse
): Modifier = composed {
    val scale by rememberInfiniteTransition().animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = animationSpec,
        label = "pulse"
    )
    this.scale(if (pulsing) scale else 1f)
}

fun Modifier.colorAnimation(
    targetColor: Color,
    animationSpec: FiniteAnimationSpec<Color> = tween(500, easing = EaseInOutCubic)
): Modifier = composed {
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = animationSpec,
        label = "colorAnimation"
    )
    this.drawBehind {
        drawRect(animatedColor)
    }
}

// Enhanced Visibility Animations
@Composable
fun AnimatedVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = AnimationSpecs.Medium) + 
                slideInVertically(animationSpec = AnimationSpecs.Medium) { it / 2 },
        exit = fadeOut(animationSpec = AnimationSpecs.Medium) + 
                slideOutVertically(animationSpec = AnimationSpecs.Medium) { -it / 2 },
        modifier = modifier
    ) {
        content()
    }
}

@Composable
fun ScaleAnimatedVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(animationSpec = AnimationSpecs.SpringBounce) + 
                fadeIn(animationSpec = AnimationSpecs.Medium),
        exit = scaleOut(animationSpec = AnimationSpecs.Medium) + 
                fadeOut(animationSpec = AnimationSpecs.Medium),
        modifier = modifier
    ) {
        content()
    }
}

@Composable
fun SlideAnimatedVisibility(
    visible: Boolean,
    from: AnimationDirection = AnimationDirection.Start,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val slideIn = when (from) {
        AnimationDirection.Start -> slideInHorizontally { -it }
        AnimationDirection.End -> slideInHorizontally { it }
        AnimationDirection.Top -> slideInVertically { -it }
        AnimationDirection.Bottom -> slideInVertically { it }
    }
    
    val slideOut = when (from) {
        AnimationDirection.Start -> slideOutHorizontally { -it }
        AnimationDirection.End -> slideOutHorizontally { it }
        AnimationDirection.Top -> slideOutVertically { -it }
        AnimationDirection.Bottom -> slideOutVertically { it }
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = slideIn + fadeIn(animationSpec = AnimationSpecs.Medium),
        exit = slideOut + fadeOut(animationSpec = AnimationSpecs.Medium),
        modifier = modifier
    ) {
        content()
    }
}

// Enhanced State-based Animations
@Composable
fun <T> AnimatedContentSwitch(
    targetState: T,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit
) {
    androidx.compose.animation.AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            fadeIn(animationSpec = AnimationSpecs.Medium) with 
            fadeOut(animationSpec = AnimationSpecs.Medium)
        },
        modifier = modifier,
        content = content
    )
}

@Composable
fun CrossFadeContent(
    targetState: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable (Boolean) -> Unit
) {
    androidx.compose.animation.Crossfade(
        targetState = targetState,
        animationSpec = AnimationSpecs.Medium,
        modifier = modifier
    ) { visible ->
        content(visible)
    }
}

// Enhanced Loading Animations
@Composable
fun PulsingLoader(
    modifier: Modifier = Modifier,
    color: Color = androidx.compose.material3.MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = AnimationSpecs.InfinitePulse,
        label = "pulsingLoader"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = AnimationSpecs.InfinitePulse,
        label = "pulsingAlpha"
    )
    
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .size(48.dp)
            .scale(scale)
    ) {
        androidx.compose.material3.CircularProgressIndicator(
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 4.dp,
            color = color.copy(alpha = alpha)
        )
    }
}

@Composable
fun RotatingLoader(
    modifier: Modifier = Modifier,
    color: Color = androidx.compose.material3.MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = AnimationSpecs.InfiniteRotation,
        label = "rotatingLoader"
    )
    
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .size(48.dp)
            .rotate(rotation)
    ) {
        androidx.compose.material3.CircularProgressIndicator(
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 4.dp,
            color = color
        )
    }
}

// Enhanced Button Animations
@Composable
fun AnimatedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = AnimationSpecs.SpringBounce,
        label = "buttonScale"
    )
    
    androidx.compose.material3.Button(
        onClick = {
            isPressed = true
            onClick()
            isPressed = false
        },
        modifier = modifier.scale(scale),
        enabled = enabled
    ) {
        content()
    }
}

// Enhanced Card Animations
@Composable
fun AnimatedCard(
    modifier: Modifier = Modifier,
    elevated: Boolean = false,
    content: @Composable () -> Unit
) {
    val elevation by animateDpAsState(
        targetValue = if (elevated) 8.dp else 2.dp,
        animationSpec = AnimationSpecs.Medium,
        label = "cardElevation"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (elevated) 1.02f else 1f,
        animationSpec = AnimationSpecs.SpringBounce,
        label = "cardScale"
    )
    
    androidx.compose.material3.Card(
        modifier = modifier.scale(scale),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        content()
    }
}

// Enhanced List Animations
@Composable
fun <T> AnimatedListItem(
    item: T,
    isVisible: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInHorizontally(animationSpec = AnimationSpecs.Medium) { -it } +
                fadeIn(animationSpec = AnimationSpecs.Medium),
        exit = slideOutHorizontally(animationSpec = AnimationSpecs.Medium) { it } +
                fadeOut(animationSpec = AnimationSpecs.Medium),
        modifier = modifier
    ) {
        content(item)
    }
}

// Animation Direction Enum
enum class AnimationDirection {
    Start,
    End,
    Top,
    Bottom
}

// Enhanced Animation Utilities
object AnimationUtils {
    fun createSpringAnimation(
        dampingRatio: Float = Spring.DampingRatioMediumBouncy,
        stiffness: Float = Spring.StiffnessMedium
    ): SpringSpec<Float> {
        return spring(dampingRatio = dampingRatio, stiffness = stiffness)
    }
    
    fun createTweenAnimation(
        duration: Int = 300,
        easing: Easing = EaseInOutCubic
    ): TweenSpec<Float> {
        return tween(duration, easing = easing)
    }
    
    fun createInfiniteAnimation(
        duration: Int = 1000,
        easing: Easing = LinearEasing
    ): InfiniteRepeatableSpec<Float> {
        return infiniteRepeatable(
            animation = tween(duration, easing = easing),
            repeatMode = RepeatMode.Reverse
        )
    }
}

// Enhanced Animation States
data class AnimationState(
    val isVisible: Boolean = false,
    val isPressed: Boolean = false,
    val isLoading: Boolean = false,
    val isElevated: Boolean = false,
    val scale: Float = 1f,
    val alpha: Float = 1f,
    val rotation: Float = 0f
)

// Enhanced Animation Controller
@Composable
fun rememberAnimationController(
    initialState: AnimationState = AnimationState()
): AnimationController {
    return remember { AnimationController(initialState) }
}

class AnimationController(
    initialState: AnimationState
) {
    var state by mutableStateOf(initialState)
    
    fun show() {
        state = state.copy(isVisible = true)
    }
    
    fun hide() {
        state = state.copy(isVisible = false)
    }
    
    fun press() {
        state = state.copy(isPressed = true)
    }
    
    fun release() {
        state = state.copy(isPressed = false)
    }
    
    fun startLoading() {
        state = state.copy(isLoading = true)
    }
    
    fun stopLoading() {
        state = state.copy(isLoading = false)
    }
    
    fun elevate() {
        state = state.copy(isElevated = true)
    }
    
    fun normalize() {
        state = state.copy(isElevated = false)
    }
    
    fun setScale(scale: Float) {
        state = state.copy(scale = scale)
    }
    
    fun setAlpha(alpha: Float) {
        state = state.copy(alpha = alpha)
    }
    
    fun setRotation(rotation: Float) {
        state = state.copy(rotation = rotation)
    }
}
