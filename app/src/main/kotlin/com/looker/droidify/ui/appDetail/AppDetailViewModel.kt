package com.looker.starrystore.ui.appDetail

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.looker.starrystore.utility.common.extension.asStateFlow
import com.looker.starrystore.datastore.SettingsRepository
import com.looker.starrystore.datastore.model.InstallerType
import com.looker.starrystore.domain.model.toPackageName
import com.looker.starrystore.BuildConfig
import com.looker.starrystore.database.Database
import com.looker.starrystore.model.InstalledItem
import com.looker.starrystore.model.Product
import com.looker.starrystore.model.Repository
import com.looker.starrystore.installer.InstallManager
import com.looker.starrystore.installer.installers.isShizukuAlive
import com.looker.starrystore.installer.installers.isShizukuGranted
import com.looker.starrystore.installer.installers.isShizukuInstalled
import com.looker.starrystore.installer.installers.requestPermissionListener
import com.looker.starrystore.installer.model.InstallState
import com.looker.starrystore.installer.model.installFrom
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class AppDetailViewModel @Inject constructor(
    private val installer: InstallManager,
    private val settingsRepository: SettingsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val packageName: String = requireNotNull(savedStateHandle[ARG_PACKAGE_NAME])

    private val repoAddress: StateFlow<String?> =
        savedStateHandle.getStateFlow(ARG_REPO_ADDRESS, null)

    val installerState: StateFlow<InstallState?> =
        installer.state.mapNotNull { stateMap ->
            stateMap[packageName.toPackageName()]
        }.asStateFlow(null)

    val state =
        combine(
            Database.ProductAdapter.getStream(packageName),
            Database.RepositoryAdapter.getAllStream(),
            Database.InstalledAdapter.getStream(packageName),
            repoAddress,
            flow { emit(settingsRepository.getInitial()) }
        ) { products, repositories, installedItem, suggestedAddress, initialSettings ->
            val idAndRepos = repositories.associateBy { it.id }
            val filteredProducts = products.filter { product ->
                idAndRepos[product.repositoryId] != null
            }
            AppDetailUiState(
                products = filteredProducts,
                repos = repositories,
                installedItem = installedItem,
                isFavourite = packageName in initialSettings.favouriteApps,
                allowIncompatibleVersions = initialSettings.incompatibleVersions,
                isSelf = packageName == BuildConfig.APPLICATION_ID,
                addressIfUnavailable = suggestedAddress
            )
        }.asStateFlow(AppDetailUiState())

    fun shizukuState(context: Context): ShizukuState? {
        val isSelected =
            runBlocking { settingsRepository.getInitial().installerType == InstallerType.SHIZUKU }
        if (!isSelected) return null
        val isAlive = isShizukuAlive()
        val isGranted = if (isAlive) {
            if (isShizukuGranted()) {
                true
            } else {
                runBlocking { requestPermissionListener() }
            }
        } else false
        return ShizukuState(
            isNotInstalled = !isShizukuInstalled(context),
            isNotGranted = !isGranted,
            isNotAlive = !isAlive,
        )
    }

    fun setDefaultInstaller() {
        viewModelScope.launch {
            settingsRepository.setInstallerType(InstallerType.Default)
        }
    }

    suspend fun shouldIgnoreSignature(): Boolean {
        return settingsRepository.getInitial().ignoreSignature
    }

    fun setFavouriteState() {
        viewModelScope.launch {
            settingsRepository.toggleFavourites(packageName)
        }
    }

    fun installPackage(packageName: String, fileName: String) {
        viewModelScope.launch {
            installer install (packageName installFrom fileName)
        }
    }

    fun uninstallPackage() {
        viewModelScope.launch {
            installer uninstall packageName.toPackageName()
        }
    }

    fun removeQueue() {
        viewModelScope.launch {
            installer remove packageName.toPackageName()
        }
    }

    companion object {
        const val ARG_PACKAGE_NAME = "package_name"
        const val ARG_REPO_ADDRESS = "repo_address"
    }
}

data class ShizukuState(
    val isNotInstalled: Boolean,
    val isNotGranted: Boolean,
    val isNotAlive: Boolean,
) {
    val check: Boolean
        get() = isNotInstalled || isNotAlive || isNotGranted
}

data class AppDetailUiState(
    val products: List<Product> = emptyList(),
    val repos: List<Repository> = emptyList(),
    val installedItem: InstalledItem? = null,
    val isSelf: Boolean = false,
    val isFavourite: Boolean = false,
    val allowIncompatibleVersions: Boolean = false,
    val addressIfUnavailable: String? = null
)
