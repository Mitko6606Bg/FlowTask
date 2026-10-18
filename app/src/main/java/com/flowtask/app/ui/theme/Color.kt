package com.flowtask.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ============================================================================
// Dock & Accent Harmonization System
// ============================================================================
/**
 * Color palette configuration for the compact floating dock and unified app actions.
 * Houses mathematically calculated shades of the same hue for outer bar and center button.
 */
data class DockColorPalette(
    val barBackground: Color,
    val centerButtonBackground: Color,
    val iconTint: Color,
    val border: Color
)

/**
 * Converts a Compose [Color] to its Hue, Saturation, Lightness representation.
 * - h: [0f, 360f]
 * - s: [0f, 1f]
 * - l: [0f, 1f]
 */
fun rgbToHsl(color: Color): FloatArray {
    val r = color.red
    val g = color.green
    val b = color.blue
    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val delta = max - min

    var h = 0f
    val l = (max + min) / 2f
    val denom = 1f - kotlin.math.abs(2f * l - 1f)
    val s = if (delta == 0f || denom <= 0.0001f) 0f else (delta / denom).coerceIn(0f, 1f)

    if (delta != 0f) {
        h = when (max) {
            r -> ((g - b) / delta) % 6f
            g -> ((b - r) / delta) + 2f
            else -> ((r - g) / delta) + 4f
        } * 60f
        if (h < 0f) h += 360f
    }
    return floatArrayOf(h, s, l.coerceIn(0f, 1f))
}

/**
 * Converts HSL components back into a Compose [Color].
 */
fun hslToRgb(h: Float, s: Float, l: Float, alpha: Float = 1f): Color {
    val clampedS = s.coerceIn(0f, 1f)
    val clampedL = l.coerceIn(0f, 1f)
    val c = (1f - kotlin.math.abs(2f * clampedL - 1f)) * clampedS
    val normalizedH = ((h % 360f) + 360f) % 360f
    val x = c * (1f - kotlin.math.abs((normalizedH / 60f) % 2f - 1f))
    val m = clampedL - c / 2f

    val (rPrime, gPrime, bPrime) = when {
        normalizedH < 60f -> Triple(c, x, 0f)
        normalizedH < 120f -> Triple(x, c, 0f)
        normalizedH < 180f -> Triple(0f, c, x)
        normalizedH < 240f -> Triple(0f, x, c)
        normalizedH < 300f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }

    return Color(
        red = (rPrime + m).coerceIn(0f, 1f),
        green = (gPrime + m).coerceIn(0f, 1f),
        blue = (bPrime + m).coerceIn(0f, 1f),
        alpha = alpha
    )
}

/**
 * Adjusts Compose Color in HSL color space.
 */
fun Color.adjustHsl(
    saturationMultiplier: Float = 1f,
    lightnessMultiplier: Float = 1f,
    lightnessOffset: Float = 0f
): Color {
    val hsl = rgbToHsl(this)
    val h = hsl[0]
    val s = (hsl[1] * saturationMultiplier).coerceIn(0f, 1f)
    val l = (hsl[2] * lightnessMultiplier + lightnessOffset).coerceIn(0f, 1f)
    return hslToRgb(h, s, l, alpha)
}

/**
 * Dynamic color palette variants extracted from wallpaper.
 */
enum class DynamicColorStyle(val key: String) {
    BALANCED("balanced"),
    VIBRANT("vibrant"),
    MUTED("muted"),
    HIGH_CONTRAST("high_contrast");

    companion object {
        fun fromKey(key: String?): DynamicColorStyle =
            entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: BALANCED
    }
}

val LocalDynamicColorStyle = compositionLocalOf { DynamicColorStyle.BALANCED }

/**
 * Transforms an Android Material You dynamic ColorScheme according to [DynamicColorStyle].
 */
