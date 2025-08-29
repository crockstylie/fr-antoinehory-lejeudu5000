package fr.antoinehory.lejeudu5000

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Custom [Application] class for "Le Jeu du 5000".
 *
 * This class is annotated with [@HiltAndroidApp] to enable Hilt for dependency injection
 * throughout the application. It serves as the entry point for Hilt's dependency graph.
 */
@HiltAndroidApp
class LeJeuDu5000App : Application() {

    /**
     * Called when the application is starting, before any other application objects have been created.
     * Use this method to perform application-level initializations.
     */
    override fun onCreate() {
        super.onCreate()
        // Application-specific initialization code can be placed here if needed in the future.
        // For example, setting up logging libraries, analytics, etc.
        // For now, only Hilt initialization via @HiltAndroidApp is required.
    }
}
