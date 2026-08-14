package it.vigilfuoco.tlcfield.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TLCColors = lightColorScheme(
    primary = Color(0xFFB71C1C),
    onPrimary = Color.White,
    secondary = Color(0xFF37474F),
    background = Color(0xFFF7F7F7),
    surface = Color.White
)

@Composable
fun TLCFieldTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TLCColors,
        content = content
    )
}
