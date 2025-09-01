package fr.antoinehory.lejeudu5000.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.antoinehory.lejeudu5000.R
import fr.antoinehory.lejeudu5000.ui.feature_game.GameUiState
import fr.antoinehory.lejeudu5000.ui.theme.LeJeuDu5000Theme

/**
 * Composable function to display the scoreboard.
 * It shows the current player's name, total score, and current turn scores.
 *
 * @param uiState The current state of the game UI.
 * @param modifier Modifier for this composable.
 */
@Composable
fun ScoreBoard(
    uiState: GameUiState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.scoreboard_title_player, uiState.activePlayerName),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                ScoreItem(
                    label = stringResource(R.string.scoreboard_total_score),
                    value = uiState.totalScore.toString(),
                    modifier = Modifier.weight(1f)
                )
                ScoreItem(
                    label = stringResource(R.string.scoreboard_turn_score),
                    value = uiState.turnScore.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                ScoreItem(
                    label = stringResource(R.string.scoreboard_selected_dice_score),
                    value = uiState.selectedDiceScore.toString(),
                    modifier = Modifier.weight(1f)
                )
                ScoreItem(
                    label = stringResource(R.string.scoreboard_potential_roll_score),
                    value = uiState.potentialScoreAfterRoll.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Composable for displaying a single score item (label and value).
 *
 * @param label The label for the score.
 * @param value The value of the score.
 * @param modifier Modifier for this composable.
 */
@Composable
private fun ScoreItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Preview(showBackground = true)
@Composable
fun ScoreBoardPreview() {
    LeJeuDu5000Theme {
        ScoreBoard(
            uiState = GameUiState(
                activePlayerName = "Player 1",
                totalScore = 1250,
                turnScore = 300,
                selectedDiceScore = 150,
                potentialScoreAfterRoll = 200
            )
        )
    }
}
