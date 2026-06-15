package com.example.myhealthconsultant.ui.animations

import androidx.compose.animation.*
import androidx.compose.animation.core.tween

/**
 * 页面转场动画系统
 * 提供克制但存在的动效，避免夸张动画
 */
object HealthTransitions {
    
    /**
     * 淡入 + 轻微位移（从右侧）
     * 用于页面进入
     */
    fun fadeInSlideFromRight(): EnterTransition {
        return fadeIn(
            animationSpec = tween(200)
        ) + slideInHorizontally(
            initialOffsetX = { 16 },
            animationSpec = tween(200)
        )
    }
    
    /**
     * 淡出 + 轻微位移（向左侧）
     * 用于页面退出
     */
    fun fadeOutSlideToLeft(): ExitTransition {
        return fadeOut(
            animationSpec = tween(150)
        ) + slideOutHorizontally(
            targetOffsetX = { -16 },
            animationSpec = tween(150)
        )
    }
    
    /**
     * 从底部滑入
     * 用于模态页面、结果卡片
     */
    fun slideUpFromBottom(): EnterTransition {
        return slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(300)
        )
    }
    
    /**
     * 向底部滑出
     * 用于模态页面关闭
     */
    fun slideDownToBottom(): ExitTransition {
        return slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(250)
        )
    }
    
    /**
     * 淡入
     * 用于简单的内容切换
     */
    fun simpleFadeIn(): EnterTransition {
        return fadeIn(
            animationSpec = tween(200)
        )
    }
    
    /**
     * 淡出
     * 用于简单的内容切换
     */
    fun simpleFadeOut(): ExitTransition {
        return fadeOut(
            animationSpec = tween(200)
        )
    }
    
    /**
     * 轻微缩放 + 淡入
     * 用于卡片、对话框出现
     */
    fun scaleInFade(): EnterTransition {
        return fadeIn(
            animationSpec = tween(200)
        ) + scaleIn(
            initialScale = 0.95f,
            animationSpec = tween(200)
        )
    }
    
    /**
     * 轻微缩放 + 淡出
     * 用于卡片、对话框消失
     */
    fun scaleOutFade(): ExitTransition {
        return fadeOut(
            animationSpec = tween(150)
        ) + scaleOut(
            targetScale = 0.95f,
            animationSpec = tween(150)
        )
    }
}

/**
 * 按钮交互动效
 */
object ButtonAnimations {
    
    /**
     * 按钮按下时的缩放比例
     */
    const val PRESSED_SCALE = 0.96f
    
    /**
     * 按钮正常时的缩放比例
     */
    const val NORMAL_SCALE = 1.0f
    
    /**
     * 缩放动画持续时间（毫秒）
     */
    const val SCALE_DURATION = 150
}

/**
 * 列表加载动效
 */
object ListAnimations {
    
    /**
     * 骨架屏动画持续时间（毫秒）
     */
    const val SKELETON_DURATION = 1000
    
    /**
     * 列表项出现动画延迟（毫秒）
     */
    const val ITEM_DELAY = 50
}