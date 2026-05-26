package com.example.ui.theme

import android.os.Build
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = MinimalistTertiary,
    onPrimary = MinimalistPrimary,
    secondary = MinimalistWhite,
    onSecondary = MinimalistPrimary,
    tertiary = MinimalistSecondary,
    onTertiary = MinimalistWhite,
    background = MinimalistPrimary,
    onBackground = MinimalistBg,
    surface = Color(0xFF2B1C19),
    onSurface = MinimalistBg,
    outline = MinimalistMutedText
  )

private val LightColorScheme =
  lightColorScheme(
    primary = MinimalistPrimary,
    onPrimary = MinimalistWhite,
    secondary = MinimalistPrimary,
    onSecondary = MinimalistBg,
    tertiary = MinimalistTertiary,
    onTertiary = MinimalistPrimary,
    background = MinimalistBg,
    onBackground = MinimalistText,
    surface = MinimalistWhite,
    onSurface = MinimalistText,
    outline = MinimalistBorder
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Force clean minimalist theme styling
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
