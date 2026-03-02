package com.helfkea.crm.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Primary80,
    onPrimary = OnPrimary80,
    primaryContainer = PrimaryContainer80,
    onPrimaryContainer = OnPrimaryContainer80,

    secondary = Gray800,
    onSecondary = Gray200,
    secondaryContainer = Gray700,
    onSecondaryContainer = Gray300,

    tertiary = AccentAmber,
    onTertiary = Black,

    background = Gray900,
    onBackground = Gray200,

    surface = Gray800,
    onSurface = Gray100,
    surfaceVariant = Gray700,
    onSurfaceVariant = Gray300,

    error = Color(0xFFCF6679),
    onError = Black,
    errorContainer = Color(0xFFB00020),
    onErrorContainer = Color(0xFFFFCDD2)
)

private val LightColorScheme = lightColorScheme(
    primary = Primary40,
    onPrimary = OnPrimary40,
    primaryContainer = PrimaryContainer40,
    onPrimaryContainer = OnPrimaryContainer40,

    secondary = Secondary40,
    onSecondary = OnSecondary40,
    secondaryContainer = SecondaryContainer40,
    onSecondaryContainer = OnSecondaryContainer40,

    tertiary = Tertiary40,
    onTertiary = OnTertiary40,

    background = Background40,
    onBackground = OnBackground40,

    surface = Surface40,
    onSurface = OnSurface40,
    surfaceVariant = SurfaceVariant40,
    onSurfaceVariant = OnSurfaceVariant40,

    error = Error40,
    onError = OnError40,
    errorContainer = ErrorContainer40,
    onErrorContainer = OnErrorContainer40,

    outline = Outline40,
    outlineVariant = OutlineVariant40
)

@Composable
fun HelfkeaCRMTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}