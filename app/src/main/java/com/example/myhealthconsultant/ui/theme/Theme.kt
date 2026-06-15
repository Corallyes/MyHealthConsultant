package com.example.myhealthconsultant.ui.theme

import android.app.Activity
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

// 深色主题配色方案
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4DB6AC), // 浅青色主色，深色模式下更亮
    onPrimary = Color(0xFF003731),
    primaryContainer = Color(0xFF004D40), // 深色容器
    onPrimaryContainer = Color(0xFF70F0DE),

    secondary = Color(0xFFB0BEC5),
    onSecondary = Color(0xFF1C313A),
    secondaryContainer = Color(0xFF37474F),
    onSecondaryContainer = Color(0xFFCFD8DC),

    background = Color(0xFF121212), // Material 深色背景
    onBackground = Color(0xFFE0E0E0),

    surface = Color(0xFF1E1E1E), // 深色表面
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFB0B0B0),

    error = Color(0xFFEF5350),
    onError = Color.White,

    outline = Color(0xFF444444),
    outlineVariant = Color(0xFF333333)
)

// 浅色主题配色方案
private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = TextPrimary,
    
    secondary = TextSecondary,
    onSecondary = Color.White,
    secondaryContainer = SurfaceVariant,
    onSecondaryContainer = TextPrimary,
    
    background = Background,
    onBackground = TextPrimary,
    
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,
    
    error = Error,
    onError = Color.White,
    
    outline = Border,
    outlineVariant = Divider
)

@Composable
fun MyHealthConsultantTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // 如果需要动态颜色且Android版本支持，可以启用
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