package com.opple.voiceremote

import com.opple.voiceremote.voice.VoiceCatalog
import org.junit.Assert.assertEquals
import org.junit.Test

class WakeLabelTest {

    @Test
    fun derivesRoomNameFromWakeWord() {
        assertEquals("客厅", VoiceCatalog.labelFor("小欧客厅灯"))
        assertEquals("主卧", VoiceCatalog.labelFor("小欧主卧灯"))
        assertEquals("卫生间", VoiceCatalog.labelFor("小欧卫生间灯"))
        assertEquals("书房", VoiceCatalog.labelFor("书房灯"))
        assertEquals("默认", VoiceCatalog.labelFor("小欧小欧"))
        assertEquals("未设置", VoiceCatalog.labelFor("  "))
    }

    @Test
    fun disambiguatesDuplicateLabels() {
        assertEquals(
            listOf("默认 1", "默认 2", "默认 3", "默认 4"),
            VoiceCatalog.labelsFor(List(4) { "小欧小欧" }),
        )
        assertEquals(
            listOf("客厅", "主卧", "默认 1", "默认 2"),
            VoiceCatalog.labelsFor(
                listOf("小欧客厅灯", "小欧主卧灯", "小欧小欧", "小欧小欧")
            ),
        )
    }
}
