package fr.antoinehory.lejeudu5000.ui.feature_game

import fr.antoinehory.lejeudu5000.data.repository.SettingsRepository
import fr.antoinehory.lejeudu5000.domain.GameEngine
import fr.antoinehory.lejeudu5000.domain.model.Dice // Domain model
import fr.antoinehory.lejeudu5000.domain.model.GameSettings
import fr.antoinehory.lejeudu5000.domain.model.GameState
import fr.antoinehory.lejeudu5000.domain.model.Player
import fr.antoinehory.lejeudu5000.domain.model.TurnData
import fr.antoinehory.lejeudu5000.domain.usecase.FinalizeTurnUseCase
import io.mockk.coEvery
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
 * Unit tests for the "keep selected dice" functionality of [GameViewModel].
 * KDoc in English.
 */
@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class GameViewModelKeepSelectedDiceTest {

    @MockK
    private lateinit var gameEngine: GameEngine

    @MockK
    private lateinit var settingsRepository: SettingsRepository

    @MockK
    private lateinit var finalizeTurnUseCase: FinalizeTurnUseCase

    private lateinit var viewModel: GameViewModel

    private val testDispatcher = StandardTestDispatcher()
    private val defaultPlayerUuid: UUID = UUID.randomUUID()
    private val defaultPlayer = Player(id = defaultPlayerUuid, name = "Player 1", totalScore = 1000)
    private val defaultGameSettings = GameSettings() // Default opening threshold is 500

    /**
     * Helper function to create a domain Dice instance with a specific UUID.
     * @param value The face value of the die.
     * @param isAvailable Whether the die is available to be rolled or kept.
     * @param id The unique identifier for the die.
     * @return A [Dice] instance.
     */
    private fun createTestDomainDice(value: Int, isAvailable: Boolean = true, id: UUID = UUID.randomUUID()): Dice =
        Dice(id = id, value = value, isAvailable = isAvailable, isSelected = false)

    /**
     * Helper function to create a list of domain Dice instances with UUIDs.
     * @param values The face values of the dice to create.
     * @return A list of [Dice] instances.
     */
    private fun createTestDomainDiceList(vararg values: Int): List<Dice> =
        values.map { createTestDomainDice(value = it, isAvailable = true) }

    private val defaultInitialDomainDice: List<Dice> = createTestDomainDiceList(1,1,1,1,1) // All 1s for simplicity in default state

    private val defaultTurnData = TurnData(diceOnTable = defaultInitialDomainDice, currentTurnScore = 0)

    private val defaultGameState = GameState(
        players = listOf(defaultPlayer),
        currentPlayerIndex = 0,
        turnData = defaultTurnData,
        isGameOver = false
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { settingsRepository.getGameSettings() } returns flowOf(defaultGameSettings)
        every { gameEngine.calculateScore(any()) } returns 0 // Default behavior, overridden in specific tests
        // Default rollDice mock, can be overridden in tests for specific dice outcomes
        coEvery { gameEngine.rollDice(any()) } returns defaultGameState.copy(
            turnData = defaultGameState.turnData.copy(diceOnTable = createTestDomainDiceList(1, 2, 3, 4, 5))
        )
        coEvery { finalizeTurnUseCase.invoke(any(), any()) } returns defaultGameState

        viewModel = GameViewModel(
            gameEngine = gameEngine,
            settingsRepository = settingsRepository,
            finalizeTurnUseCase = finalizeTurnUseCase
        )
        // Initialize the ViewModel's state by collecting the initial UiState
        // This helps ensure that the uiState is populated before tests run.
        testDispatcher.scheduler.runCurrent() // Process initial state mapping
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `keepSelectedDiceAndContinueTurn when selectedDiceScore is 0 does nothing and updates message`() = runTest {
        val diceAfterRoll = createTestDomainDiceList(2, 3, 4, 6, 6) // Non-scoring dice
        val gameStateAfterRoll = defaultGameState.copy(
            turnData = defaultTurnData.copy(diceOnTable = diceAfterRoll, currentTurnScore = 0)
        )
        coEvery { gameEngine.rollDice(any()) } returns gameStateAfterRoll
        every { gameEngine.calculateScore(diceAfterRoll) } returns 0 // Explicitly 0 for these dice

        viewModel.rollDice()
        testDispatcher.scheduler.advanceUntilIdle()

        val initialUiState = viewModel.uiState.value
        assertEquals(0, initialUiState.selectedDiceScore, "Pre-condition: selectedDiceScore should be 0 after bust.")

        viewModel.keepSelectedDiceAndContinueTurn()
        testDispatcher.scheduler.advanceUntilIdle()

        val finalState = viewModel.uiState.value
        assertEquals(initialUiState.turnScore, finalState.turnScore, "Turn score should not change on invalid keep.")
        assertEquals(0, finalState.selectedDiceScore, "Selected dice score should remain 0.")
        assertTrue(
            finalState.gameMessage.contains("Invalid action", ignoreCase = true) ||
            finalState.gameMessage.contains("score of 0", ignoreCase = true) ||
            finalState.gameMessage.contains("Invalid selection", ignoreCase = true),
            "Game message issue. Expected message about invalid keep or zero score. Was: '''${finalState.gameMessage}'''"
        )
    }

    @Test
    fun `keepSelectedDiceAndContinueTurn successfully keeps score and updates dice`() = runTest {
        val diceRolled = createTestDomainDiceList(1, 5, 2, 3, 4) // 1 scores 100, 5 scores 50
        val domainStateAfterRoll = defaultGameState.copy(
            turnData = defaultTurnData.copy(diceOnTable = diceRolled)
        )
        coEvery { gameEngine.rollDice(any()) } returns domainStateAfterRoll

        every { gameEngine.calculateScore(match { it.size == 1 && it.first().value == 1 }) } returns 100
        every { gameEngine.calculateScore(match { it.size == 1 && it.first().value == 5 }) } returns 50
        every { gameEngine.calculateScore(match { list -> list.size == 2 && list.any { it.value == 1 } && list.any { it.value == 5 } }) } returns 150
        every { gameEngine.calculateScore(diceRolled) } returns 150 // For potentialScoreAfterRoll

        viewModel.rollDice()
        testDispatcher.scheduler.advanceUntilIdle()
        // Player is already open (totalScore = 1000 in defaultPlayer)

        val diceUi1Id = viewModel.uiState.value.currentDice.first { it.value == 1 }.id
        val diceUi5Id = viewModel.uiState.value.currentDice.first { it.value == 5 }.id

        viewModel.selectDice(diceUi1Id)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.selectDice(diceUi5Id)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(150, viewModel.uiState.value.selectedDiceScore, "Selected dice score mismatch.")

        viewModel.keepSelectedDiceAndContinueTurn()
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedState = viewModel.uiState.value
        assertEquals(150, updatedState.turnScore, "Turn score not updated.")
        assertEquals(0, updatedState.selectedDiceScore, "Selected dice score not reset.")
        assertTrue(updatedState.currentDice.first { it.id == diceUi1Id }.isScored, "Die '1' not marked as scored.")
        assertTrue(updatedState.currentDice.first { it.id == diceUi5Id }.isScored, "Die '5' not marked as scored.")
        assertFalse(updatedState.currentDice.first { it.value == 2 }.isScored, "Die '2' marked as scored incorrectly.")
        assertTrue(updatedState.canRoll, "Should be able to roll with remaining dice.")
        assertTrue(updatedState.canBank, "Should be able to bank as player is open and turnScore > 0.")
    }

    @Test
    fun `keepSelectedDiceAndContinueTurn when all dice are scored allows re-roll of all dice`() = runTest {
        val allScoringDomainDice = createTestDomainDiceList(1, 1, 1, 5, 5) // Expected score = 1000 + 50 + 50 = 1100
        val scoreForAllDice = 1100
        val domainStateAfterRoll = defaultGameState.copy(
            turnData = defaultTurnData.copy(diceOnTable = allScoringDomainDice, currentTurnScore = 0)
        )
        // This mock is for the initial roll in viewModel.rollDice()
        coEvery { gameEngine.rollDice(any()) } returns domainStateAfterRoll

        // Mocks for calculateScore during selection phase
        every { gameEngine.calculateScore(match { it.size == 1 && it.first().value == 1 }) } returns 100
        every { gameEngine.calculateScore(match { it.size == 1 && it.first().value == 5 }) } returns 50
        every { gameEngine.calculateScore(match { it.size == 2 && it.count { d -> d.value == 1 } == 2 }) } returns 200 // e.g. (1,1)
        every { gameEngine.calculateScore(match { it.size == 2 && it.count { d -> d.value == 5 } == 2 }) } returns 100 // e.g. (5,5)
        every { gameEngine.calculateScore(match { it.size == 3 && it.count { d -> d.value == 1 } == 3 }) } returns 1000 // (1,1,1)
        // (1,1,1,5)
        every { gameEngine.calculateScore(match { list -> list.size == 4 && list.count { it.value == 1 } == 3 && list.count { it.value == 5 } == 1 }) } returns 1050
        // (1,1,1,5,5) - This is the crucial mock for the assertion that fails
        every {
            gameEngine.calculateScore(match { list ->
                list.size == 5 &&
                list.count { it.value == 1 } == 3 &&
                list.count { it.value == 5 } == 2
            })
        } returns scoreForAllDice


        viewModel.rollDice() // This sets up uiState.currentDice based on domainStateAfterRoll
        testDispatcher.scheduler.advanceUntilIdle()

        // Select all dice one by one by their UUIDs from the UI state
        // The uiState.currentDice should now reflect the DiceUi versions of allScoringDomainDice
        val diceToSelect = viewModel.uiState.value.currentDice.toList() // Take a snapshot
        diceToSelect.forEach { uiDie ->
            viewModel.selectDice(uiDie.id)
            testDispatcher.scheduler.advanceUntilIdle()
        }

        // Assertion that is failing:
        assertEquals(scoreForAllDice, viewModel.uiState.value.selectedDiceScore, "All dice selected score mismatch.")

        // Mock for the gameEngine.rollDice call within keepSelectedDiceAndContinueTurn (when all dice are used)
        val fiveNewDiceAfterReRoll = createTestDomainDiceList(1,1,5,1,5) // Use dice that score individually
        val stateAfterReRollAll = domainStateAfterRoll.copy( // Base on previous state
            turnData = domainStateAfterRoll.turnData.copy(
                diceOnTable = fiveNewDiceAfterReRoll,
                currentTurnScore = scoreForAllDice // This score should be carried over
            )
        )
        coEvery {
            gameEngine.rollDice(match { gameState ->
                // Check if this roll is happening after all dice were kept (now unavailable)
                // and the turn score reflects the score of those kept dice.
                gameState.turnData.diceOnTable.all { !it.isAvailable } &&
                gameState.turnData.currentTurnScore == scoreForAllDice
            })
        } returns stateAfterReRollAll

        viewModel.keepSelectedDiceAndContinueTurn()
        testDispatcher.scheduler.advanceUntilIdle()

        val finalState = viewModel.uiState.value
        assertEquals(scoreForAllDice, finalState.turnScore, "Turn score after keeping all dice and re-rolling mismatch.")
        assertEquals(0, finalState.selectedDiceScore, "Selected dice score should be reset after keep.")
        assertEquals(5, finalState.currentDice.size, "Should have 5 new dice after re-roll.")
        assertTrue(finalState.currentDice.all { it.canBeHeld && !it.isScored && !it.isSelected }, "New dice are not all available for selection.")
        assertTrue(finalState.canRoll, "Should be able to roll new dice.")
        assertTrue(finalState.canBank, "Should be able to bank accumulated score.")
    }
}
