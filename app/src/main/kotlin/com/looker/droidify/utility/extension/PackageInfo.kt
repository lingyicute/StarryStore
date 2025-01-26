package com.looker.starrystore.utility.extension

import android.content.pm.PackageInfo
import com.looker.starrystore.utility.common.extension.calculateHash
import com.looker.starrystore.utility.common.extension.singleSignature
import com.looker.starrystore.utility.common.extension.versionCodeCompat
import com.looker.starrystore.model.InstalledItem

fun PackageInfo.toInstalledItem(): InstalledItem {
    val signatureString = singleSignature?.calculateHash().orEmpty()
    return InstalledItem(
        packageName,
        versionName.orEmpty(),
        versionCodeCompat,
        signatureString
    )
}
