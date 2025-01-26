package com.looker.starrystore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.looker.starrystore.utility.common.Exporter
import com.looker.starrystore.datastore.PreferenceSettingsRepository
import com.looker.starrystore.datastore.Settings
import com.looker.starrystore.datastore.SettingsRepository
import com.looker.starrystore.datastore.SettingsSerializer
import com.looker.starrystore.datastore.exporter.SettingsExporter
import com.looker.starrystore.datastore.migration.ProtoToPreferenceMigration
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import javax.inject.Singleton

private const val PREFERENCES = "settings_file"

private const val SETTINGS = "settings"

@Module
@InstallIn(SingletonComponent::class)
object DatastoreModule {

    @Singleton
    @Provides
    fun provideProtoDatastore(
        @ApplicationContext context: Context,
    ): DataStore<Settings> = DataStoreFactory.create(
        serializer = SettingsSerializer,
    ) {
        context.dataStoreFile(PREFERENCES)
    }

    @Singleton
    @Provides
    fun providePreferenceDatastore(
        @ApplicationContext context: Context,
        oldDatastore: DataStore<Settings>,
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        migrations = listOf(
            ProtoToPreferenceMigration(oldDatastore)
        )
    ) {
        context.preferencesDataStoreFile(SETTINGS)
    }

    @Singleton
    @Provides
    fun provideSettingsExporter(
        @ApplicationContext context: Context,
        @ApplicationScope scope: CoroutineScope,
        @IoDispatcher dispatcher: CoroutineDispatcher
    ): Exporter<Settings> = SettingsExporter(
        context = context,
        scope = scope,
        ioDispatcher = dispatcher,
        json = Json {
            encodeDefaults = true
            prettyPrint = true
        }
    )

    @Singleton
    @Provides
    fun provideSettingsRepository(
        dataStore: DataStore<Preferences>,
        exporter: Exporter<Settings>
    ): SettingsRepository = PreferenceSettingsRepository(dataStore, exporter)
}
