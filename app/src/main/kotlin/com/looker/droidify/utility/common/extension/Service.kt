package com.looker.starrystore.utility.common.extension

import android.app.Service
import android.content.Intent
import com.looker.starrystore.utility.common.SdkCheck

fun Service.startServiceCompat() {
    val intent = Intent(this, this::class.java)
    if (SdkCheck.isOreo) {
        startForegroundService(intent)
    } else {
        startService(intent)
    }
}

fun Service.stopForegroundCompat(removeNotification: Boolean = true) {
    @Suppress("DEPRECATION")
    if (SdkCheck.isNougat) {
        stopForeground(
            if (removeNotification) {
                Service.STOP_FOREGROUND_REMOVE
            } else {
                Service.STOP_FOREGROUND_DETACH
            }
        )
    } else {
        stopForeground(removeNotification)
    }
    stopSelf()
}
