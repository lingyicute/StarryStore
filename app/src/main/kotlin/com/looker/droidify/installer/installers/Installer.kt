package com.looker.starrystore.installer.installers

import com.looker.starrystore.domain.model.PackageName
import com.looker.starrystore.installer.model.InstallItem
import com.looker.starrystore.installer.model.InstallState

interface Installer : AutoCloseable {

    suspend fun install(installItem: InstallItem): InstallState

    suspend fun uninstall(packageName: PackageName)

}
