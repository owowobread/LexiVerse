package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import com.example.domain.model.ThemeSetting

private val LightColorScheme = lightColorScheme(
    primary = BluePurplePrimary,
    onPrimary = Color.White,
    primaryContainer = BluePurplePrimaryContainer,
    onPrimaryContainer = BluePurpleOnPrimaryContainer,
    secondary = PurplishSecondary,
    onSecondary = Color.White,
    secondaryContainer = PurplishSecondaryContainer,
    onSecondaryContainer = PurplishOnSecondaryContainer,
    tertiary = IndigoTertiary,
    onTertiary = Color.White,
    tertiaryContainer = IndigoTertiaryContainer,
    onTertiaryContainer = IndigoOnTertiaryContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant
)

private val DarkColorScheme = darkColorScheme(
    primary = BluePurpleLight,
    onPrimary = Color(0xFF0A0738),
    primaryContainer = BluePurpleDark,
    onPrimaryContainer = BluePurplePrimaryContainer,
    secondary = PurplishSecondaryLight,
    onSecondary = Color(0xFF230738),
    secondaryContainer = Color(0xFF3F1366),
    onSecondaryContainer = PurplishSecondaryContainer,
    tertiary = IndigoTertiaryGlow,
    onTertiary = Color(0xFF07194D),
    tertiaryContainer = Color(0xFF1E3A8A),
    onTertiaryContainer = IndigoTertiaryContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline
)

private val AmoledColorScheme = darkColorScheme(
    primary = BluePurpleLight,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF120D54),
    onPrimaryContainer = BluePurplePrimaryContainer,
    secondary = PurplishSecondaryLight,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF2F0B4F),
    onSecondaryContainer = PurplishSecondaryContainer,
    tertiary = IndigoTertiaryGlow,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF102159),
    onTertiaryContainer = IndigoTertiaryContainer,
    background = AmoledBackground,
    onBackground = AmoledOnBackground,
    surface = AmoledSurface,
    onSurface = AmoledOnSurface,
    surfaceVariant = AmoledSurfaceVariant,
    onSurfaceVariant = AmoledOnSurfaceVariant,
    outline = AmoledOutline
)

@Composable
fun LexiVerseTheme(
    themeSetting: ThemeSetting = ThemeSetting.SYSTEM,
    fontFamily: FontFamily = FontFamily.Default,
    fontScale: Float = 1.0f,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeSetting) {
        ThemeSetting.SYSTEM -> isSystemDark
        ThemeSetting.LIGHT -> false
        ThemeSetting.DARK, ThemeSetting.AMOLED -> true
    }

    val context = LocalContext.current
    val colorScheme = when {
        themeSetting == ThemeSetting.AMOLED -> AmoledColorScheme
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = getCustomTypography(fontFamily, fontScale),
        content = content
    )
}
