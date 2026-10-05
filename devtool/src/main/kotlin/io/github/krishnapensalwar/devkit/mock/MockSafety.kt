package io.github.krishnapensalwar.devkit.mock

import android.content.pm.ApplicationInfo
import android.content.Context

internal object MockSafety {
    @Volatile
    var debugBuild: Boolean = false
        private set

    fun init(context: Context) {
        debugBuild = context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
    }

    fun allowMocking(globalEnabled: Boolean): Boolean = debugBuild && globalEnabled
}
