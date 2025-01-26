package com.looker.starrystore.utility.extension

import com.looker.starrystore.model.InstalledItem
import com.looker.starrystore.model.Product
import com.looker.starrystore.model.Repository
import com.looker.starrystore.model.findSuggested
import com.looker.starrystore.service.Connection
import com.looker.starrystore.service.DownloadService
import com.looker.starrystore.utility.extension.android.Android

fun Connection<DownloadService.Binder, DownloadService>.startUpdate(
    packageName: String,
    installedItem: InstalledItem?,
    products: List<Pair<Product, Repository>>
) {
    if (binder == null || products.isEmpty()) return

    val (product, repository) = products.findSuggested(installedItem) ?: return

    val compatibleReleases = product.selectedReleases
        .filter { installedItem == null || installedItem.signature == it.signature }
        .ifEmpty { return }

    val selectedRelease = compatibleReleases.singleOrNull() ?: compatibleReleases.run {
        filter { Android.primaryPlatform in it.platforms }.minByOrNull { it.platforms.size }
            ?: minByOrNull { it.platforms.size }
            ?: firstOrNull()
    } ?: return

    requireNotNull(binder).enqueue(
        packageName = packageName,
        name = product.name,
        repository = repository,
        release = selectedRelease,
        isUpdate = installedItem != null
    )
}
