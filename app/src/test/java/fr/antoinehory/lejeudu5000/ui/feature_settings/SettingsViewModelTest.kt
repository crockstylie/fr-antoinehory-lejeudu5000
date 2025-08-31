package fr.antoinehory.lejeudu5000.ui.feature_settings

import app.cash.turbine.test
import fr.antoinehory.lejeudu5000.data.repository.SettingsRepository
import fr.antoinehory.lejeudu5000.domain.model.GameSettings
import fr.antoinehory.lejeudu5000.util.CoroutineDispatchers
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Unit tests for the [SettingsViewModel].
 *
 * These tests verify the ViewModel's logic for loading and updating game settings,
 * ensuring correct interaction with the [SettingsRepository] and proper UI state updates.
 * MockK is used for mocking dependencies, and kotlinx-coroutines-test for managing coroutines.
 */
@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class) // Integrates MockK with JUnit 5
class SettingsViewModelTest {

    // Test dispatcher for coroutines
    private lateinit var testDispatcher: TestDispatcher

    // Mocks
    @RelaxedMockK // Relaxed mock for SettingsRepository, defaults to returning empty/null for non-stubbed methods
    private lateinit var mockSettingsRepository: SettingsRepository

    private lateinit var mockCoroutineDispatchers: CoroutineDispatchers

    // Subject under test
    private lateinit var viewModel: SettingsViewModel

    @BeforeEach
    fun setUp() {
        testDispatcher = StandardTestDispatcher() // Provides a TestCoroutineScheduler
        Dispatchers.setMain(testDispatcher) // Set main dispatcher for ViewModel's viewModelScope

        // Mock CoroutineDispatchers to use the testDispatcher for all its properties
        mockCoroutineDispatchers = mockk {
            every { io } returns testDispatcher
            every { main } returns testDispatcher
            every { default } returns testDispatcher
        }
    }

    private fun initializeViewModel() {
        viewModel = SettingsViewModel(mockSettingsRepository, mockCoroutineDispatchers)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain() // Reset main dispatcher after each test
    }

