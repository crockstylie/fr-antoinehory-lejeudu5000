package fr.antoinehory.lejeudu5000.ui.feature_game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
// import androidx.compose.ui.graphics.Color // Not used directly, MaterialTheme provides colors
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import fr.antoinehory.lejeudu5000.R
import fr.antoinehory.lejeudu5000.ui.common.CommonTopAppBar
import fr.antoinehory.lejeudu5000.ui.common.DiceRow
import fr.antoinehory.lejeudu5000.ui.common.ScoreBoard
import fr.antoinehory.lejeudu5000.ui.theme.LeJeuDu5000Theme
import java.util.UUID // Import UUID

/**
 * Composable function for the Game screen.
 * This is where the main gameplay takes place.
 * KDoc in English as requested.
 *
 * @param navController The NavController used for navigation.
 * @param viewModel The GameViewModel, injected by Hilt.
 */
@OptIn(ExperimentalMaterial3Api::class) // Required for Scaffold and TopAppBar
@Composable
fun GameScreen(
    navController: NavController,
    viewModel: GameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = stringResource(id = R.string.game_screen_title_with_player, uiState.activePlayerName),
                navController = navController,
                showBackButton = true
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ScoreBoard(uiState = uiState)

            Spacer(modifier = Modifier.height(16.dp))

            val messageColor = if (uiState.gameMessage.startsWith("Bust!") || uiState.gameMessage.startsWith("Dommage !")) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface // Or specific color from theme
            }
            Text(
                text = uiState.gameMessage,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = messageColor,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Spacer to push dice and buttons towards the bottom if content is not filling the screen
            Spacer(modifier = Modifier.weight(1f))

            DiceRow(
                diceList = uiState.currentDice,
                onDiceClick = { diceId -> viewModel.selectDice(diceId) }, // diceId is now UUID
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { viewModel.rollDice() },
                    enabled = uiState.canRoll && !uiState.isGameOver,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.action_roll_dice))
                }

                if (uiState.selectedDiceScore > 0 && !uiState.isGameOver) {
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    ElevatedButton(
                        onClick = { viewModel.keepSelectedDiceAndContinueTurn() },
                        enabled = !uiState.isGameOver,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.action_keep_selected_score))
                    }
                }
            }
            Button(
                onClick = { viewModel.endTurnAndBankAccumulatedScore() },
                enabled = uiState.canBank && !uiState.isGameOver,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.action_bank_total_turn_score))
            }

            if (uiState.isGameOver) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.game_over_message),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { viewModel.newGame() }) {
                    Text(stringResource(R.string.action_new_game))
                }
            }
            Spacer(modifier = Modifier.height(16.dp)) // Bottom padding
        }
    }
}


@Preview(showBackground = true, name = "Game Screen - Default State")
@Composable
fun GameScreenPreview_Default() {
    LeJeuDu5000Theme {
        val previewUiState = GameUiState(
            activePlayerName = "Player 1",
            currentDice = listOf(
                DiceUi(id = UUID.randomUUID(), value = 1, isSelected = false, canBeHeld = true, isScored = false),
                DiceUi(id = UUID.randomUUID(), value = 5, isSelected = false, canBeHeld = true, isScored = false),
                DiceUi(id = UUID.randomUUID(), value = 2, isSelected = false, canBeHeld = false, isScored = false),
                DiceUi(id = UUID.randomUUID(), value = 3, isSelected = false, canBeHeld = false, isScored = false),
                DiceUi(id = UUID.randomUUID(), value = 4, isSelected = false, canBeHeld = false, isScored = false)
            ),
            canRoll = true,
            canBank = false,
            turnScore = 0,
            totalScore = 0,
            selectedDiceScore = 0,
            potentialScoreAfterRoll = 150,
            gameMessage = "Welcome! Roll the dice.",
            isGameOver = false
        )
        val navController = rememberNavController()
        Scaffold(
            topBar = {
                CommonTopAppBar(
                    title = stringResource(id = R.string.game_screen_title_with_player, previewUiState.activePlayerName),
                    navController = navController,
                    showBackButton = true
                )
            }
        ) {
            Column(modifier = Modifier.padding(it).padding(16.dp)) {
                ScoreBoard(uiState = previewUiState)
                Text(previewUiState.gameMessage)
                DiceRow(diceList = previewUiState.currentDice, onDiceClick = {})
                Button(onClick = {}, enabled = previewUiState.canRoll) { Text("Roll Dice") }
                Button(onClick = {}, enabled = previewUiState.canBank) { Text("Bank Total Turn Score") }
            }
        }
    }
}

@Preview(showBackground = true, name = "Game Screen - Bust State")
@Composable
fun GameScreenPreview_Bust() {
    LeJeuDu5000Theme {
        val bustState = GameUiState(
            activePlayerName = "Player 1",
            currentDice = listOf(
                DiceUi(id = UUID.randomUUID(), value = 2, isSelected = false, canBeHeld = false, isScored = true),
                DiceUi(id = UUID.randomUUID(), value = 3, isSelected = false, canBeHeld = false, isScored = true),
                DiceUi(id = UUID.randomUUID(), value = 4, isSelected = false, canBeHeld = false, isScored = true),
                DiceUi(id = UUID.randomUUID(), value = 6, isSelected = false, canBeHeld = false, isScored = true),
                DiceUi(id = UUID.randomUUID(), value = 2, isSelected = false, canBeHeld = false, isScored = true)
            ),
            canRoll = false,
            canBank = false,
            turnScore = 0,
            totalScore = 1200,
            selectedDiceScore = 0,
            potentialScoreAfterRoll = 0,
            gameMessage = "Bust! No scoring dice. Turn ends.",
            isGameOver = false
        )
        val navController = rememberNavController()
        Scaffold(
            topBar = {
                CommonTopAppBar(
                    title = stringResource(id = R.string.game_screen_title_with_player, bustState.activePlayerName),
                    navController = navController,
                    showBackButton = true
                )
            }
        ) {
            Column(modifier = Modifier.padding(it).padding(16.dp)) {
                ScoreBoard(uiState = bustState)
                Text(bustState.gameMessage, color = MaterialTheme.colorScheme.error)
                DiceRow(diceList = bustState.currentDice, onDiceClick = {})
                Button(onClick = {}, enabled = bustState.canRoll) { Text("Roll Dice") }
                Button(onClick = {}, enabled = bustState.canBank) { Text("Bank Total Turn Score") }
            }
        }
    }
}

