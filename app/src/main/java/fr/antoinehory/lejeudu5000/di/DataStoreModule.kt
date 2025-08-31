package fr.antoinehory.lejeudu5000.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.antoinehory.lejeudu5000.data.datastore.SettingsDataStore // Assurez-vous que le chemin est correct
import javax.inject.Singleton

/**
 * Hilt module for providing DataStore related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    /**
     * Provides a singleton instance of [SettingsDataStore].
     *
     * @param context The application context, injected by Hilt.
     * @return A singleton instance of [SettingsDataStore].
     */
    @Provides
    @Singleton
    fun provideSettingsDataStore(@ApplicationContext context: Context): SettingsDataStore {
        return SettingsDataStore(context)
    }

    // Note: provideApplicationContext n'est généralement pas nécessaire si vous injectez
    // @ApplicationContext directement là où c'est nécessaire (comme dans provideSettingsDataStore).
    // Si vous aviez besoin d'injecter un Context non qualifié, vous pourriez le fournir ici.
}
