package fr.antoinehory.lejeudu5000.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides standard CoroutineDispatchers for use throughout the application.
 * This class is injectable via Hilt.
 *
 * Exposes dispatchers for IO-bound, Main thread, and Default (CPU-intensive) operations.
 */
@Singleton // It's common to have a single instance of dispatchers provider
class CoroutineDispatchers @Inject constructor() {
    /**
     * Dispatcher for IO-bound work (e.g., network requests, disk I/O).
     */
    val io: CoroutineDispatcher = Dispatchers.IO

    /**
     * Dispatcher for Main thread operations (e.g., UI updates).
     */
    val main: CoroutineDispatcher = Dispatchers.Main

    /**
     * Dispatcher for CPU-intensive work.
     */
    val default: CoroutineDispatcher = Dispatchers.Default
}
