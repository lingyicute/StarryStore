package com.looker.starrystore.sync.common

import kotlinx.serialization.json.Json

val JsonParser = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    isLenient = true
}