fun transformDynamicScheme(
    base: ColorScheme,
    style: DynamicColorStyle,
    isDark: Boolean
): ColorScheme {
    return when (style) {
        DynamicColorStyle.BALANCED -> base
        DynamicColorStyle.VIBRANT -> {
            val primaryVibrant = base.primary.adjustHsl(
                saturationMultiplier = 1.35f,
                lightnessMultiplier = if (isDark) 1.04f else 0.95f
            )
            val secondaryVibrant = base.secondary.adjustHsl(saturationMultiplier = 1.30f)
            val tertiaryVibrant = base.tertiary.adjustHsl(saturationMultiplier = 1.30f)
            val primaryContainerVibrant = base.primaryContainer.adjustHsl(saturationMultiplier = 1.25f)
            val secondaryContainerVibrant = base.secondaryContainer.adjustHsl(saturationMultiplier = 1.25f)
            val tertiaryContainerVibrant = base.tertiaryContainer.adjustHsl(saturationMultiplier = 1.25f)

            base.copy(
                primary = primaryVibrant,
                secondary = secondaryVibrant,
                tertiary = tertiaryVibrant,
                primaryContainer = primaryContainerVibrant,
                secondaryContainer = secondaryContainerVibrant,
                tertiaryContainer = tertiaryContainerVibrant
            )
        }
        DynamicColorStyle.MUTED -> {
            val primaryMuted = base.primary.adjustHsl(
                saturationMultiplier = 0.52f,
                lightnessMultiplier = if (isDark) 1.02f else 1.04f
            )
            val secondaryMuted = base.secondary.adjustHsl(saturationMultiplier = 0.48f)
            val tertiaryMuted = base.tertiary.adjustHsl(saturationMultiplier = 0.48f)
            val primaryContainerMuted = base.primaryContainer.adjustHsl(saturationMultiplier = 0.45f)
            val secondaryContainerMuted = base.secondaryContainer.adjustHsl(saturationMultiplier = 0.45f)
            val tertiaryContainerMuted = base.tertiaryContainer.adjustHsl(saturationMultiplier = 0.45f)

            base.copy(
                primary = primaryMuted,
                secondary = secondaryMuted,
                tertiary = tertiaryMuted,
                primaryContainer = primaryContainerMuted,
                secondaryContainer = secondaryContainerMuted,
                tertiaryContainer = tertiaryContainerMuted
            )
        }
        DynamicColorStyle.HIGH_CONTRAST -> {
            if (isDark) {
                val primaryContrast = base.primary.adjustHsl(saturationMultiplier = 1.25f, lightnessOffset = 0.06f)
                val bgDeep = base.surfaceContainerLow.adjustHsl(lightnessMultiplier = 0.72f)
                val surfaceLowestDeep = base.surfaceContainerLowest.adjustHsl(lightnessMultiplier = 0.60f)
                val surfaceBright = base.surfaceContainerHighest.adjustHsl(lightnessMultiplier = 1.14f)
                val outlineCrisp = base.outlineVariant.copy(alpha = 0.85f)

                base.copy(
                    primary = primaryContrast,
                    background = bgDeep,
                    surfaceContainerLowest = surfaceLowestDeep,
                    surfaceContainerLow = bgDeep,
                    surfaceContainer = base.surfaceContainer.adjustHsl(lightnessMultiplier = 0.85f),
                    surfaceContainerHigh = base.surfaceContainerHigh.adjustHsl(lightnessMultiplier = 0.96f),
                    surfaceContainerHighest = surfaceBright,
                    outline = outlineCrisp,
                    outlineVariant = outlineCrisp
                )
            } else {
                val primaryContrast = base.primary.adjustHsl(saturationMultiplier = 1.20f, lightnessMultiplier = 0.86f)
                val containerCrisp = base.surfaceContainer.adjustHsl(lightnessMultiplier = 0.94f)
                val outlineCrisp = base.outlineVariant.copy(alpha = 0.90f)

                base.copy(
                    primary = primaryContrast,
                    surfaceContainer = containerCrisp,
                    surfaceContainerHigh = base.surfaceContainerHigh.adjustHsl(lightnessMultiplier = 0.90f),
                    outline = outlineCrisp,
                    outlineVariant = outlineCrisp
                )
            }
        }
    }
}

