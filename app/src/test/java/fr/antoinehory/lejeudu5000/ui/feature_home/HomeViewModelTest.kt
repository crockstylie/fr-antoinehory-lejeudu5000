package fr.antoinehory.lejeudu5000.ui.feature_home

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class HomeViewModelTest {

    private lateinit var viewModel: HomeViewModel
    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = HomeViewModel()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state shows no game in progress`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertFalse(uiState.hasGameInProgress)
        assertFalse(uiState.isLoading)
    }

    @Test
    fun `onNewGameStarted sets hasGameInProgress to true`() {
        viewModel.onNewGameStarted()

        val uiState = viewModel.uiState.value
        assertTrue(uiState.hasGameInProgress)
    }

    @Test
    fun `onGameEnded sets hasGameInProgress to false`() {
        // Arrange
        viewModel.onNewGameStarted()

        // Act
        viewModel.onGameEnded()

        // Assert
        val uiState = viewModel.uiState.value
        assertFalse(uiState.hasGameInProgress)
    }
}