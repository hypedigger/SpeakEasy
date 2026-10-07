package com.teacher.app.ui.theme

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

private val Indigo = Color(0xFF3D5A98)
private val IndigoLight = Color(0xFFB6C6EE)
private val Amber = Color(0xFFB4690E)
private val AmberLight = Color(0xFFFFD9A0)

private val LightColors = lightColorScheme(
    primary = Indigo,
    onPrimary = Color.White,
    primaryContainer = IndigoLight,
    onPrimaryContainer = Color(0xFF10203F),
    secondary = Amber,
    onSecondary = Color.White,
    secondaryContainer = AmberLight,
    onSecondaryContainer = Color(0xFF2A1800),
)

private val DarkColors = darkColorScheme(
    primary = IndigoLight,
    onPrimary = Color(0xFF10203F),
    primaryContainer = Color(0xFF294372),
    onPrimaryContainer = Color(0xFFDCE4FA),
    secondary = AmberLight,
    onSecondary = Color(0xFF2A1800),
    secondaryContainer = Color(0xFF7A4700),
    onSecondaryContainer = Color(0xFFFFE6C4),
)

/** Couleur de la boite Leitner, du rouge (fragile) au vert (acquis). */
val boxColors = listOf(
    Color(0xFFC5443B),
    Color(0xFFD97C2B),
    Color(0xFFD9B02B),
    Color(0xFF8FAE2E),
    Color(0xFF4E9A3D),
    Color(0xFF2F7A4F),
)

@Composable
fun TeacherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(colorScheme = colorScheme, content = content)
}