/**
 * Calculates harmonious shades of the exact same accent color hue for the dock bar
 * and center create button.
 *
 * In dark mode:
 * - Outer bar is a radiant, luminous tone of the accent color.
 * - Center button uses a deeper, contrasting shade of the same hue.
 * - Icons are high-contrast white.
 *
 * In light mode:
 * - Outer bar uses a lush, elegant medium accent tone matching the reference pill dock.
 * - Center button uses a rich, distinct tone of the same hue (never black).
 * - Icons are high-contrast white.
 */
fun calculateDockColors(accent: Color, isDark: Boolean): DockColorPalette {
    val hsl = rgbToHsl(accent)
    val h = hsl[0]
    val origS = hsl[1]

    val barLightness: Float
    val centerLightness: Float
    val barS: Float
    val centerS: Float

    if (isDark) {
        // In dark mode: ensure radiant, luminous color (not drab/dark gray or muddy)
        barLightness = 0.44f
        centerLightness = 0.28f
        barS = if (origS < 0.08f) origS else (origS * 1.05f).coerceIn(0.32f, 0.75f)
        centerS = if (origS < 0.08f) origS else (origS * 1.30f).coerceIn(0.42f, 0.88f)
    } else {
        // In light mode: match reference dock proportions & shades (e.g. moss green outer, forest green center)
        barLightness = 0.40f
        centerLightness = 0.25f
        barS = if (origS < 0.08f) origS else origS.coerceIn(0.28f, 0.70f)
        centerS = if (origS < 0.08f) origS else (origS * 1.25f).coerceIn(0.38f, 0.85f)
    }

    val barBg = hslToRgb(h, barS, barLightness)
    val centerBg = hslToRgb(h, centerS, centerLightness)
    val border = if (isDark) {
        Color.White.copy(alpha = 0.12f)
    } else {
        Color.Transparent
    }

    return DockColorPalette(
        barBackground = barBg,
        centerButtonBackground = centerBg,
        iconTint = Color.White,
        border = border
    )
}

// ============================================================================
// FlowTask Semantic Colors Model
// ============================================================================
data class FlowTaskColors(
    val isDark: Boolean,
    val bg: Color,
    val surface: Color,
    val surfaceContainerLowest: Color,
    val surfaceContainerLow: Color,
    val surfaceContainerLight: Color,
    val surfaceContainerHigh: Color,
    val surfaceContainerHighest: Color,
    val surfaceVariant: Color,
    val outlineSubtle: Color,
    val outlineVariant: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val accentPrimary: Color,
    val textOnAccent: Color = Color.White,
    val checkboxChecked: Color,
    val checkboxUnchecked: Color,
    val checkboxHover: Color,
    val calendarHighlight: Color,
    val calendarTodayHighlight: Color,
    val calendarSelectedBg: Color
)

// ============================================================================
// Color Palettes
// ============================================================================
fun lightFlowTaskColors(effectiveAccent: Color = AccentTeal): FlowTaskColors {
    val dockPalette = calculateDockColors(effectiveAccent, isDark = false)
    val barColor = dockPalette.barBackground
    return FlowTaskColors(
        isDark = false,
        bg = Color(0xFFF8F9FC),
        surface = Color(0xFFFFFFFF),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFF2F4F8),
        surfaceContainerLight = Color(0xFFFFFFFF),
        surfaceContainerHigh = Color(0xFFECEFF5),
        surfaceContainerHighest = Color(0xFFE4E9F0),
        surfaceVariant = Color(0xFFF0F3F7),
        outlineSubtle = Color(0xFFE2E6ED),
        outlineVariant = Color(0xFFEAEEF3),
        textPrimary = Color(0xFF191C20),
        textSecondary = Color(0xFF43474E),
        textMuted = Color(0xFF74777F),
        accentPrimary = barColor,
        textOnAccent = Color.White,
        checkboxChecked = barColor,
        checkboxUnchecked = Color(0xFFCBD5E1),
        checkboxHover = Color(0xFFE0E7EB),
        calendarHighlight = barColor,
        calendarTodayHighlight = barColor,
        calendarSelectedBg = barColor.copy(alpha = 0.18f)
    )
}

