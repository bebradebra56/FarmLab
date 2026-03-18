package com.farmlab.labfarmis.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = FarmGreen,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = FarmGreenContainer,
    onPrimaryContainer = FarmGreenDark,
    secondary = FarmYellow,
    onSecondary = FarmBrown,
    secondaryContainer = FarmYellowContainer,
    onSecondaryContainer = FarmBrown,
    tertiary = FarmBrown,
    onTertiary = androidx.compose.ui.graphics.Color.White,
    tertiaryContainer = FarmBrownContainer,
    onTertiaryContainer = FarmGreenDark,
    background = FarmCream,
    onBackground = FarmOnSurface,
    surface = FarmSurface,
    onSurface = FarmOnSurface,
    surfaceVariant = FarmCreamDark,
    onSurfaceVariant = FarmBrown,
    outline = FarmOutline,
    error = FarmRed,
    onError = androidx.compose.ui.graphics.Color.White,
    errorContainer = FarmRedContainer,
    onErrorContainer = FarmRed
)

private val DarkColorScheme = darkColorScheme(
    primary = FarmGreenDarkTheme,
    onPrimary = FarmGreenDark,
    primaryContainer = FarmGreenDark,
    onPrimaryContainer = FarmGreenContainer,
    secondary = FarmYellowLight,
    onSecondary = FarmBrown,
    secondaryContainer = FarmBrown,
    onSecondaryContainer = FarmYellowContainer,
    tertiary = FarmBrownLight,
    onTertiary = FarmGreenDark,
    tertiaryContainer = FarmBrown,
    onTertiaryContainer = FarmBrownContainer,
    background = FarmSurfaceDark,
    onBackground = FarmOnSurfaceDark,
    surface = FarmSurfaceDark,
    onSurface = FarmOnSurfaceDark,
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFF2A2A22),
    onSurfaceVariant = FarmCreamDark
)

@Composable
fun FarmLabTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
