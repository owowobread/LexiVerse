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
import com.example.domain.model.ThemeSetting

private val LightColorScheme = lightColorScheme(
    primary = NaturalGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = NaturalSagePrimaryContainer,
    onPrimaryContainer = NaturalOnPrimaryContainer,
    secondary = NaturalOliveSecondary,
    onSecondary = Color.White,
    secondaryContainer = NaturalSecondaryContainer,
    onSecondaryContainer = NaturalOnSecondaryContainer,
    tertiary = NaturalWarmOchreTertiary,
    onTertiary = Color.White,
    tertiaryContainer = NaturalTertiaryContainer,
    onTertiaryContainer = NaturalOnTertiaryContainer,
    background = NaturalLightBackground,
    onBackground = NaturalLightOnBackground,
    surface = NaturalLightSurface,
    onSurface = NaturalLightOnSurface,
    surfaceVariant = NaturalLightSurfaceVariant,
    onSurfaceVariant = NaturalLightOnSurfaceVariant,
    outline = NaturalLightOutline,
    outlineVariant = NaturalLightOutlineVariant
)

private val DarkColorScheme = darkColorScheme(
    primary = NaturalGreenLight,
    onPrimary = Color(0xFF0F2012),
    primaryContainer = NaturalGreenDark,
    onPrimaryContainer = NaturalSagePrimaryContainer,
    secondary = NaturalOliveLight,
    onSecondary = Color(0xFF132014),
    secondaryContainer = Color(0xFF2E3D2F),
    onSecondaryContainer = NaturalSecondaryContainer,
    tertiary = NaturalWarmOchreGlow,
    onTertiary = Color(0xFF332000),
    tertiaryContainer = Color(0xFF573E14),
    onTertiaryContainer = NaturalTertiaryContainer,
    background = NaturalDarkBackground,
    onBackground = NaturalDarkOnBackground,
    surface = NaturalDarkSurface,
    onSurface = NaturalDarkOnSurface,
    surfaceVariant = NaturalDarkSurfaceVariant,
    onSurfaceVariant = NaturalDarkOnSurfaceVariant,
    outline = NaturalDarkOutline
)

private val AmoledColorScheme = darkColorScheme(
    primary = NaturalGreenLight,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF18301B),
    onPrimaryContainer = NaturalSagePrimaryContainer,
    secondary = NaturalOliveLight,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF223023),
    onSecondaryContainer = NaturalSecondaryContainer,
    tertiary = NaturalWarmOchreGlow,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF45300B),
    onTertiaryContainer = NaturalTertiaryContainer,
    background = NaturalAmoledBackground,
    onBackground = NaturalAmoledOnBackground,
    surface = NaturalAmoledSurface,
    onSurface = NaturalAmoledOnSurface,
    surfaceVariant = NaturalAmoledSurfaceVariant,
    onSurfaceVariant = NaturalAmoledOnSurfaceVariant,
    outline = NaturalAmoledOutline
)

@Composable
fun LexiVerseTheme(
    themeSetting: ThemeSetting = ThemeSetting.SYSTEM,
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
        typography = Typography,
        content = content
    )
}