fun modernDarkFlowTaskColors(effectiveAccent: Color = AccentTeal, isAmoled: Boolean = false): FlowTaskColors {
    val dockPalette = calculateDockColors(effectiveAccent, isDark = true)
    val barColor = dockPalette.barBackground
    return if (isAmoled) {
        FlowTaskColors(
            isDark = true,
            bg = Color(0xFF000000),
            surface = Color(0xFF14171E),
            surfaceContainerLowest = Color(0xFF000000),
            surfaceContainerLow = Color(0xFF0D0F14),
            surfaceContainerLight = Color(0xFF11141B),
            surfaceContainerHigh = Color(0xFF1C202A),
            surfaceContainerHighest = Color(0xFF262C38),
            surfaceVariant = Color(0xFF14171E),
            outlineSubtle = Color.White.copy(alpha = 0.12f),
            outlineVariant = Color(0xFF2E3544),
            textPrimary = Color(0xFFF8F9FD),
            textSecondary = Color(0xFFB8BFD0),
            textMuted = Color(0xFF80889C),
            accentPrimary = barColor,
            textOnAccent = Color.White,
            checkboxChecked = barColor,
            checkboxUnchecked = Color(0xFF6B7488),
            checkboxHover = Color(0xFF242A36),
            calendarHighlight = barColor,
            calendarTodayHighlight = Color(0xFF60A5FA),
            calendarSelectedBg = barColor.copy(alpha = 0.28f)
        )
    } else {
        FlowTaskColors(
            isDark = true,
            bg = Color(0xFF181B24),
            surface = Color(0xFF2C3242),
            surfaceContainerLowest = Color(0xFF141720),
            surfaceContainerLow = Color(0xFF1E222E),
            surfaceContainerLight = Color(0xFF252B3A),
            surfaceContainerHigh = Color(0xFF333B4E),
            surfaceContainerHighest = Color(0xFF3E475E),
            surfaceVariant = Color(0xFF2E3547),
            outlineSubtle = Color.White.copy(alpha = 0.08f),
            outlineVariant = Color(0xFF424B5E),
            textPrimary = Color(0xFFF8F9FD),
            textSecondary = Color(0xFFC0C6D4),
            textMuted = Color(0xFF8B93A7),
            accentPrimary = barColor,
            textOnAccent = Color.White,
            checkboxChecked = barColor,
            checkboxUnchecked = Color(0xFF6B7488),
            checkboxHover = Color(0xFF2E3544),
            calendarHighlight = barColor,
            calendarTodayHighlight = Color(0xFF60A5FA),
            calendarSelectedBg = barColor.copy(alpha = 0.28f)
        )
    }
}

fun dynamicLightFlowTaskColors(dynamicScheme: ColorScheme): FlowTaskColors {
    val dockPalette = calculateDockColors(dynamicScheme.primary, isDark = false)
    val barColor = dockPalette.barBackground
    return FlowTaskColors(
        isDark = false,
        bg = dynamicScheme.background,
        surface = dynamicScheme.surface,
        surfaceContainerLowest = dynamicScheme.surfaceContainerLowest,
        surfaceContainerLow = dynamicScheme.surfaceContainerLow,
        surfaceContainerLight = dynamicScheme.surfaceContainer,
        surfaceContainerHigh = dynamicScheme.surfaceContainerHigh,
        surfaceContainerHighest = dynamicScheme.surfaceContainerHighest,
        surfaceVariant = dynamicScheme.surfaceVariant,
        outlineSubtle = dynamicScheme.outlineVariant.copy(alpha = 0.7f),
        outlineVariant = dynamicScheme.outlineVariant,
        textPrimary = dynamicScheme.onSurface,
        textSecondary = dynamicScheme.onSurfaceVariant,
        textMuted = dynamicScheme.onSurfaceVariant.copy(alpha = 0.72f),
        accentPrimary = barColor,
        textOnAccent = dynamicScheme.onPrimary,
        checkboxChecked = barColor,
        checkboxUnchecked = dynamicScheme.outlineVariant,
        checkboxHover = dynamicScheme.primaryContainer.copy(alpha = 0.3f),
        calendarHighlight = barColor,
        calendarTodayHighlight = dynamicScheme.tertiary,
        calendarSelectedBg = barColor.copy(alpha = 0.18f)
    )
}

