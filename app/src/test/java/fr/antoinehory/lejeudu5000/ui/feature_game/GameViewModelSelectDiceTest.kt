package fr.antoinehory.lejeudu5000.ui.feature_game

import fr.antoinehory.lejeudu5000.data.repository.SettingsRepository
import fr.antoinehory.lejeudu5000.domain.GameEngine
import fr.antoinehory.lejeudu5000.domain.model.Dice
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
 * Unit tests for the dice selection functionality of [GameViewModel].
 */
@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class GameViewModelSelectDiceTest {

    @MockK
    private lateinit var gameEngine: GameEngine

    @MockK
    private lateinit var settingsRepository: SettingsRepository

    @MockK
    private lateinit var finalizeTurnUseCase: FinalizeTurnUseCase

    private lateinit var viewModel: GameViewModel

    private val testDispatcher = StandardTestDispatcher()
    private val defaultPlayerUuid: UUID = UUID.randomUUID()
    private val defaultPlayer = Player(id = defaultPlayerUuid, name = "Player 1", totalScore = 0)
    private val defaultGameSettings = GameSettings()

    private fun createTestDice(value: Int, isAvailable: Boolean = true, id: UUID = UUID.randomUUID()): Dice =
        Dice(id = id, value = value, isAvailable = isAvailable)

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
        every { gameEngine.calculateScore(any()) } returns 0 // Default
        coEvery { gameEngine.rollDice(any()) } returns defaultGameState // Default roll
        coEvery { finalizeTurnUseCase.invoke(any(), any()) } returns defaultGameState

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
     * Tests that selecting a scoring die correctly updates the UI state,
     * including the selected status of the die and the selected dice score.
     */
    @Test
    fun `selectDice when selecting a scoring die updates uiState correctly`() = runTest {
        val dieIdToSelect = UUID.randomUUID()
        val diceRolled = listOf(
            createTestDice(value = 1, id = dieIdToSelect), // Die '1' scores 100
            createTestDice(value = 2),
            createTestDice(value = 3),
            createTestDice(value = 4),
            createTestDice(value = 6)
        )
        val initialDomainState = defaultGameState.copy(
            turnData = defaultTurnData.copy(diceOnTable = diceRolled)
        )

        coEvery { gameEngine.rollDice(any()) } returns initialDomainState

        // ✅ Mock score for the entire roll (available dice for potentialScoreAfterRoll)
        every {
            gameEngine.calculateScore(match { list ->
                list.size == 5 && list.all { it.isAvailable } && list.any { it.value == 1 }
            })
        } returns 100

        // ✅ Mock score for the individual die '1' (used when it's selected)
        every {
            gameEngine.calculateScore(match { list ->
                list.size == 1 && list.first().value == 1
            })
        } returns 100

        // ✅ Mocks for non-scoring dice (used for canBeHeld logic)
        every { gameEngine.calculateScore(match { list -> list.size == 1 && list.first().value == 2 }) } returns 0
        every { gameEngine.calculateScore(match { list -> list.size == 1 && list.first().value == 3 }) } returns 0
        every { gameEngine.calculateScore(match { list -> list.size == 1 && list.first().value == 4 }) } returns 0
        every { gameEngine.calculateScore(match { list -> list.size == 1 && list.first().value == 6 }) } returns 0

        viewModel.rollDice() // This populates uiState.currentDice based on initialDomainState
        testDispatcher.scheduler.advanceUntilIdle()

        // Ensure the die to select is present in the UI state
        val diceUiToSelect = viewModel.uiState.value.currentDice.firstOrNull { it.id == dieIdToSelect }
        assertTrue(diceUiToSelect != null, "Die to select not found in UI state after roll.")
        assertTrue(diceUiToSelect!!.canBeHeld, "Scoring die '1' should be holdable.")

        viewModel.selectDice(dieIdToSelect)
        testDispatcher.scheduler.advanceUntilIdle()

        val stateAfterSelection = viewModel.uiState.value
        val selectedUiDie = stateAfterSelection.currentDice.first { it.id == dieIdToSelect }
        assertTrue(selectedUiDie.isSelected, "Die '1' should be selected.")
        assertEquals(100, stateAfterSelection.selectedDiceScore, "Selected dice score should be 100.")

        // ✅ CORRECTION : Le joueur n'est pas ouvert (totalScore = 0) et le score sélectionné (100)
        // est inférieur au seuil d'ouverture par défaut (500), donc canBank = false
        val playerIsOpened = defaultPlayer.totalScore >= defaultGameSettings.openingScoreThreshold
        val selectedScoreReachesThreshold = stateAfterSelection.selectedDiceScore >= defaultGameSettings.openingScoreThreshold
        val expectedCanBank = playerIsOpened || selectedScoreReachesThreshold

        assertEquals(expectedCanBank, stateAfterSelection.canBank,
            "canBank should be $expectedCanBank when player opened=$playerIsOpened and selected score=${stateAfterSelection.selectedDiceScore} vs threshold=${defaultGameSettings.openingScoreThreshold}")

        assertTrue(stateAfterSelection.canRoll, "Should be able to roll remaining dice.")
    }

    /**
     * Tests that a die already scored (not available in domain) cannot be selected
     * and does not change the UI state upon attempted selection.
     */
    @Test
    fun `selectDice when die is already scored (not available) should not be selectable`() = runTest {
        // ✅ APPROCHE ALTERNATIVE : Simuler un scénario réaliste
        // 1. Lancer des dés normalement
        // 2. Sélectionner et garder un dé (le rendre isAvailable = false)
        // 3. Tester la sélection du dé gardé

        val diceRolled = listOf(
            createTestDice(value = 1), // Ce dé sera gardé puis tenté d'être re-sélectionné
            createTestDice(value = 2),
            createTestDice(value = 3),
            createTestDice(value = 4),
            createTestDice(value = 6)
        )

        val stateAfterRoll = defaultGameState.copy(
            turnData = defaultTurnData.copy(diceOnTable = diceRolled)
        )

        coEvery { gameEngine.rollDice(any()) } returns stateAfterRoll

        // Mocks pour les scores
        every { gameEngine.calculateScore(match { list -> list.size == 1 && list.first().value == 1 }) } returns 100
        every { gameEngine.calculateScore(match { list -> list.size == 1 && list.first().value == 2 }) } returns 0
        every { gameEngine.calculateScore(match { list -> list.size == 1 && list.first().value == 3 }) } returns 0
        every { gameEngine.calculateScore(match { list -> list.size == 1 && list.first().value == 4 }) } returns 0
        every { gameEngine.calculateScore(match { list -> list.size == 1 && list.first().value == 6 }) } returns 0

        every {
            gameEngine.calculateScore(match { list ->
                list.size == 5 && list.all { it.isAvailable } && list.any { it.value == 1 }
            })
        } returns 100

        // 1. Lancer les dés
        viewModel.rollDice()
        testDispatcher.scheduler.advanceUntilIdle()

        // 2. Identifier et sélectionner le dé "1"
        val dieToSelect = viewModel.uiState.value.currentDice.first { it.value == 1 }
        val selectedDieId = dieToSelect.id

        viewModel.selectDice(selectedDieId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Vérifier que le dé est sélectionné
        assertTrue(viewModel.uiState.value.currentDice.first { it.id == selectedDieId }.isSelected)
        assertEquals(100, viewModel.uiState.value.selectedDiceScore)

        // 3. Garder les dés sélectionnés (cela rendra le dé isAvailable = false)
        val diceAfterKeep = diceRolled.map {
            if (it.id == selectedDieId) it.copy(isAvailable = false) else it
        }

        val stateAfterKeep = stateAfterRoll.copy(
            turnData = stateAfterRoll.turnData.copy(
                diceOnTable = diceAfterKeep,
                currentTurnScore = 100
            )
        )

        // Mock pour keepSelectedDiceAndContinueTurn - pas besoin de rollDice à nouveau
        coEvery { gameEngine.rollDice(any()) } returns stateAfterKeep

        viewModel.keepSelectedDiceAndContinueTurn()
        testDispatcher.scheduler.advanceUntilIdle()

        // 4. Vérifier que le dé est maintenant "scoré" (isScored = true)
        val stateAfterKeeping = viewModel.uiState.value
        val keptDie = stateAfterKeeping.currentDice.first { it.id == selectedDieId }

        assertTrue(keptDie.isScored, "Die should be marked as scored after keeping.")
        assertFalse(keptDie.canBeHeld, "Scored die should not be holdable.")
        assertFalse(keptDie.isSelected, "Die should be deselected after keeping.")

        val initialSelectedScoreAfterKeep = stateAfterKeeping.selectedDiceScore

        // 5. Tenter de sélectionner le dé déjà gardé
        viewModel.selectDice(selectedDieId)
        testDispatcher.scheduler.advanceUntilIdle()

        // 6. Vérifier que rien n'a changé
        val finalState = viewModel.uiState.value
        val attemptedDie = finalState.currentDice.first { it.id == selectedDieId }

        assertFalse(attemptedDie.isSelected, "Already scored die should not become selected.")
        assertTrue(attemptedDie.isScored, "Die should still be marked as scored.")
        assertFalse(attemptedDie.canBeHeld, "Die should still not be holdable.")
        assertEquals(initialSelectedScoreAfterKeep, finalState.selectedDiceScore, "Selected score should not change.")
    }
}