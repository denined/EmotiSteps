package com.example.emotiontherapy.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = CalmTeal,
    onPrimary = IvorySurface,
    primaryContainer = CalmTealContainer,
    onPrimaryContainer = DeepTeal,
    secondary = DustyBlue,
    onSecondary = IvorySurface,
    secondaryContainer = DustyBlueContainer,
    onSecondaryContainer = DeepBlue,
    tertiary = SoftApricot,
    onTertiary = DeepApricot,
    tertiaryContainer = SoftApricotContainer,
    onTertiaryContainer = DeepApricot,
    background = CreamBackground,
    onBackground = SlateText,
    surface = IvorySurface,
    onSurface = SlateText,
    surfaceVariant = SandSurfaceVariant,
    onSurfaceVariant = MutedSlateText,
    outline = WarmOutline,
    error = SoftError,
    onError = IvorySurface,
    errorContainer = SoftErrorContainer,
    onErrorContainer = SlateText
)

private val DarkColorScheme = darkColorScheme(
    primary = CalmTeal,
    onPrimary = DeepTeal,
    primaryContainer = DeepTeal,
    onPrimaryContainer = CalmTealContainer,
    secondary = DustyBlue,
    onSecondary = DeepBlue,
    secondaryContainer = DeepBlue,
    onSecondaryContainer = DustyBlueContainer,
    tertiary = SoftApricot,
    onTertiary = DeepApricot,
    tertiaryContainer = DeepApricot,
    onTertiaryContainer = SoftApricotContainer,
    background = DarkBackground,
    onBackground = IvorySurface,
    surface = DarkSurface,
    onSurface = IvorySurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = SandSurfaceVariant,
    outline = DarkOutline,
    error = SoftError,
    onError = IvorySurface,
    errorContainer = SoftErrorContainer,
    onErrorContainer = SlateText
)

@Composable
fun EmotionTherapyAppTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