    @Nested
    @DisplayName("Initialization Tests")
    inner class Initialization {
        @Test
        @DisplayName("GIVEN repository returns settings successfully WHEN ViewModel is initialized THEN UI state reflects loaded settings")
        fun init_loadsSettingsAndUpdatesUiState_onSuccess() = runTest(testDispatcher) {
            // GIVEN
            val expectedGameSettings = GameSettings(openingScoreThreshold = 750, victoryScore = 10000)
            every { mockSettingsRepository.getGameSettings() } returns flowOf(expectedGameSettings)

            // WHEN
            initializeViewModel() // ViewModel init block calls loadSettings

            // THEN
            viewModel.uiState.test {
                var emittedItem = awaitItem() // Initial state (isLoading = true)
                assertTrue(emittedItem.isLoading, "Initial state should be loading")

                emittedItem = awaitItem() // State after successful load
                assertFalse(emittedItem.isLoading, "State should not be loading after success")
                assertEquals(expectedGameSettings, emittedItem.gameSettings, "GameSettings should match expected")
                assertNull(emittedItem.error, "Error should be null on success")
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        @DisplayName("GIVEN repository throws error WHEN ViewModel is initialized THEN UI state reflects error")
        fun init_loadsSettingsAndUpdatesUiState_onError() = runTest(testDispatcher) {
            // GIVEN
            val errorMessage = "Database error"
            every { mockSettingsRepository.getGameSettings() } returns flow { throw RuntimeException(errorMessage) }

            // WHEN
            initializeViewModel()

            // THEN
            viewModel.uiState.test {
                var emittedItem = awaitItem() // Initial state (isLoading = true)
                assertTrue(emittedItem.isLoading, "Initial state should be loading")

                emittedItem = awaitItem() // State after error
                assertFalse(emittedItem.isLoading, "State should not be loading after error")
                assertNotNull(emittedItem.error, "Error should not be null")
                assertTrue(emittedItem.error!!.contains(errorMessage), "Error message should contain original error")
                // GameSettings might be default or last known, depending on implementation detail if needed.
                // Here, we primarily care about the error state.
                assertEquals(GameSettings(), emittedItem.gameSettings, "GameSettings should be default on error")
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    @DisplayName("Settings Update Tests")
    inner class SettingsUpdates {

        @BeforeEach
        fun setupViewModelForUpdates() {
            // Ensure a non-error state for update tests by providing a default flow
            every { mockSettingsRepository.getGameSettings() } returns flowOf(GameSettings())
            initializeViewModel()
            // Consume initial loading states if necessary, or ensure testDispatcher advances past init
            testDispatcher.scheduler.advanceUntilIdle()
        }

        @Test
        @DisplayName("WHEN onOpeningScoreThresholdChanged is called THEN repository is updated")
        fun onOpeningScoreThresholdChanged_updatesRepository() = runTest(testDispatcher) {
            // GIVEN
            val newThreshold = 1000

            // WHEN
            viewModel.onOpeningScoreThresholdChanged(newThreshold)
            testDispatcher.scheduler.advanceUntilIdle() // Ensure the launched coroutine completes

            // THEN
            coVerify { mockSettingsRepository.updateOpeningScoreThreshold(newThreshold) }
        }

        @Test
        @DisplayName("WHEN onVictoryScoreChanged is called THEN repository is updated")
        fun onVictoryScoreChanged_updatesRepository() = runTest(testDispatcher) {
            // GIVEN
            val newScore = 10000

            // WHEN
            viewModel.onVictoryScoreChanged(newScore)
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            coVerify { mockSettingsRepository.updateVictoryScore(newScore) }
        }

        @Test
        @DisplayName("WHEN onMustWinOnExactScoreChanged is called THEN repository is updated")
        fun onMustWinOnExactScoreChanged_updatesRepository() = runTest(testDispatcher) {
            // GIVEN
            val newValue = true

            // WHEN
            viewModel.onMustWinOnExactScoreChanged(newValue)
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            coVerify { mockSettingsRepository.updateMustWinOnExactScore(newValue) }
        }

        @Test
        @DisplayName("WHEN onCancelOpponentScoreOnMatchChanged is called THEN repository is updated")
        fun onCancelOpponentScoreOnMatchChanged_updatesRepository() = runTest(testDispatcher) {
            // GIVEN
            val newValue = true

            // WHEN
            viewModel.onCancelOpponentScoreOnMatchChanged(newValue)
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            coVerify { mockSettingsRepository.updateCancelOpponentScoreOnMatch(newValue) }
        }

        @Test
        @DisplayName("WHEN onAllowFiftyPointScoresChanged is called THEN repository is updated")
        fun onAllowFiftyPointScoresChanged_updatesRepository() = runTest(testDispatcher) {
            // GIVEN
            val newValue = true

            // WHEN
            viewModel.onAllowFiftyPointScoresChanged(newValue)
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            coVerify { mockSettingsRepository.updateAllowFiftyPointScores(newValue) }
        }

        @Test
        @DisplayName("WHEN onUseThreeLivesRuleChanged is called THEN repository is updated")
        fun onUseThreeLivesRuleChanged_updatesRepository() = runTest(testDispatcher) {
            // GIVEN
            val newValue = true

            // WHEN
            viewModel.onUseThreeLivesRuleChanged(newValue)
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            coVerify { mockSettingsRepository.updateUseThreeLivesRule(newValue) }
        }

        @Test
        @DisplayName("WHEN onAllowStealOnPassChanged is called THEN repository is updated")
        fun onAllowStealOnPassChanged_updatesRepository() = runTest(testDispatcher) {
            // GIVEN
            val newValue = true

            // WHEN
            viewModel.onAllowStealOnPassChanged(newValue)
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            coVerify { mockSettingsRepository.updateAllowStealOnPass(newValue) }
        }
    }
}
