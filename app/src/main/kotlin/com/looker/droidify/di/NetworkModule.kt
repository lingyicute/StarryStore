package com.looker.starrystore.di

import com.looker.starrystore.network.Downloader
import com.looker.starrystore.network.KtorDownloader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    fun provideDownloader(
        @IoDispatcher
        dispatcher: CoroutineDispatcher
    ): Downloader = KtorDownloader(
        httpClientEngine = OkHttp.create(),
        dispatcher = dispatcher,
    )

}
