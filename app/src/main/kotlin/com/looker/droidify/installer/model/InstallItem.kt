package com.looker.starrystore.installer.model

import com.looker.starrystore.domain.model.PackageName
import com.looker.starrystore.domain.model.toPackageName

data class InstallItem(
    val packageName: PackageName,
    val installFileName: String
)

infix fun String.installFrom(fileName: String) = InstallItem(this.toPackageName(), fileName)
