package com.example.guitartuner.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF4B56A5),
    secondary = Color(0xFF8B4A62),
    background = Color(0xFFF8F9FF),
    surface = Color(0xFFF8F9FF),
)

@Composable
fun GuitarTunerTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightColors, content = content)
}
