package dev.plantapp.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dev.plantapp.data.reminder.ReminderScheduler
import dev.plantapp.data.reminder.ReminderScheduling
import dev.plantapp.data.repository.AuthRepositoryImpl
import dev.plantapp.data.repository.InventoryRepositoryImpl
import dev.plantapp.data.repository.SessionRefreshManager
import dev.plantapp.data.settings.SettingsStore
import dev.plantapp.data.settings.TokenWriter
import dev.plantapp.domain.repository.AuthRepository
import dev.plantapp.domain.repository.InventoryRepository
import dev.plantapp.network.AuthTokenProvider
import dev.plantapp.network.PlantAppApi
import dev.plantapp.network.PlantAppApiFactory
import dev.plantapp.network.SessionRefresher
import dev.plantapp.network.SupabaseAuthApi
import dev.plantapp.network.SupabaseAuthApiFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.plantapp.data.BuildConfig
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "plantapp_settings")

/** Public local-dev Supabase anon key (well-known supabase-demo JWT — safe to commit).
 *  Override for a real Supabase project. */
private const val DEFAULT_ANON_KEY =
    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZS1kZW1vIiwicm9sZSI6ImFub24iLCJleHAiOjE5ODM4MTI5OTZ9.CRXP1A7WOeoJeXxjNni43kdQwgnWNReilDMblYTn_I0"

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
        refreshManager: SessionRefreshManager,
    ): PlantAppApi =
        PlantAppApiFactory.create(
            baseUrl = settings.baseUrlBlocking(BuildConfig.API_BASE_URL),
            tokenProvider = tokenProvider,
            sessionRefresher = SessionRefresher { refreshManager.refreshSessionBlocking() },
        )

    @Provides
    @Singleton
    fun provideSupabaseAuthApi(): SupabaseAuthApi =
        SupabaseAuthApiFactory.create(authBaseUrl = BuildConfig.AUTH_BASE_URL, anonKey = DEFAULT_ANON_KEY)

    @Provides
    @Singleton
    fun provideClock(): java.time.Clock = java.time.Clock.systemUTC()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindInventoryRepository(impl: InventoryRepositoryImpl): InventoryRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindTokenWriter(impl: SettingsStore): TokenWriter

    @Binds
    @Singleton
    abstract fun bindReminderScheduling(impl: ReminderScheduler): ReminderScheduling
}
