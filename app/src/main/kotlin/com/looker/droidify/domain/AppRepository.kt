package com.looker.starrystore.domain

import com.looker.starrystore.domain.model.App
import com.looker.starrystore.domain.model.AppMinimal
import com.looker.starrystore.domain.model.Author
import com.looker.starrystore.domain.model.Package
import com.looker.starrystore.domain.model.PackageName
import kotlinx.coroutines.flow.Flow

interface AppRepository {

    fun getApps(): Flow<List<AppMinimal>>

    fun getApp(packageName: PackageName): Flow<List<App>>

    fun getAppFromAuthor(author: Author): Flow<List<App>>

    fun getPackages(packageName: PackageName): Flow<List<Package>>

    /**
     * returns true is the app is added successfully
     * returns false if the app was already in the favourites and so it is removed
     */
    suspend fun addToFavourite(packageName: PackageName): Boolean
}
