package com.flowtask.app.ui.theme

import android.app.Activity
import android.content.ContextWrapper
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.flowtask.app.util.BulgarianStrings
import com.flowtask.app.util.EnglishStrings
import com.flowtask.app.util.LocalAppStrings

val LocalAccentColor = compositionLocalOf { AccentTeal }

// ============================================================================
// Material 3 Expressive Shapes (Bolder, softer, friendlier corner scales)
// ============================================================================
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

// ============================================================================
// Material 3 Expressive Typography (Assertive, high contrast, tight tracking)
// ============================================================================
fun createAppTypography(
    textPrimary: Color = Color(0xFF191C20),
    textSecondary: Color = Color(0xFF43474E),
    textMuted: Color = Color(0xFF74777F)
): Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Black,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.8).sp,
        color = textPrimary
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.5).sp,
        color = textPrimary
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.3).sp,
        color = textPrimary
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.2).sp,
        color = textPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        color = textPrimary
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = textPrimary
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        color = textPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = textSecondary
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = textSecondary
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        color = textPrimary
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.2.sp,
        color = textSecondary
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.4.sp,
        color = textMuted
    )
)

val AppTypography = createAppTypography()

// ============================================================================
// Material 3 Expressive Motion Tokens & Tactile Interactions
// ============================================================================
object ExpressiveMotion {
    val SpringBouncy = spring<Float>(dampingRatio = 0.65f, stiffness = 450f)
    val SpringSnappy = spring<Float>(dampingRatio = 0.8f, stiffness = 550f)
    val SpringGentle = spring<Float>(dampingRatio = 0.9f, stiffness = 300f)
    val TactileSpring = spring<Float>(dampingRatio = 0.75f, stiffness = 500f)
}

/**
 * Material 3 Expressive & Emil Kowalski tactile touch feedback:
 * Smoothly compresses by [pressScale] (default 3%) when pressed, then springs back with physical bounce.
 * Supports both explicit [interactionSource] binding and zero-config pointerInput touch observation.
 */
@Composable
fun Modifier.expressiveBounce(
    interactionSource: MutableInteractionSource? = null,
    pressScale: Float = 0.97f
): Modifier {
    var isPressedLocal by remember { mutableStateOf(false) }
    val isPressedFromSource = interactionSource?.collectIsPressedAsState()?.value ?: false
    val isPressed = isPressedLocal || isPressedFromSource

    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressScale else 1f,
        animationSpec = spring(
            dampingRatio = 0.75f,
            stiffness = 450f
        ),
        label = "expressiveBounce"
    )

    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .then(
            if (interactionSource == null) {
                Modifier.pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        isPressedLocal = true
                        waitForUpOrCancellation()
                        isPressedLocal = false
                    }
                }
            } else {
                Modifier
            }
        )
}

