package fr.antoinehory.lejeudu5000.ui.common

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.antoinehory.lejeudu5000.ui.feature_game.DiceUi // DiceUi now uses UUID for id
import fr.antoinehory.lejeudu5000.ui.theme.LeJeuDu5000Theme
import java.util.UUID // Import UUID

/**
 * Displays a single die.
 * KDoc in English as requested.
 *
 * @param diceUi The UI state of the die to display.
 * @param onClick Lambda to be invoked when the die is clicked.
 * @param modifier Modifier for this composable.
 */
@Composable
fun DiceView(
    diceUi: DiceUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = when {
        diceUi.isScored -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        diceUi.isSelected -> MaterialTheme.colorScheme.primary
        diceUi.canBeHeld -> MaterialTheme.colorScheme.secondary
        else -> Color.Transparent
    }
    val backgroundColor = when {
        diceUi.isScored -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Surface(
        modifier = modifier
            .size(64.dp)
            .aspectRatio(1f)
            .border(
                width = 2.dp,
                color = borderColor,
                shape = MaterialTheme.shapes.medium
            )
            .padding(4.dp)
            .clickable(
                enabled = diceUi.canBeHeld && !diceUi.isScored,
                onClick = onClick
            ),
        shape = MaterialTheme.shapes.medium,
        color = backgroundColor,
        shadowElevation = 2.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxHeight()
        ) {
            Text(
                text = diceUi.value.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (diceUi.isScored) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Displays a row of dice.
 * KDoc in English as requested.
 *
 * @param diceList The list of DiceUi states to display.
 * @param onDiceClick Lambda to be invoked when a die is clicked, providing the die's ID (UUID).
 * @param modifier Modifier for this composable.
 */
@Composable
fun DiceRow(
    diceList: List<DiceUi>,
    onDiceClick: (UUID) -> Unit, // Changed from (Int) -> Unit
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        items(diceList, key = { it.id }) { diceUi -> // diceUi is the correct name here
            DiceView(
                diceUi = diceUi,
                onClick = { onDiceClick(diceUi.id) } // Pass diceUi.id which is UUID
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DiceViewPreview_Selectable() {
    LeJeuDu5000Theme {
        DiceView(
            diceUi = DiceUi(id = UUID.randomUUID(), value = 5, isSelected = false, canBeHeld = true, isScored = false),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DiceViewPreview_Selected() {
    LeJeuDu5000Theme {
        DiceView(
            diceUi = DiceUi(id = UUID.randomUUID(), value = 1, isSelected = true, canBeHeld = true, isScored = false),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DiceViewPreview_Scored() {
    LeJeuDu5000Theme {
        DiceView(
            diceUi = DiceUi(id = UUID.randomUUID(), value = 3, isSelected = false, canBeHeld = false, isScored = true),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DiceViewPreview_NotHoldable() {
    LeJeuDu5000Theme {
        DiceView(
            diceUi = DiceUi(id = UUID.randomUUID(), value = 2, isSelected = false, canBeHeld = false, isScored = false),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DiceRowPreview() {
    LeJeuDu5000Theme {
        DiceRow(
            diceList = listOf(
                DiceUi(id = UUID.randomUUID(), value = 1, isSelected = true, canBeHeld = true, isScored = false),
                DiceUi(id = UUID.randomUUID(), value = 5, isSelected = false, canBeHeld = true, isScored = false),
                DiceUi(id = UUID.randomUUID(), value = 2, isSelected = false, canBeHeld = false, isScored = false),
                DiceUi(id = UUID.randomUUID(), value = 3, isSelected = false, canBeHeld = true, isScored = true),
                DiceUi(id = UUID.randomUUID(), value = 4, isSelected = false, canBeHeld = false, isScored = false)
            ),
            onDiceClick = {} // Preview onDiceClick doesn't use the ID, so this is fine
        )
    }
}
