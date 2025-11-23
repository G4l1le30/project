package com.example.umkami.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

// New Color Schemes based on the "Food Delivery" design brief
private val DarkColorScheme = darkColorScheme(
    primary = WarmOrange,
    onPrimary = Color.White,
    secondary = PlayfulPink,
    onSecondary = Color.White,
    tertiary = AccentYellow,
    onTertiary = TextBlack,
    background = Color(0xFF121212), // Standard dark theme background
    surface = Color(0xFF1E1E1E),    // Slightly lighter surface for cards
    onBackground = AppBackground,
    onSurface = AppBackground,
    surfaceVariant = Color(0xFF333333),
    onSurfaceVariant = TextGrey
)

private val LightColorScheme = lightColorScheme(
    primary = WarmOrange,
    onPrimary = Color.White,
    secondary = PlayfulPink,
    onSecondary = Color.White,
    tertiary = AccentYellow,
    onTertiary = TextBlack,
    background = AppBackground,
    onBackground = TextBlack,
    surface = AppBackground,
    onSurface = TextBlack,
    surfaceVariant = SurfaceGrey,
    onSurfaceVariant = TextBlack
)

// Shapes based on the design brief (rounded corners)
val AppShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp)
)

@Composable
fun UmkamiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    // Disabled to enforce the custom theme
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
        shapes = AppShapes, // Apply custom shapes
        content = content
    )
}
