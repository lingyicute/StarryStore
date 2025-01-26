package com.looker.starrystore.ui.settings

import android.content.Context
import android.net.Uri
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.looker.starrystore.datastore.Settings
import com.looker.starrystore.datastore.SettingsRepository
import com.looker.starrystore.datastore.get
import com.looker.starrystore.datastore.model.AutoSync
import com.looker.starrystore.datastore.model.InstallerType
import com.looker.starrystore.datastore.model.InstallerType.*
import com.looker.starrystore.datastore.model.ProxyType
import com.looker.starrystore.datastore.model.Theme
import com.looker.starrystore.database.Database
import com.looker.starrystore.database.RepositoryExporter
import com.looker.starrystore.work.CleanUpWorker
import com.looker.starrystore.installer.installers.isMagiskGranted
import com.looker.starrystore.installer.installers.isShizukuAlive
import com.looker.starrystore.installer.installers.isShizukuGranted
import com.looker.starrystore.installer.installers.isShizukuInstalled
import com.looker.starrystore.installer.installers.requestPermissionListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
import kotlin.time.Duration
import com.looker.starrystore.R

@HiltViewModel
class SettingsViewModel
@Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val repositoryExporter: RepositoryExporter
) : ViewModel() {

    private val initialSetting = flow {
        emit(settingsRepository.getInitial())
    }
    val settingsFlow get() = settingsRepository.data

    private val _backgroundTask = MutableStateFlow(false)
    val backgroundTask = _backgroundTask.asStateFlow()

    private val _snackbarStringId = MutableSharedFlow<Int>()
    val snackbarStringId = _snackbarStringId.asSharedFlow()

    fun <T> getSetting(block: Settings.() -> T): Flow<T> = settingsRepository.get(block)

    fun <T> getInitialSetting(block: Settings.() -> T): Flow<T> = initialSetting.map { it.block() }

    fun allowBackground() {
        viewModelScope.launch {
            _backgroundTask.emit(true)
        }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch {
            val appLocale = LocaleListCompat.create(language.toLocale())
            AppCompatDelegate.setApplicationLocales(appLocale)
            settingsRepository.setLanguage(language)
        }
    }

    fun setTheme(theme: Theme) {
        viewModelScope.launch {
            settingsRepository.setTheme(theme)
        }
    }

    fun setDynamicTheme(enable: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDynamicTheme(enable)
        }
    }

    fun setHomeScreenSwiping(enable: Boolean) {
        viewModelScope.launch {
            settingsRepository.setHomeScreenSwiping(enable)
        }
    }

    fun setCleanUpInterval(interval: Duration) {
        viewModelScope.launch {
            settingsRepository.setCleanUpInterval(interval)
        }
    }

    fun forceCleanup(context: Context) {
        viewModelScope.launch {
            CleanUpWorker.force(context)
        }
    }

    fun setAutoSync(autoSync: AutoSync) {
        viewModelScope.launch {
            settingsRepository.setAutoSync(autoSync)
        }
    }

    fun setNotifyUpdates(enable: Boolean) {
        viewModelScope.launch {
            settingsRepository.enableNotifyUpdates(enable)
        }
    }

    fun setAutoUpdate(enable: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAutoUpdate(enable)
        }
    }

    fun setUnstableUpdates(enable: Boolean) {
        viewModelScope.launch {
            settingsRepository.enableUnstableUpdates(enable)
        }
    }

    fun setIgnoreSignature(enable: Boolean) {
        viewModelScope.launch {
            settingsRepository.setIgnoreSignature(enable)
        }
    }

    fun setIncompatibleUpdates(enable: Boolean) {
        viewModelScope.launch {
            settingsRepository.enableIncompatibleVersion(enable)
        }
    }

    fun setProxyType(proxyType: ProxyType) {
        viewModelScope.launch {
            settingsRepository.setProxyType(proxyType)
        }
    }

    fun setProxyHost(proxyHost: String) {
        viewModelScope.launch {
            settingsRepository.setProxyHost(proxyHost)
        }
    }

    fun setProxyPort(proxyPort: String) {
        viewModelScope.launch {
            try {
                settingsRepository.setProxyPort(proxyPort.toInt())
            } catch (_: NumberFormatException) {
                createSnackbar(R.string.proxy_port_error_not_int)
            }
        }
    }

    fun setInstaller(context: Context, installerType: InstallerType) {
        viewModelScope.launch {
            when (installerType) {
                SHIZUKU -> {
                    if (isShizukuInstalled(context)) {
                        if (!isShizukuAlive()) {
                            createSnackbar(R.string.shizuku_not_alive)
                            return@launch
                        } else if (isShizukuGranted()) {
                            settingsRepository.setInstallerType(installerType)
                        } else if (!isShizukuGranted()) {
                            if(requestPermissionListener()) {
                                settingsRepository.setInstallerType(installerType)
                            }
                        }
                    } else {
                        createSnackbar(R.string.shizuku_not_installed)
                    }
                }

                ROOT -> {
                    if (isMagiskGranted()) {
                        settingsRepository.setInstallerType(installerType)
                    }
                }

                else -> {
                    settingsRepository.setInstallerType(installerType)
                }
            }
        }
    }

    fun exportSettings(file: Uri) {
        viewModelScope.launch {
            settingsRepository.export(file)
        }
    }

    fun importSettings(file: Uri) {
        viewModelScope.launch {
            settingsRepository.import(file)
        }
    }

    fun exportRepos(file: Uri) {
        viewModelScope.launch {
            val repos = Database.RepositoryAdapter.getAll()
            repositoryExporter.export(repos, file)
        }
    }

    fun importRepos(file: Uri) {
        viewModelScope.launch {
            val repos = repositoryExporter.import(file)
            Database.RepositoryAdapter.importRepos(repos)
        }
    }

    fun createSnackbar(@StringRes message: Int) {
        viewModelScope.launch {
            _snackbarStringId.emit(message)
        }
    }
}

private fun String.toLocale(): Locale = when {
    contains("-r") -> Locale(
        substring(0, 2),
        substring(4)
    )

    contains("_") -> Locale(
        substring(0, 2),
        substring(3)
    )

    else -> Locale(this)
}
