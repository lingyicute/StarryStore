package com.looker.starrystore.utility.common.extension

import java.io.File

val File.size: Long?
    get() = if (exists()) length().takeIf { it > 0L } else null

