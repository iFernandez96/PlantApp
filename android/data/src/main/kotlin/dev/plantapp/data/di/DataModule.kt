package dev.plantapp.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dev.plantapp.data.repository.InventoryRepositoryImpl
import dev.plantapp.data.settings.SettingsStore
import dev.plantapp.domain.repository.InventoryRepository
import dev.plantapp.network.AuthTokenProvider
import dev.plantapp.network.PlantAppApi
import dev.plantapp.network.PlantAppApiFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "plantapp_settings")

/** Default local base URL: Android emulator loopback to the host's Supabase API (54321).
 *  Overridable at runtime via [SettingsStore]. */
private const val DEFAULT_BASE_URL = "http://10.0.2.2:54321/"

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.settingsDataStore

    @Provides
    @Singleton
    fun provideAuthTokenProvider(settings: SettingsStore): AuthTokenProvider =
        AuthTokenProvider { settings.tokenBlocking() }

    @Provides
    @Singleton
    fun providePlantAppApi(
        settings: SettingsStore,
        tokenProvider: AuthTokenProvider,
    ): PlantAppApi =
        PlantAppApiFactory.create(
            baseUrl = settings.baseUrlBlocking(DEFAULT_BASE_URL),
            tokenProvider = tokenProvider,
        )
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindInventoryRepository(impl: InventoryRepositoryImpl): InventoryRepository
}
