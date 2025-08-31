package fr.antoinehory.lejeudu5000.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.antoinehory.lejeudu5000.domain.utils.AppVersionProvider
import fr.antoinehory.lejeudu5000.domain.utils.BuildConfigAppVersionProvider
import javax.inject.Singleton

/**
 * Application-level Hilt module for providing common dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    /**
     * Binds [BuildConfigAppVersionProvider] to its [AppVersionProvider] interface.
     *
     * This allows Hilt to inject [AppVersionProvider] where needed,
     * promoting loose coupling and testability.
     *
     * @param impl The concrete implementation of [AppVersionProvider].
     * @return An instance of [AppVersionProvider].
     */
    @Binds
    @Singleton // Ensure a single instance throughout the app
    abstract fun bindAppVersionProvider(
        impl: BuildConfigAppVersionProvider
    ): AppVersionProvider

    // You can add other application-wide bindings here if needed in the future.
}
