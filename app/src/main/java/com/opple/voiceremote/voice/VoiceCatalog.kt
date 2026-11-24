package com.opple.voiceremote.voice

/**
 * 口令词表，严格对应 OPPLE 第二代离线语音模块说明书。
 * 注意：口令必须逐字一致，模块不做模糊匹配。
 */
object VoiceCatalog {

    const val WAKE_DEFAULT = "小欧小欧"

    const val POWER_ON = "开灯"
    const val POWER_OFF = "关灯"

    const val BRIGHTER = "亮一点"
    const val DIMMER = "暗一点"
    const val BRIGHTEST = "最亮"
    const val DARKEST = "最暗"

    const val COOLER = "白一点"
    const val WARMER = "黄一点"
    const val WHITE = "白光"
    const val YELLOW = "黄光"

    const val AMBIENT_ON = "打开氛围光"
    const val AMBIENT_OFF = "关闭氛围光"
    const val DELAY_OFF = "延时关灯"
    const val QUIET = "安静模式"

    data class Scene(val label: String, val spoken: String)

    val scenes = listOf(
        Scene("会客", "会客模式"),
        Scene("娱乐", "娱乐模式"),
        Scene("影院", "影院模式"),
        Scene("夜灯", "夜灯模式"),
    )

    /** 房间名 -> 配对命令 / 配对后的唤醒词 */
    data class Room(val name: String, val pairCommand: String, val wakeWord: String)

    val rooms = listOf(
        Room("客厅", "设为客厅灯", "小欧客厅灯"),
        Room("餐厅", "设为餐厅灯", "小欧餐厅灯"),
        Room("主卧", "设为主卧灯", "小欧主卧灯"),
        Room("次卧", "设为次卧灯", "小欧次卧灯"),
        Room("书房", "设为书房灯", "小欧书房灯"),
        Room("阳台", "设为阳台灯", "小欧阳台灯"),
        Room("入户", "设为入户灯", "小欧入户灯"),
        Room("厨房", "设为厨房灯", "小欧厨房灯"),
        Room("卫生间", "设为卫生间灯", "小欧卫生间灯"),
    )

    /** 灯键可选唤醒词：默认词 + 九大房间 */
    val wakePresets: List<String> = listOf(WAKE_DEFAULT) + rooms.map { it.wakeWord }

    /**
     * 从唤醒词推导灯键名称。
     * 小欧小欧 -> 默认；小欧客厅灯 -> 客厅；客厅灯 -> 客厅。
     * 灯具的离线模块只认唤醒词，因此唤醒词就是这盏灯的身份。
     */
    fun labelFor(wake: String): String {
        val w = wake.trim()
        if (w.isEmpty()) return "未设置"
        if (w == WAKE_DEFAULT) return "默认"
        var s = w.removePrefix("小欧")
        if (s.endsWith("灯")) s = s.dropLast(1)
        s = s.trim()
        return s.ifBlank { "默认" }
    }

    /** 同名槽位自动编号，避免四个键都叫“默认”时无法区分 */
    fun labelsFor(wakes: List<String>): List<String> {
        val base = wakes.map { labelFor(it) }
        val counts = base.groupingBy { it }.eachCount()
        val seen = mutableMapOf<String, Int>()
        return base.map { label ->
            if ((counts[label] ?: 0) > 1) {
                val n = (seen[label] ?: 0) + 1
                seen[label] = n
                "$label $n"
            } else {
                label
            }
        }
    }
}
