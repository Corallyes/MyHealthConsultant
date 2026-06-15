package com.example.myhealthconsultant.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 导航目的地枚举
 */
enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
    val route: String
) {
    AI_CHAT("AI问答", Icons.Default.SmartToy, "ai_chat"),
    CAMERA("拍照识药", Icons.Default.CameraAlt, "camera"),
    DRUGS("药品库", Icons.Default.Medication, "drugs"),
    CALENDAR("我的", Icons.Default.CalendarMonth, "calendar");

    companion object {
        fun fromRoute(route: String?): AppDestinations {
            return entries.find { it.route == route } ?: AI_CHAT
        }
    }
}

/**
 * 侧边栏菜单项
 */
enum class DrawerDestinations(
    val label: String,
    val icon: ImageVector,
    val route: String
) {
    PROFILE("个人资料", Icons.Default.Person, "profile"),
    ACCOUNT("账号管理", Icons.Default.ManageAccounts, "account"),
    THEME("外观设置", Icons.Default.Palette, "theme"),
    NOTIFICATION("通知设置", Icons.Default.Notifications, "notification"),
    ABOUT("关于APP", Icons.Default.Info, "about"),
    PRIVACY("隐私政策", Icons.Default.PrivacyTip, "privacy"),
    LOGOUT("退出登录", Icons.Default.Logout, "logout")
}
