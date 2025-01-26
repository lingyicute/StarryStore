package com.looker.starrystore.ui.favourites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.looker.starrystore.utility.common.extension.asStateFlow
import com.looker.starrystore.datastore.SettingsRepository
import com.looker.starrystore.datastore.get
import com.looker.starrystore.model.Product
import com.looker.starrystore.database.Database
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@HiltViewModel
class FavouritesViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val favouriteApps: StateFlow<List<List<Product>>> =
        settingsRepository
            .get { favouriteApps }
            .map { favourites ->
                favourites.mapNotNull { app ->
                    Database.ProductAdapter.get(app, null).ifEmpty { null }
                }
            }.asStateFlow(emptyList())

    fun updateFavourites(packageName: String) {
        viewModelScope.launch {
            settingsRepository.toggleFavourites(packageName)
        }
    }
}
