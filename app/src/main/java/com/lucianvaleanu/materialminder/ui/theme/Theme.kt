package com.lucianvaleanu.materialminder.ui.theme


import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Gray700,
    onPrimary = Color.White,
    secondary = Gray500,
    onSecondary = Color.White,
    background = Gray100,
    onBackground = Gray900,
    surface = Gray200,
    onSurface = Gray900
)

@Composable
fun MaterialMinderTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}