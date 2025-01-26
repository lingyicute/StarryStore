package com.looker.starrystore.installer

import android.content.Context
import com.looker.starrystore.utility.common.extension.addAndCompute
import com.looker.starrystore.utility.common.extension.filter
import com.looker.starrystore.utility.common.extension.notificationManager
import com.looker.starrystore.utility.common.extension.updateAsMutable
import com.looker.starrystore.datastore.SettingsRepository
import com.looker.starrystore.datastore.get
import com.looker.starrystore.datastore.model.InstallerType
import com.looker.starrystore.domain.model.PackageName
import com.looker.starrystore.installer.installers.Installer
import com.looker.starrystore.installer.installers.LegacyInstaller
import com.looker.starrystore.installer.installers.root.RootInstaller
import com.looker.starrystore.installer.installers.session.SessionInstaller
import com.looker.starrystore.installer.installers.shizuku.ShizukuInstaller
import com.looker.starrystore.installer.model.InstallItem
import com.looker.starrystore.installer.model.InstallState
import com.looker.starrystore.installer.notification.removeInstallNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InstallManager(
    private val context: Context,
    settingsRepository: SettingsRepository
) {

    private val installItems = Channel<InstallItem>()
    private val uninstallItems = Channel<PackageName>()

    val state = MutableStateFlow<Map<PackageName, InstallState>>(emptyMap())

    private var _installer: Installer? = null
        set(value) {
            field?.close()
            field = value
        }
    private val installer: Installer get() = _installer!!

    private val lock = Mutex()
    private val installerPreference = settingsRepository.get { installerType }

    suspend operator fun invoke() = coroutineScope {
        setupInstaller()
        installer()
        uninstaller()
    }

    fun close() {
        _installer = null
        uninstallItems.close()
        installItems.close()
    }

    suspend infix fun install(installItem: InstallItem) {
        installItems.send(installItem)
    }

    suspend infix fun uninstall(packageName: PackageName) {
        uninstallItems.send(packageName)
    }

    infix fun remove(packageName: PackageName) {
        updateState { remove(packageName) }
    }

    private fun CoroutineScope.setupInstaller() = launch {
        installerPreference.collectLatest(::setInstaller)
    }

    private fun CoroutineScope.installer() = launch {
        val currentQueue = mutableSetOf<String>()
        installItems.filter { item ->
            currentQueue.addAndCompute(item.packageName.name) { isAdded ->
                if (isAdded) {
                    updateState { put(item.packageName, InstallState.Pending) }
                }
            }
        }.consumeEach { item ->
            if (state.value.containsKey(item.packageName)) {
                updateState { put(item.packageName, InstallState.Installing) }
                val success = installer.use {
                    it.install(item)
                }
                context.notificationManager?.removeInstallNotification(item.packageName.name)
                updateState { put(item.packageName, success) }
                currentQueue.remove(item.packageName.name)
            }
        }
    }

    private fun CoroutineScope.uninstaller() = launch {
        uninstallItems.consumeEach {
            installer.uninstall(it)
        }
    }

    private suspend fun setInstaller(installerType: InstallerType) {
        lock.withLock {
            _installer = when (installerType) {
                InstallerType.LEGACY -> LegacyInstaller(context)
                InstallerType.SESSION -> SessionInstaller(context)
                InstallerType.SHIZUKU -> ShizukuInstaller(context)
                InstallerType.ROOT -> RootInstaller(context)
            }
        }
    }

    private inline fun updateState(block: MutableMap<PackageName, InstallState>.() -> Unit) {
        state.update { it.updateAsMutable(block) }
    }
}
