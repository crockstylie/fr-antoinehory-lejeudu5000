package fr.antoinehory.lejeudu5000.ui.feature_settings

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import fr.antoinehory.lejeudu5000.R
import fr.antoinehory.lejeudu5000.domain.model.GameSettings
import fr.antoinehory.lejeudu5000.ui.theme.LeJeuDu5000Theme
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

// Import explicite pour hasText pour éviter toute ambiguïté
import androidx.compose.ui.test.hasText

/**
 * Test suite for the [SettingsScreen].
 * This class tests UI interactions and ViewModel communication for the settings screen.
 */
@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

    /**
     * Compose test rule for UI testing.
     */
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockViewModel: SettingsViewModel
    private val mutableUiStateFlow = MutableStateFlow(SettingsUiState(isLoading = true))
    private val targetContext by lazy { InstrumentationRegistry.getInstrumentation().targetContext }
    private val initialSettings = GameSettings(
        openingScoreThreshold = 500,
        victoryScore = 5000,
        mustWinOnExactScore = false,
        cancelOpponentScoreOnMatch = false,
        allowFiftyPointScores = false,
        useThreeLivesRule = false,
        allowStealOnPass = false
    )

    /**
     * Sets up the test environment before each test.
     * Initializes the mock ViewModel and sets the content for the Compose test rule.
     */
    @Before
    fun setUp() {
        mockViewModel = mockk(relaxed = true) {
            every { uiState } returns mutableUiStateFlow.asStateFlow()
        }
        composeTestRule.setContent {
            LeJeuDu5000Theme {
                SettingsScreen(navController = rememberNavController(), viewModel = mockViewModel)
            }
        }
    }

    /**
     * Tests if the loading indicator is displayed when the UI state is loading.
     */
    @Test
    fun loadingIndicator_isDisplayed_whenStateIsLoading() {
        mutableUiStateFlow.value = SettingsUiState(isLoading = true)
        composeTestRule.onNodeWithTag("loadingIndicator", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    /**
     * Tests if an error message is displayed when the UI state contains an error.
     */
    @Test
    fun errorMessage_isDisplayed_whenStateHasError() {
        val errorMessage = "Failed to load"
        val fullErrorMessage = targetContext.getString(R.string.settings_error_loading_generic_message, errorMessage)
        mutableUiStateFlow.value = SettingsUiState(isLoading = false, error = errorMessage)
        composeTestRule.onNodeWithText(fullErrorMessage).assertIsDisplayed()
    }

    /**
     * Tests if the main settings content is displayed correctly when the UI state is success.
     * This includes labels, initial values, and the state of a switch.
     */
    @Test
    fun settingsContent_isDisplayed_whenStateIsSuccess() {
        mutableUiStateFlow.value = SettingsUiState(isLoading = false, gameSettings = initialSettings)
        composeTestRule.onNodeWithText(targetContext.getString(R.string.settings_opening_threshold_label)).assertIsDisplayed()
        composeTestRule.onNodeWithText(initialSettings.openingScoreThreshold.toString()).assertIsDisplayed()
        composeTestRule.onNodeWithText(targetContext.getString(R.string.settings_exact_victory_label)).assertIsDisplayed()

        // Assumes the "setting_item_exact_victory_label" testTag is on the container of this specific setting
        val exactVictoryItemTag = "setting_item_${targetContext.resources.getResourceEntryName(R.string.settings_exact_victory_label).replace("settings_","").lowercase(Locale.ROOT)}" // CORRECTION: toLowerCase -> lowercase

        composeTestRule.onNodeWithTag(exactVictoryItemTag)
            .assertExists()
            .onChildren()
            .filterToOne(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch) and isOff())
            .assertExists()
    }

    /**
     * Tests if interacting with the opening threshold dropdown calls the correct ViewModel method.
     */
    @Test
    fun openingThresholdDropdown_interaction_callsViewModel() {
        mutableUiStateFlow.value = SettingsUiState(isLoading = false, gameSettings = initialSettings)
        val newThreshold = 750
        val initialThresholdText = initialSettings.openingScoreThreshold.toString()
        val newThresholdText = newThreshold.toString()
        every { mockViewModel.onOpeningScoreThresholdChanged(newThreshold) } just Runs

        // This assumes dropdowns are identifiable by their current text value
        composeTestRule.onNodeWithText(initialThresholdText, useUnmergedTree = true).performClick()
        composeTestRule.onNodeWithText(newThresholdText, useUnmergedTree = true).performClick() // Assuming the new text appears in a dropdown list

        verify { mockViewModel.onOpeningScoreThresholdChanged(newThreshold) }
    }

    /**
     * Tests if interacting with the victory score dropdown calls the correct ViewModel method.
     */
    @Test
    fun victoryScoreDropdown_interaction_callsViewModel() {
        mutableUiStateFlow.value = SettingsUiState(isLoading = false, gameSettings = initialSettings)
        val newScore = 10000
        val initialScoreText = initialSettings.victoryScore.toString()
        val newScoreText = newScore.toString()
        every { mockViewModel.onVictoryScoreChanged(newScore) } just Runs

        composeTestRule.onNodeWithText(initialScoreText, useUnmergedTree = true).performClick()
        composeTestRule.onNodeWithText(newScoreText, useUnmergedTree = true).performClick()

        verify { mockViewModel.onVictoryScoreChanged(newScore) }
    }

    /**
     * Helper function to test switch interactions.
     * It sets up the UI state, prepares the ViewModel mock, performs a click on the switch,
     * and verifies the ViewModel interaction.
     * This function assumes that the container of each switch setting item has a unique
     * `Modifier.testTag` derived from its label resource ID.
     *
     * @param settingLabelResId The string resource ID for the setting's label.
     * @param initialCheckedState The initial checked state of the switch for the test.
     * @param viewModelAction A lambda to set up the ViewModel mock expectation for the action.
     * @param verification A lambda to verify the ViewModel interaction.
     */
    private fun testSwitchInteraction(
        settingLabelResId: Int,
        initialCheckedState: Boolean,
        viewModelAction: (Boolean) -> Unit,
        verification: () -> Unit
    ) {
        // Construct the test tag based on the label resource ID's entry name
        val itemTestTag = "setting_item_${targetContext.resources.getResourceEntryName(settingLabelResId).replace("settings_","").lowercase(Locale.ROOT)}" // CORRECTION: toLowerCase -> lowercase

        // GIVEN
        val specificSettings = when (settingLabelResId) {
            R.string.settings_exact_victory_label -> initialSettings.copy(mustWinOnExactScore = initialCheckedState)
            R.string.settings_cancel_score_label -> initialSettings.copy(cancelOpponentScoreOnMatch = initialCheckedState)
            R.string.settings_allow_fifty_pts_label -> initialSettings.copy(allowFiftyPointScores = initialCheckedState)
            R.string.settings_three_lives_rule_label -> initialSettings.copy(useThreeLivesRule = initialCheckedState)
            R.string.settings_steal_on_pass_label -> initialSettings.copy(allowStealOnPass = initialCheckedState)
            else -> initialSettings
        }
        mutableUiStateFlow.value = SettingsUiState(isLoading = false, gameSettings = specificSettings)

        viewModelAction.invoke(!initialCheckedState) // Expect the action to change to the opposite state

        // WHEN
        // Find the specific setting item by its testTag, then find the Switch within its children
        // in its CURRENT state (based on initialCheckedState).
        composeTestRule.onNodeWithTag(itemTestTag)
            .onChildren()
            .filterToOne(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch) and (if (initialCheckedState) isOn() else isOff())) // CORRECTION: Logic inverted
            .performClick()

        // THEN
        verification.invoke()
    }

    /**
     * Tests the "Exact Victory Score" switch interaction.
     */
    @Test
    fun exactVictorySwitch_interaction_callsViewModel() {
        testSwitchInteraction(
            settingLabelResId = R.string.settings_exact_victory_label,
            initialCheckedState = false,
            viewModelAction = { newCheckedState -> every { mockViewModel.onMustWinOnExactScoreChanged(newCheckedState) } just Runs },
            verification = { verify { mockViewModel.onMustWinOnExactScoreChanged(true) } }
        )
    }

    /**
     * Tests the "Cancel Opponent's Score" switch interaction.
     */
    @Test
    fun cancelOpponentScoreSwitch_interaction_callsViewModel() {
        testSwitchInteraction(
            settingLabelResId = R.string.settings_cancel_score_label,
            initialCheckedState = false,
            viewModelAction = { newCheckedState -> every { mockViewModel.onCancelOpponentScoreOnMatchChanged(newCheckedState) } just Runs },
            verification = { verify { mockViewModel.onCancelOpponentScoreOnMatchChanged(true) } }
        )
    }

    /**
     * Tests the "Allow Scores of 50" switch interaction.
     */
    @Test
    fun allowFiftyPointsSwitch_interaction_callsViewModel() {
        testSwitchInteraction(
            settingLabelResId = R.string.settings_allow_fifty_pts_label,
            initialCheckedState = false,
            viewModelAction = { newCheckedState -> every { mockViewModel.onAllowFiftyPointScoresChanged(newCheckedState) } just Runs },
            verification = { verify { mockViewModel.onAllowFiftyPointScoresChanged(true) } }
        )
    }

    /**
     * Tests the "Three Lives Rule" switch interaction.
     */
    @Test
    fun threeLivesRuleSwitch_interaction_callsViewModel() {
        testSwitchInteraction(
            settingLabelResId = R.string.settings_three_lives_rule_label,
            initialCheckedState = false,
            viewModelAction = { newCheckedState -> every { mockViewModel.onUseThreeLivesRuleChanged(newCheckedState) } just Runs },
            verification = { verify { mockViewModel.onUseThreeLivesRuleChanged(true) } }
        )
    }

    /**
     * Tests the "Allow Steal on Pass" switch interaction.
     */
    @Test
    fun allowStealOnPassSwitch_interaction_callsViewModel() {
        testSwitchInteraction(
            settingLabelResId = R.string.settings_steal_on_pass_label,
            initialCheckedState = false,
            viewModelAction = { newCheckedState -> every { mockViewModel.onAllowStealOnPassChanged(newCheckedState) } just Runs },
            verification = { verify { mockViewModel.onAllowStealOnPassChanged(true) } }
        )
    }
}