fun dynamicDarkFlowTaskColors(dynamicScheme: ColorScheme, isAmoled: Boolean = false): FlowTaskColors {
    val dockPalette = calculateDockColors(dynamicScheme.primary, isDark = true)
    val barColor = dockPalette.barBackground
    return if (isAmoled) {
        FlowTaskColors(
            isDark = true,
            bg = Color(0xFF000000),
            surface = dynamicScheme.surfaceContainer,
            surfaceContainerLowest = Color(0xFF000000),
            surfaceContainerLow = dynamicScheme.surfaceContainerLowest,
            surfaceContainerLight = dynamicScheme.surfaceContainerLow,
            surfaceContainerHigh = dynamicScheme.surfaceContainerHigh,
            surfaceContainerHighest = dynamicScheme.surfaceContainerHighest,
            surfaceVariant = dynamicScheme.surfaceContainer,
            outlineSubtle = dynamicScheme.outlineVariant.copy(alpha = 0.35f),
            outlineVariant = dynamicScheme.outlineVariant.copy(alpha = 0.50f),
            textPrimary = dynamicScheme.onSurface,
            textSecondary = dynamicScheme.onSurfaceVariant,
            textMuted = dynamicScheme.onSurfaceVariant.copy(alpha = 0.75f),
            accentPrimary = barColor,
            textOnAccent = dynamicScheme.onPrimary,
            checkboxChecked = barColor,
            checkboxUnchecked = dynamicScheme.onSurfaceVariant.copy(alpha = 0.5f),
            checkboxHover = dynamicScheme.primaryContainer.copy(alpha = 0.3f),
            calendarHighlight = barColor,
            calendarTodayHighlight = dynamicScheme.tertiary,
            calendarSelectedBg = barColor.copy(alpha = 0.28f)
        )
    } else {
        FlowTaskColors(
            isDark = true,
            bg = dynamicScheme.surfaceContainerLow,
            surface = dynamicScheme.surfaceContainerHighest,
            surfaceContainerLowest = dynamicScheme.surfaceContainerLow,
            surfaceContainerLow = dynamicScheme.surfaceContainer,
            surfaceContainerLight = dynamicScheme.surfaceContainerHigh,
            surfaceContainerHigh = dynamicScheme.surfaceContainerHighest,
            surfaceContainerHighest = dynamicScheme.surfaceBright,
            surfaceVariant = dynamicScheme.surfaceContainerHighest,
            outlineSubtle = dynamicScheme.outlineVariant.copy(alpha = 0.45f),
            outlineVariant = dynamicScheme.outlineVariant,
            textPrimary = dynamicScheme.onSurface,
            textSecondary = dynamicScheme.onSurfaceVariant,
            textMuted = dynamicScheme.onSurfaceVariant.copy(alpha = 0.75f),
            accentPrimary = barColor,
            textOnAccent = dynamicScheme.onPrimary,
            checkboxChecked = barColor,
            checkboxUnchecked = dynamicScheme.outlineVariant,
            checkboxHover = dynamicScheme.primaryContainer.copy(alpha = 0.35f),
            calendarHighlight = barColor,
            calendarTodayHighlight = dynamicScheme.tertiary,
            calendarSelectedBg = barColor.copy(alpha = 0.28f)
        )
    }
}

