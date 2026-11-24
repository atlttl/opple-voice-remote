package com.opple.voiceremote

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

/**
 * 启动图标切换：两套图标对应同一个 Activity 的两个 activity-alias，
 * 切换只改启用状态，不重装、不重启进程。
 */
object LauncherIcon {

    // Apple 图标（暖光玻璃）；别名名沿用初版，改名会让升级用户的桌面入口丢失
    private const val APPLE = ".IconWarm"
    // Android 图标（蓝调），现在作为默认
    private const val ANDROID = ".IconLine"

    /** debug 包名带 .dev 后缀，组件名必须按实际包名拼接 */
    private fun component(context: Context, suffix: String) =
        ComponentName(context.packageName, context.packageName + suffix)

    fun apply(context: Context, style: Int) {
        val pm = context.packageManager
        val target = if (style == 1) APPLE else ANDROID
        val other = if (style == 1) ANDROID else APPLE

        // 先启用目标再禁用另一个，避免桌面出现短暂无图标
        pm.setComponentEnabledSetting(
            component(context, target),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP,
        )
        pm.setComponentEnabledSetting(
            component(context, other),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP,
        )
    }

    /** 覆盖安装后系统可能把别名状态重置回清单默认值，启动时对齐一次。 */
    fun syncIfNeeded(context: Context, style: Int) {
        val pm = context.packageManager
        val target = if (style == 1) APPLE else ANDROID
        val other = if (style == 1) ANDROID else APPLE
        val targetState = pm.getComponentEnabledSetting(component(context, target))
        val otherState = pm.getComponentEnabledSetting(component(context, other))
        // 两个都要正确，否则可能出现两个图标或没有图标
        if (targetState != PackageManager.COMPONENT_ENABLED_STATE_ENABLED ||
            otherState != PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        ) {
            apply(context, style)
        }
    }
}
