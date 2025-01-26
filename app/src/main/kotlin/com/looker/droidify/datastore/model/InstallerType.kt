package com.looker.starrystore.datastore.model

import com.looker.starrystore.utility.common.device.Miui

enum class InstallerType {
    LEGACY,
    SESSION,
    SHIZUKU,
    ROOT;

    companion object {
        val Default: InstallerType
            get() = if (Miui.isMiui) {
                if (Miui.isMiuiOptimizationDisabled()) SESSION else LEGACY
            } else {
                SESSION
            }
    }
}