// ============================================================================
// Expressive Accent Color Palette Constants
// ============================================================================
val AccentTeal = Color(0xFF1D7868)
val AccentBlue = Color(0xFF215CCD)
val AccentAmber = Color(0xFFB45309)
val AccentRose = Color(0xFFE11D48)
val AccentPurple = Color(0xFF7C3AED)
val TextOnAccent: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalFlowTaskColors.current.textOnAccent

// ============================================================================
// Composition Locals
// ============================================================================
val LocalFlowTaskColors = staticCompositionLocalOf { lightFlowTaskColors() }
val LocalDarkTheme = compositionLocalOf { false }
val LocalAmoledDarkMode = compositionLocalOf { false }
val LocalDockColors = staticCompositionLocalOf { calculateDockColors(AccentTeal, false) }

val DockBarColor: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalDockColors.current.barBackground

// ============================================================================
// Material 3 Expressive Dynamic Semantic Accessors
// ============================================================================
val BgLight: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalFlowTaskColors.current.bg

val SurfaceLight: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalFlowTaskColors.current.surface

val SurfaceContainerLowest: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalFlowTaskColors.current.surfaceContainerLowest

val SurfaceContainerLow: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalFlowTaskColors.current.surfaceContainerLow

val SurfaceContainerLight: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalFlowTaskColors.current.surfaceContainerLight

val SurfaceContainerHigh: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalFlowTaskColors.current.surfaceContainerHigh

val SurfaceContainerHighest: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalFlowTaskColors.current.surfaceContainerHighest

val SurfaceVariantLight: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalFlowTaskColors.current.surfaceVariant

val OutlineSubtle: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalFlowTaskColors.current.outlineSubtle

val OutlineVariant: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalFlowTaskColors.current.outlineVariant

val TextPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalFlowTaskColors.current.textPrimary

val TextSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalFlowTaskColors.current.textSecondary

val TextMuted: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalFlowTaskColors.current.textMuted

val AccentPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalFlowTaskColors.current.accentPrimary

val CheckboxChecked: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalFlowTaskColors.current.checkboxChecked

val CheckboxUnchecked: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalFlowTaskColors.current.checkboxUnchecked

val CheckboxHover: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalFlowTaskColors.current.checkboxHover

val CalendarHighlight: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalFlowTaskColors.current.calendarHighlight

val CalendarTodayHighlight: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalFlowTaskColors.current.calendarTodayHighlight

val CalendarSelectedBg: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalFlowTaskColors.current.calendarSelectedBg

// ============================================================================
// Dynamic M3 Project Color Harmonization
// ============================================================================
/**
 * Harmonizes a raw hex color string into a Material 3 Tonal Palette color.
 * Uses Tone 40 (deep/rich) for Light Mode and Tone 80 (bright/pastel) for Dark Mode.
 */
@Composable
@ReadOnlyComposable
fun getM3TonalProjectColor(hexString: String?): Color {
    if (hexString.isNullOrBlank() || hexString.equals("app", ignoreCase = true) || hexString.equals("dynamic", ignoreCase = true)) {
        return AccentPrimary
    }
    
    val isDark = LocalFlowTaskColors.current.isDark
    val baseColor = try {
        Color(android.graphics.Color.parseColor(hexString))
    } catch (_: Exception) {
        AccentPrimary
    }
    
    val hsl = rgbToHsl(baseColor)
    val h = hsl[0]
    val origS = hsl[1]

    // M3 Expressive saturation tends to be around 0.40 - 0.75
    val s = if (origS < 0.1f) origS else (origS * 0.85f).coerceIn(0.40f, 0.75f)
    
    // Light mode: Tone 40 (approx 0.40 lightness)
    // Dark mode: Tone 80 (approx 0.76 lightness)
    val l = if (isDark) 0.76f else 0.40f

    return hslToRgb(h, s, l)
}
