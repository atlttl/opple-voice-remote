package com.opple.voiceremote

import android.content.ComponentName
import android.content.pm.PackageManager
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LauncherIconTest {

    @Test
    fun switchesBetweenTwoLauncherAliases() {
        val ctx = RuntimeEnvironment.getApplication()
        val pm = ctx.packageManager

        // 1 = Apple（暖光）
        LauncherIcon.apply(ctx, 1)
        assertEquals(
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            pm.getComponentEnabledSetting(ComponentName(ctx.packageName, ctx.packageName + ".IconWarm")),
        )
        assertEquals(
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            pm.getComponentEnabledSetting(ComponentName(ctx.packageName, ctx.packageName + ".IconLine")),
        )

        // 0 = Android（蓝调，默认）
        LauncherIcon.apply(ctx, 0)
        assertEquals(
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            pm.getComponentEnabledSetting(ComponentName(ctx.packageName, ctx.packageName + ".IconLine")),
        )
        assertEquals(
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            pm.getComponentEnabledSetting(ComponentName(ctx.packageName, ctx.packageName + ".IconWarm")),
        )
    }
}
