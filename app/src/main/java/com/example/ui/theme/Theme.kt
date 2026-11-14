package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class DecoSignalColors(
    val signalHigh: Color,
    val signalLow: Color,
    val clockPulse: Color,
    val hit: Color,
    val miss: Color,
    val gridLine: Color,
    val cardElevated: Color
)

val LocalDecoSignalColors = staticCompositionLocalOf {
    DecoSignalColors(
        signalHigh = SignalHighDark,
        signalLow = SignalLowDark,
        clockPulse = ClockDark,
        hit = HitDark,
        miss = MissDark,
        gridLine = DarkOutline,
        cardElevated = DarkSurfaceElevated
    )
}

private val DecoDarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    error = DarkError,
    onError = DarkOnError,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutline.copy(alpha = 0.55f)
)

private val DecoLightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    error = LightError,
    onError = LightOnError,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutline.copy(alpha = 0.65f)
)

val DecoShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun DecoLabTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DecoDarkColorScheme else DecoLightColorScheme
    val signalColors = if (darkTheme) {
        DecoSignalColors(
            signalHigh = SignalHighDark,
            signalLow = SignalLowDark,
            clockPulse = ClockDark,
            hit = HitDark,
            miss = MissDark,
            gridLine = DarkOutline,
            cardElevated = DarkSurfaceElevated
        )
    } else {
        DecoSignalColors(
            signalHigh = SignalHighLight,
            signalLow = SignalLowLight,
            clockPulse = ClockLight,
            hit = HitLight,
            miss = MissLight,
            gridLine = LightOutline,
            cardElevated = LightSurfaceElevated
        )
    }

    CompositionLocalProvider(LocalDecoSignalColors provides signalColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = DecoTypography,
            shapes = DecoShapes,
            content = content
        )
    }
}
