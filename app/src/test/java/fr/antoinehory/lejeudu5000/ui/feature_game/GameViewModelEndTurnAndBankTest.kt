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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.UUID

/**
 * Unit tests for the end turn and bank score functionalities of [GameViewModel].
 */
@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class GameViewModelEndTurnAndBankTest {

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
        every { gameEngine.calculateScore(any()) } returns 0 // Default, override in tests
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
     * Tests that banking score when player is opened and turn score is valid invokes use case.
     */
    @Test
    fun `endTurnAndBankAccumulatedScore when player opened and turn score is valid`() = runTest {
        val scoreToBank = 200
        val playerOpened = defaultPlayer.copy(totalScore = 600)
        val initialGameState = defaultGameState.copy(
            players = listOf(playerOpened),
            turnData = defaultTurnData.copy(diceOnTable = createTestDiceList(1, 1, 2, 3, 4))
        )
        coEvery { gameEngine.rollDice(any()) } returns initialGameState
        every { gameEngine.calculateScore(match { it.size == 1 && it.first().value == 1 }) } returns 100
        every { gameEngine.calculateScore(match { it.size == 2 && it.count { d -> d.value == 1 } == 2 }) } returns scoreToBank

        viewModel.rollDice()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.selectDice(viewModel.uiState.value.currentDice.first { it.value == 1 }.id)
        viewModel.selectDice(viewModel.uiState.value.currentDice.filter { it.value == 1 }[1].id)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.keepSelectedDiceAndContinueTurn()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(scoreToBank, viewModel.uiState.value.turnScore)

        val expectedStateAfterBank = initialGameState.copy(
            players = listOf(playerOpened.copy(totalScore = playerOpened.totalScore + scoreToBank)),
            turnData = TurnData()
        )
        coEvery { finalizeTurnUseCase.invoke(any(), eq(defaultGameSettings)) } returns expectedStateAfterBank

        viewModel.endTurnAndBankAccumulatedScore()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { finalizeTurnUseCase.invoke(any(), eq(defaultGameSettings)) }
        assertEquals(expectedStateAfterBank.players.first().totalScore, viewModel.uiState.value.totalScore)
        assertEquals(0, viewModel.uiState.value.turnScore)
    }

    /**
     * Tests that banking below opening threshold when not opened fails appropriately.
     */
    @Test
    fun `endTurnAndBankAccumulatedScore when not opened and score is below threshold`() = runTest {
        val settings = defaultGameSettings.copy(openingScoreThreshold = 500)
        coEvery { settingsRepository.getGameSettings() } returns flowOf(settings)
        viewModel = GameViewModel(gameEngine, settingsRepository, finalizeTurnUseCase) // Re-init
        testDispatcher.scheduler.advanceUntilIdle()

        val scoreToAttempt = 200
        val roll = createTestDiceList(1, 1, 2, 3, 4)
        val stateAfterRoll = defaultGameState.copy(turnData = defaultTurnData.copy(diceOnTable = roll))
        coEvery { gameEngine.rollDice(any()) } returns stateAfterRoll
        every { gameEngine.calculateScore(match { it.size == 1 && it.first().value == 1 }) } returns 100
        every { gameEngine.calculateScore(match { it.size == 2 && it.count { d -> d.value == 1 } == 2 }) } returns scoreToAttempt

        viewModel.rollDice()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.selectDice(viewModel.uiState.value.currentDice.first { it.value == 1 }.id)
        viewModel.selectDice(viewModel.uiState.value.currentDice.filter { it.value == 1 }[1].id)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.keepSelectedDiceAndContinueTurn()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(scoreToAttempt, viewModel.uiState.value.turnScore)

        val stateAfterFailedOpen = defaultGameState.copy(turnData = TurnData())
        coEvery { finalizeTurnUseCase.invoke(any(), eq(settings)) } returns stateAfterFailedOpen

        viewModel.endTurnAndBankAccumulatedScore()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.totalScore)
        assertTrue(viewModel.uiState.value.gameMessage.contains("open", ignoreCase = true))
    }

    /**
     * Tests successful opening and banking when score meets threshold.
     */
    @Test
    fun `endTurnAndBankAccumulatedScore when not opened and score meets threshold`() = runTest {
        val settings = defaultGameSettings.copy(openingScoreThreshold = 500)
        coEvery { settingsRepository.getGameSettings() } returns flowOf(settings)
        viewModel = GameViewModel(gameEngine, settingsRepository, finalizeTurnUseCase) // Re-init
        testDispatcher.scheduler.advanceUntilIdle()

        val scoreToOpen = 1000
        val roll = createTestDiceList(1, 1, 1, 2, 3) // Three 1s = 1000
        val stateAfterRoll = defaultGameState.copy(turnData = defaultTurnData.copy(diceOnTable = roll))
        coEvery { gameEngine.rollDice(any()) } returns stateAfterRoll
        every { gameEngine.calculateScore(match { it.size == 1 && it.first().value == 1 }) } returns 100
        every { gameEngine.calculateScore(match { it.size == 2 && it.count { d -> d.value == 1 } == 2 }) } returns 200
        every { gameEngine.calculateScore(match { it.size == 3 && it.count { d -> d.value == 1 } == 3 }) } returns scoreToOpen

        viewModel.rollDice()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.selectDice(viewModel.uiState.value.currentDice.filter { it.value == 1 }[0].id)
        viewModel.selectDice(viewModel.uiState.value.currentDice.filter { it.value == 1 }[1].id)
        viewModel.selectDice(viewModel.uiState.value.currentDice.filter { it.value == 1 }[2].id)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(scoreToOpen, viewModel.uiState.value.selectedDiceScore, "Selected score mismatch.")
        viewModel.keepSelectedDiceAndContinueTurn()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(scoreToOpen, viewModel.uiState.value.turnScore, "Turn score mismatch before banking.")

        val stateAfterOpen = defaultGameState.copy(
            players = listOf(defaultPlayer.copy(totalScore = scoreToOpen)),
            turnData = TurnData()
        )
        coEvery { finalizeTurnUseCase.invoke(any(), eq(settings)) } returns stateAfterOpen

        viewModel.endTurnAndBankAccumulatedScore()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(scoreToOpen, viewModel.uiState.value.totalScore, "Total score mismatch.")
        assertEquals(0, viewModel.uiState.value.turnScore, "Turn score not reset.")
    }
}