// ============================================================================
// FlowTask Theme Configuration
// ============================================================================
@Composable
fun FlowTaskTheme(
    language: String = "bg",
    accentColorHex: String = "#2A8575",
    themeMode: String = "system",
    amoledDarkMode: Boolean = false,
    dynamicColorStyle: DynamicColorStyle = DynamicColorStyle.BALANCED,
    updateWindowBackground: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemInDark = isSystemInDarkTheme()

    val useDarkTheme = when (themeMode.lowercase()) {
        "dark" -> true
        "light" -> false
        else -> systemInDark
    }

    val isDynamic = accentColorHex == "DYNAMIC" && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val dynamicScheme = if (isDynamic) {
        val rawDynScheme = if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        transformDynamicScheme(rawDynScheme, dynamicColorStyle, useDarkTheme)
    } else null

    val effectiveAccent = if (isDynamic && dynamicScheme != null) {
        dynamicScheme.primary
    } else {
        try {
            Color(android.graphics.Color.parseColor(accentColorHex))
        } catch (_: Exception) {
            AccentTeal
        }
    }

    val effectiveFlowColors = when {
        isDynamic && dynamicScheme != null && useDarkTheme -> dynamicDarkFlowTaskColors(dynamicScheme, isAmoled = amoledDarkMode)
        isDynamic && dynamicScheme != null && !useDarkTheme -> dynamicLightFlowTaskColors(dynamicScheme)
        useDarkTheme -> modernDarkFlowTaskColors(effectiveAccent, isAmoled = amoledDarkMode)
        else -> lightFlowTaskColors(effectiveAccent)
    }

    val dockPalette = remember(effectiveAccent, useDarkTheme) {
        calculateDockColors(effectiveAccent, useDarkTheme)
    }

    val colorScheme = when {
        isDynamic && dynamicScheme != null && useDarkTheme -> {
            dynamicScheme.copy(
                background = effectiveFlowColors.bg,
                surface = effectiveFlowColors.surface,
                surfaceVariant = effectiveFlowColors.surfaceVariant,
                surfaceContainerLowest = effectiveFlowColors.surfaceContainerLowest,
                surfaceContainerLow = effectiveFlowColors.surfaceContainerLow,
                surfaceContainer = effectiveFlowColors.surfaceContainerLight,
                surfaceContainerHigh = effectiveFlowColors.surfaceContainerHigh,
                surfaceContainerHighest = effectiveFlowColors.surfaceContainerHighest,
                outline = effectiveFlowColors.outlineSubtle,
                outlineVariant = effectiveFlowColors.outlineVariant
            )
        }
        isDynamic && dynamicScheme != null && !useDarkTheme -> {
            dynamicScheme.copy(
                background = effectiveFlowColors.bg,
                surface = effectiveFlowColors.surface,
                surfaceVariant = effectiveFlowColors.surfaceVariant,
                surfaceContainerLowest = effectiveFlowColors.surfaceContainerLowest,
                surfaceContainerLow = effectiveFlowColors.surfaceContainerLow,
                surfaceContainer = effectiveFlowColors.surfaceContainerLight,
                surfaceContainerHigh = effectiveFlowColors.surfaceContainerHigh,
                surfaceContainerHighest = effectiveFlowColors.surfaceContainerHighest,
                outline = effectiveFlowColors.outlineSubtle,
                outlineVariant = effectiveFlowColors.outlineVariant
            )
        }
        useDarkTheme -> {
            val barColor = effectiveFlowColors.accentPrimary
            darkColorScheme(
                primary = barColor,
                onPrimary = Color.White,
                primaryContainer = barColor.copy(alpha = 0.22f),
                onPrimaryContainer = Color.White,
                secondary = barColor,
                onSecondary = Color.White,
                secondaryContainer = barColor.copy(alpha = 0.16f),
                onSecondaryContainer = Color.White,
                background = effectiveFlowColors.bg,
                onBackground = effectiveFlowColors.textPrimary,
                surface = effectiveFlowColors.surface,
                onSurface = effectiveFlowColors.textPrimary,
                surfaceVariant = effectiveFlowColors.surfaceVariant,
                onSurfaceVariant = effectiveFlowColors.textSecondary,
                surfaceContainerLowest = effectiveFlowColors.surfaceContainerLowest,
                surfaceContainerLow = effectiveFlowColors.surfaceContainerLow,
                surfaceContainer = effectiveFlowColors.surfaceContainerLight,
                surfaceContainerHigh = effectiveFlowColors.surfaceContainerHigh,
                surfaceContainerHighest = effectiveFlowColors.surfaceContainerHighest,
                outline = effectiveFlowColors.outlineSubtle,
                outlineVariant = effectiveFlowColors.outlineVariant
            )
        }
        else -> {
            val barColor = effectiveFlowColors.accentPrimary
            lightColorScheme(
                primary = barColor,
                onPrimary = Color.White,
                primaryContainer = barColor.copy(alpha = 0.14f),
                onPrimaryContainer = barColor,
                secondary = barColor,
                onSecondary = Color.White,
                secondaryContainer = barColor.copy(alpha = 0.10f),
                onSecondaryContainer = barColor,
                background = effectiveFlowColors.bg,
                onBackground = effectiveFlowColors.textPrimary,
                surface = effectiveFlowColors.surface,
                onSurface = effectiveFlowColors.textPrimary,
                surfaceVariant = effectiveFlowColors.surfaceVariant,
                onSurfaceVariant = effectiveFlowColors.textSecondary,
                surfaceContainerLowest = effectiveFlowColors.surfaceContainerLowest,
                surfaceContainerLow = effectiveFlowColors.surfaceContainerLow,
                surfaceContainer = effectiveFlowColors.surfaceContainerLight,
                surfaceContainerHigh = effectiveFlowColors.surfaceContainerHigh,
                surfaceContainerHighest = effectiveFlowColors.surfaceContainerHighest,
                outline = effectiveFlowColors.outlineSubtle,
                outlineVariant = effectiveFlowColors.outlineVariant
            )
        }
    }

    val typography = remember(effectiveFlowColors) {
        createAppTypography(
            textPrimary = effectiveFlowColors.textPrimary,
            textSecondary = effectiveFlowColors.textSecondary,
            textMuted = effectiveFlowColors.textMuted
        )
    }

    val strings = if (language.lowercase() == "en") EnglishStrings else BulgarianStrings

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = generateSequence(view.context) { (it as? ContextWrapper)?.baseContext }
                .filterIsInstance<Activity>()
                .firstOrNull()
            activity?.window?.let { window ->
                if (updateWindowBackground) {
                    window.setBackgroundDrawable(ColorDrawable(effectiveFlowColors.bg.toArgb()))
                }
                window.statusBarColor = android.graphics.Color.TRANSPARENT
                window.navigationBarColor = android.graphics.Color.TRANSPARENT
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !useDarkTheme
                    isAppearanceLightNavigationBars = !useDarkTheme
                }
            }
        }
    }

    CompositionLocalProvider(
        LocalAppStrings provides strings,
        LocalAccentColor provides effectiveAccent,
        LocalDockColors provides dockPalette,
        LocalFlowTaskColors provides effectiveFlowColors,
        LocalDarkTheme provides useDarkTheme,
        LocalAmoledDarkMode provides amoledDarkMode,
        LocalDynamicColorStyle provides dynamicColorStyle
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = AppShapes,
            content = content
        )
    }
}
