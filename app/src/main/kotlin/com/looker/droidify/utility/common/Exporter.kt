package com.looker.starrystore.utility.common

import android.net.Uri

interface Exporter<T> {

    suspend fun export(item: T, target: Uri)

    suspend fun import(target: Uri): T

}
