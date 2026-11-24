package com.opple.voiceremote.data

import android.content.Context
import android.content.SharedPreferences

class Prefs(context: Context) {

    private val sp: SharedPreferences =
        context.applicationContext.getSharedPreferences("opple_voice_remote", Context.MODE_PRIVATE)

    /**
     * 主题：0 = 跟随系统（默认），1 = 深色，2 = 浅色。
     * 老版本只存过 dark_theme 布尔值，这里做一次映射，升级用户的选择不会丢。
     */
    var themeMode: Int
        get() {
            if (sp.contains("theme_mode")) return sp.getInt("theme_mode", 0).coerceIn(0, 2)
            if (sp.contains("dark_theme")) return if (sp.getBoolean("dark_theme", true)) 1 else 2
            return 0
        }
        set(value) = sp.edit().putInt("theme_mode", value.coerceIn(0, 2)).apply()

    /** 朗读语速，默认略慢于自然语速，识别率更高 */
    var rate: Float
        get() = sp.getFloat("rate", 0.95f)
        set(value) = sp.edit().putFloat("rate", value).apply()

    /** 唤醒词与命令之间的停顿（毫秒） */
    var gapMs: Int
        get() = sp.getInt("gap_ms", 700)
        set(value) = sp.edit().putInt("gap_ms", value).apply()

    /** 命令重复次数 */
    var repeatTimes: Int
        get() = sp.getInt("repeat", 1)
        set(value) = sp.edit().putInt("repeat", value).apply()

    /** 启动图标：0 = Android（蓝调，默认），1 = Apple（暖光） */
    var iconStyle: Int
        get() = sp.getInt("icon_style", 0).coerceIn(0, 1)
        set(value) = sp.edit().putInt("icon_style", value.coerceIn(0, 1)).apply()

    /** 播放前自动提升媒体音量，播完还原 */
    var autoBoostVolume: Boolean
        get() = sp.getBoolean("auto_max_volume", true)
        set(value) = sp.edit().putBoolean("auto_max_volume", value).apply()

    /** 自动提升的目标音量（占媒体音量的百分比） */
    var boostVolumePercent: Int
        get() = sp.getInt("boost_percent", 100).coerceIn(30, 100)
        set(value) = sp.edit().putInt("boost_percent", value.coerceIn(30, 100)).apply()

    /** 15 秒连续控制窗口：窗口内只播命令，不重复唤醒词 */
    var continuousWindow: Boolean
        get() = sp.getBoolean("continuous_window", true)
        set(value) = sp.edit().putBoolean("continuous_window", value).apply()

    /** 灯键数量：默认 1 个，用加号增加 */
    var lampCount: Int
        get() = sp.getInt("lamp_count", 1).coerceIn(1, 6)
        set(value) = sp.edit().putInt("lamp_count", value.coerceIn(1, 6)).apply()

    /** 当前选中的灯键 */
    var selectedSlot: Int
        get() = sp.getInt("selected_slot", 0).coerceIn(0, lampCount - 1)
        set(value) = sp.edit().putInt("selected_slot", value.coerceIn(0, lampCount - 1)).apply()

    /** 四个灯键各自的唤醒词，默认都是“小欧小欧” */
    fun slotWake(index: Int): String =
        sp.getString("slot_wake_$index", "小欧小欧") ?: "小欧小欧"

    fun setSlotWake(index: Int, wake: String) {
        sp.edit().putString("slot_wake_$index", wake).apply()
    }

    /** 删除某个灯键，后面的灯键依次前移 */
    fun removeSlot(index: Int) {
        val count = lampCount
        if (count <= 1 || index !in 0 until count) return
        for (i in index until count - 1) setSlotWake(i, slotWake(i + 1))
        setSlotWake(count - 1, "小欧小欧")
        lampCount = count - 1
        if (selectedSlot >= lampCount) selectedSlot = lampCount - 1
    }

    init {
        // 兼容上一版：把旧的“卧室灯唤醒词”迁到第一个灯键
        if (!sp.contains("slot_wake_0")) {
            val legacy = sp.getString("bedroom_wake", "小欧小欧") ?: "小欧小欧"
            if (legacy != "小欧小欧") sp.edit().putString("slot_wake_0", legacy).apply()
        }
    }
}
