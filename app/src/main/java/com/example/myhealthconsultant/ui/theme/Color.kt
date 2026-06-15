package com.example.myhealthconsultant.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 专业医疗健康配色方案 - 深青色与白色对比

// 主色调 - 深青绿色系
val Primary = Color(0xFF00695C)      // 深青绿主色
val PrimaryVariant = Color(0xFF004D40) // 深青绿变体
val PrimaryLight = Color(0xFFD0E8E4) // 浅青绿背景
val PrimaryDark = Color(0xFF003D33)  // 深青绿深色变体

// 中性色系 - 清爽白色
val Background = Color(0xFFF8F9FA)   // 浅灰白背景
val Surface = Color(0xFFFFFFFF)      // 纯白表面
val SurfaceVariant = Color(0xFFF1F3F4) // 浅灰表面
val CardBackground = Color(0xFFFFFFFF) // 卡片背景

// 文本色系 - 清晰的层级
val TextPrimary = Color(0xFF1A1A1A)  // 主要文本，近黑色
val TextSecondary = Color(0xFF6B6B6B) // 次要文本，中灰色
val TextHint = Color(0xFF9E9E9E)     // 提示文本，浅灰色
val TextDisabled = Color(0xFFC4C4C4) // 禁用文本

// 功能色 - 低饱和度，古朴典雅
val Success = Color(0xFF2E7D5A)      // 成功色，深青绿
val Warning = Color(0xFFA67C52)      // 警告色，琥珀黄褐/干草色
val WarningContainer = Color(0xFFE6E3D8) // 警告容器色，雾米绿灰/沙绿背景
val Error = Color(0xFF8E3B3B)        // 错误色，陶土红/低饱和红棕
val Info = Color(0xFF5A7A8C)         // 信息色，低饱和蓝灰

// 辅助色
val Divider = Color(0xFFE8E6E3)      // 分割线，非常浅的灰色
val Border = Color(0xFFD4D2CF)       // 边框，浅灰色
val Shadow = Color(0x14000000)       // 阴影，8%黑色

// 保留旧颜色以防兼容性问题（但不再使用）
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// 主题感知的扩展属性，使硬编码颜色名称能跟随深色/浅色主题自动切换
// 用法：MaterialTheme.colorScheme.themedBackground
val ColorScheme.themedBackground: Color get() = background
val ColorScheme.themedSurface: Color get() = surface
val ColorScheme.themedSurfaceVariant: Color get() = surfaceVariant
val ColorScheme.themedCardBackground: Color get() = surface
val ColorScheme.themedTextPrimary: Color get() = onSurface
val ColorScheme.themedTextSecondary: Color get() = onSurfaceVariant
val ColorScheme.themedTextHint: Color get() = onSurfaceVariant.copy(alpha = 0.7f)
val ColorScheme.themedTextDisabled: Color get() = onSurface.copy(alpha = 0.38f)
val ColorScheme.themedPrimaryLight: Color get() = primaryContainer
val ColorScheme.themedDivider: Color get() = outlineVariant
val ColorScheme.themedBorder: Color get() = outline