package fr.antoinehory.lejeudu5000.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.antoinehory.lejeudu5000.data.repository.SettingsRepository
import fr.antoinehory.lejeudu5000.data.repository.SettingsRepositoryImpl
import javax.inject.Singleton

/**
 * Hilt module for providing repository dependencies.
 * This module is responsible for binding repository interfaces to their concrete implementations.
 */
@Module
@InstallIn(SingletonComponent::class) // Repositories are typically singletons
abstract class RepositoryModule {

    /**
     * Binds [SettingsRepositoryImpl] to its [SettingsRepository] interface.
     *
     * This allows Hilt to inject [SettingsRepository] where needed,
     * using [SettingsRepositoryImpl] as the concrete implementation.
     * The [Singleton] annotation ensures that only one instance of the repository
     * is created and shared throughout the application's lifecycle.
     *
     * @param settingsRepositoryImpl The concrete implementation of [SettingsRepository].
     * @return An instance of [SettingsRepository].
     */
    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): SettingsRepository

    // Vous pourrez ajouter d'autres bindings de repository ici à l'avenir,
    // par exemple pour GameRepository quand il sera finalisé.
}
