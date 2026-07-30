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

private val DarkColorScheme =
  darkColorScheme(
    primary = SakuraPink,
    secondary = SoftPurple,
    tertiary = WarmPeach,
    background = DarkPurple,
    surface = DarkPurple,
    onPrimary = DarkPurple,
    onSecondary = Color.White,
    onTertiary = DarkPurple,
    onBackground = Color.White,
    onSurface = Color.White
  )

private val LightColorScheme =
  lightColorScheme(
    primary = SakuraPink,
    secondary = SoftPurple,
    tertiary = WarmPeach,
    background = Color(0xFFFFF0F5),
    surface = Color(0xFFFFF0F5),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark theme to match prompt requirement
  dynamicColor: Boolean = false, // Disable dynamic color to enforce our custom theme
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
