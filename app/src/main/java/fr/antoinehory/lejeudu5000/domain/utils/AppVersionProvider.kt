package fr.antoinehory.lejeudu5000.domain.utils

import fr.antoinehory.lejeudu5000.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides access to the application's version name.
 * This interface allows for easier testing by decoupling ViewModels
 * or other components from the static [BuildConfig].
 */
interface AppVersionProvider {
    /**
     * @return The application's version name (e.g., "1.0.0").
     */
    fun getVersionName(): String
}

/**
 * Default implementation of [AppVersionProvider] that retrieves the
 * version name directly from [BuildConfig].
 */
@Singleton // Or appropriate scope if needed elsewhere with a different lifecycle
class BuildConfigAppVersionProvider @Inject constructor() : AppVersionProvider {
    /**
     * Retrieves the application version name from [BuildConfig.VERSION_NAME].
     *
     * @return The application's version name.
     */
    override fun getVersionName(): String = BuildConfig.VERSION_NAME
}
