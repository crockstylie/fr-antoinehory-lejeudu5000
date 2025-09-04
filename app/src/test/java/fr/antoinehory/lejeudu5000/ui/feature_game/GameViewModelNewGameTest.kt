package fr.antoinehory.lejeudu5000.ui.feature_game

import app.cash.turbine.test
import fr.antoinehory.lejeudu5000.data.repository.SettingsRepository
import fr.antoinehory.lejeudu5000.domain.GameEngine
import fr.antoinehory.lejeudu5000.domain.model.Dice
import fr.antoinehory.lejeudu5000.domain.model.GameSettings
import fr.antoinehory.lejeudu5000.domain.model.GameState
import fr.antoinehory.lejeudu5000.domain.model.Player
import fr.antoinehory.lejeudu5000.domain.model.TurnData
import fr.antoinehory.lejeudu5000.domain.usecase.FinalizeTurnUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.UUID

/**
 * Unit tests for the "new game" functionality of [GameViewModel].
 */
@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class GameViewModelNewGameTest {

    @MockK
    private lateinit var gameEngine: GameEngine

    @MockK
    private lateinit var settingsRepository: SettingsRepository

    @MockK
    private lateinit var finalizeTurnUseCase: FinalizeTurnUseCase // Needed for GameViewModel init

    private lateinit var viewModel: GameViewModel

    private val testDispatcher = StandardTestDispatcher()
    private val defaultPlayerUuid: UUID = UUID.randomUUID()
    private val defaultPlayer = Player(id = defaultPlayerUuid, name = "Player 1", totalScore = 0)
    private val defaultGameSettings = GameSettings()

    private fun createTestDice(value: Int, isAvailable: Boolean = true): Dice =
        Dice(id = UUID.randomUUID(), value = value, isAvailable = isAvailable)

    private fun createTestDiceList(vararg values: Int): List<Dice> =
        values.map { createTestDice(value = it, isAvailable = true) }

    private val defaultInitialDice: List<Dice> = List(5) { createTestDice(value = 1, isAvailable = true) }
    private val defaultTurnData = TurnData(diceOnTable = defaultInitialDice, currentTurnScore = 0)
    private val defaultGameState = GameState(
        players = listOf(defaultPlayer),
        currentPlayerIndex = 0,
        turnData = defaultTurnData
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { settingsRepository.getGameSettings() } returns flowOf(defaultGameSettings)
        every { gameEngine.calculateScore(any()) } returns 0 // Default, can be overridden
        coEvery { finalizeTurnUseCase.invoke(any(), any()) } returns defaultGameState // Default
        coEvery { gameEngine.rollDice(any()) } returns defaultGameState.copy( // Default roll
            turnData = defaultTurnData.copy(diceOnTable = createTestDiceList(1,2,3,4,5))
        )

        viewModel = GameViewModel(
            gameEngine = gameEngine,
            settingsRepository = settingsRepository,
            finalizeTurnUseCase = finalizeTurnUseCase
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Tests that calling newGame resets the ViewModel to its initial state,
     * reflecting a brand new game.
     */
    @Test
    fun `newGame resets uiState to initial new game state`() = runTest {
        // 1. Setup: Modify state to be different from initial
        val scoreToBank = 600
        val diceRolledForSetup = createTestDiceList(6,6,6,2,3)

        // Mock gameEngine behavior for the setup phase
        every { gameEngine.calculateScore(match { it.size == 3 && it.count { d -> d.value == 6 } == 3 }) } returns scoreToBank
        // Ensure the roll of (6,6,6,2,3) is NOT a bust by returning its potential score (can be the score of three 6s or any >0)
        // If the combined score of (6,6,6,2,3) is calculated, it should be >0 to prevent immediate bust in rollDice()
        // For simplicity, let\'s say the engine identifies the three 6s as scorable even among other dice.
        every { gameEngine.calculateScore(diceRolledForSetup) } returns scoreToBank // Prevents bust

        coEvery { gameEngine.rollDice(any()) } returns defaultGameState.copy(
            turnData = defaultTurnData.copy(diceOnTable = diceRolledForSetup)
        )

        val bankedGameState = defaultGameState.copy(
            players = listOf(defaultPlayer.copy(totalScore = scoreToBank)),
            turnData = defaultTurnData.copy(currentTurnScore = 0, diceOnTable = defaultInitialDice) // Dice reset to initial after banking
        )
        coEvery { finalizeTurnUseCase.invoke(any(), any()) } returns bankedGameState

        // Perform actions to change the state (e.g., score points)
        viewModel.rollDice() // Rolls (6,6,6,2,3)
        testDispatcher.scheduler.advanceUntilIdle()

        // Select the three 6s
        val currentDiceInUi = viewModel.uiState.value.currentDice
        val sixesToSelect = currentDiceInUi.filter { it.value == 6 }
        assertTrue(sixesToSelect.size >= 3, "Not enough sixes to select. UI Dice: ${currentDiceInUi.map { it.value }}")

        viewModel.selectDice(sixesToSelect[0].id)
        viewModel.selectDice(sixesToSelect[1].id)
        viewModel.selectDice(sixesToSelect[2].id)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(scoreToBank, viewModel.uiState.value.selectedDiceScore, "Selected score for three 6s is wrong.")

        viewModel.keepSelectedDiceAndContinueTurn() // Keeps the 600 points
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(scoreToBank, viewModel.uiState.value.turnScore, "Turn score after keeping is wrong.")

        viewModel.endTurnAndBankAccumulatedScore() // Banks the 600 points
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.totalScore == scoreToBank, "Total score should be modified to $scoreToBank before newGame call. Was ${viewModel.uiState.value.totalScore}")

        // Ensure settings are re-fetched for a new game
        val freshSettings = defaultGameSettings.copy(victoryScore = 6000) // Create a new instance for clarity, change victory score
        coEvery {
            settingsRepository.getGameSettings()
        } coAnswers {
            // Message de diagnostic pour vérifier l\'appel lors du rechargement des paramètres
            println(">>> SETTINGS_RELOAD_TEST: settingsRepository.getGameSettings() CALLED to provide freshSettings")
            flowOf(freshSettings)
        }

        // Mocks for calculateScore for the initial dice state AFTER newGame()
        // newGame() should result in defaultInitialDice (five 1s)
        every { gameEngine.calculateScore(match { list -> list.size == 1 && list.first().value == 1 }) } returns 100
        every { gameEngine.calculateScore(match { list -> list.size == 1 && list.first().value == 5 }) } returns 50 // If a 5 could appear
        every { gameEngine.calculateScore(match { list -> list.size == 1 && list.first().value != 1 && list.first().value != 5 }) } returns 0

        // Ensure that if newGame() triggers a dice roll, it gets the default initial dice.
        coEvery { gameEngine.rollDice(any()) } returns defaultGameState

        // 2. Action
        viewModel.newGame()
        testDispatcher.scheduler.advanceUntilIdle()

        // 3. Verification
        val resetState = viewModel.uiState.value
        assertEquals(defaultPlayer.name, resetState.activePlayerName)
        assertEquals(0, resetState.totalScore, "Total score not reset.")
        assertEquals(0, resetState.turnScore, "Turn score not reset.")
        assertEquals(0, resetState.selectedDiceScore, "Selected dice score not reset.")
        assertEquals(5, resetState.currentDice.size, "Dice count wrong after new game.")
        // Verify dice are the default (five 1s)
        assertTrue(resetState.currentDice.all { it.value == 1 }, "Dice not reset to five 1s.")


        // Check canBeHeld for the new dice (all 1s)
        assertTrue(
            resetState.currentDice.all { it.canBeHeld && !it.isScored && !it.isSelected },
            "After newGame(), all dice (1s) should be holdable, not scored, and not selected. Dice state: ${resetState.currentDice.map { "v=${it.value} h=${it.canBeHeld} s=${it.isScored} sel=${it.isSelected}" }}"
        )

        assertTrue(resetState.canRoll, "canRoll should be true after new game.")
        assertFalse(resetState.canBank, "canBank should be false after new game.")
        assertFalse(resetState.isGameOver, "isGameOver should be false after new game.")
        coVerify(atLeast = 1) { settingsRepository.getGameSettings() } // Initial call + call in newGame
    }
}
