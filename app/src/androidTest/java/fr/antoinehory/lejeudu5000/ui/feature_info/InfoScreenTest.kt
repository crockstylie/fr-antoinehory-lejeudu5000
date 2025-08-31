package fr.antoinehory.lejeudu5000.ui.feature_info

import androidx.annotation.StringRes
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import fr.antoinehory.lejeudu5000.R
import fr.antoinehory.lejeudu5000.domain.utils.AppVersionProvider
import fr.antoinehory.lejeudu5000.ui.theme.LeJeuDu5000Theme
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.jupiter.api.DisplayName

/**
 * UI tests for the [InfoScreen] composable.
 * Verifies that the UI elements are displayed correctly based on the [InfoScreenUiState].
 * These tests use JUnit4 with AndroidJUnit4 runner, standard for Compose UI tests.
 */
@RunWith(AndroidJUnit4::class)
class InfoScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockNavController: NavHostController
    private val targetContext by lazy { InstrumentationRegistry.getInstrumentation().targetContext }

    // Test data
    private val testAppVersion = "1.2.3-ui-test"

    private val personalInfoItemsTestData = listOf(
        InfoItemContent(labelResId = R.string.info_developer_label, value = "Test Developer Name", isLink = false, linkUri = null),
        InfoItemContent(labelResId = R.string.info_website_label, value = "test.example.com", isLink = true, linkUri = "https://test.example.com")
    )

    private val socialMediaLinksTestData = listOf(
        SocialMediaLinkContent(iconResId = R.drawable.ic_linkedin_logo, contentDescriptionResId = R.string.info_cd_linkedin, url = "https://linkedin.com/in/test"),
        SocialMediaLinkContent(iconResId = R.drawable.ic_behance_logo, contentDescriptionResId = R.string.info_cd_behance, url = "https://behance.net/test-profile")
    )

    private val baseMockUiState = InfoScreenUiState(
        personalInfoItems = personalInfoItemsTestData,
        socialMediaLinks = socialMediaLinksTestData,
        appVersion = testAppVersion
    )

    // Dummy AppVersionProvider for the mock ViewModel, its return value is not directly
    // asserted when we control the full UiState, but needed for ViewModel construction.
    private val dummyAppVersionProvider: AppVersionProvider = mockk {
        every { getVersionName() } returns "dummy-preview-version"
    }

    /**
     * Sets up the Compose content for the test.
     * @param uiState The [InfoScreenUiState] to be used by the [InfoScreen].
     */
    private fun launchInfoScreenWithState(uiState: InfoScreenUiState) {
        composeTestRule.setContent {
            mockNavController = rememberNavController() // Initialize NavController within Composable
            LeJeuDu5000Theme {
                // Create a mock ViewModel that provides the desired uiState
                val mockViewModel = object : InfoViewModel(dummyAppVersionProvider) {
                    override val uiState: StateFlow<InfoScreenUiState> = MutableStateFlow(uiState)
                }
                InfoScreen(navController = mockNavController, viewModel = mockViewModel)
            }
        }
    }

    @Test
    @DisplayName("Given InfoScreen, then the screen title should be displayed correctly.")
    fun screenTitleIsDisplayed() {
        launchInfoScreenWithState(baseMockUiState)
        val expectedTitle = targetContext.getString(R.string.info_screen_title)
        composeTestRule.onNodeWithText(expectedTitle).assertIsDisplayed()
    }

    @Test
    @DisplayName("Given InfoScreen with data, then personal information items should be displayed.")
    fun personalInfoItemsAreDisplayed() {
        launchInfoScreenWithState(baseMockUiState)

        personalInfoItemsTestData.forEach { item ->
            val rawLabel = targetContext.getString(item.labelResId)
            // CORRECTION: The InfoItemDisplay composable formats the label with " :" at the end.
            val displayedLabel = "$rawLabel :"
            val value = item.value

            composeTestRule.onNodeWithText(displayedLabel).assertIsDisplayed()
            composeTestRule.onNodeWithText(value).assertIsDisplayed()
        }
    }

    @Test
    @DisplayName("Given InfoScreen with a clickable personal info item, then it should have click action.")
    fun personalInfoItemLinkHasClickAction() {
        val uiStateWithLink = baseMockUiState.copy(
            personalInfoItems = listOf(
                InfoItemContent(labelResId = R.string.info_website_label, value = "clickable.example.com", isLink = true, linkUri = "https://clickable.example.com")
            )
        )
        launchInfoScreenWithState(uiStateWithLink)

        composeTestRule.onNodeWithText("clickable.example.com")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    @DisplayName("Given InfoScreen with data, then social media links should be displayed and have click actions.")
    fun socialMediaLinksAreDisplayedAndClickable() {
        launchInfoScreenWithState(baseMockUiState)

        socialMediaLinksTestData.forEach { link ->
            val contentDescription = targetContext.getString(link.contentDescriptionResId)
            composeTestRule.onNodeWithContentDescription(contentDescription)
                .assertIsDisplayed()
                .assertHasClickAction()
        }
    }

    @Test
    @DisplayName("Given InfoScreen with data, then the app version should be displayed correctly.")
    fun appVersionIsDisplayed() {
        launchInfoScreenWithState(baseMockUiState)
        val expectedVersionText = targetContext.getString(R.string.info_app_version_label, testAppVersion)
        composeTestRule.onNodeWithText(expectedVersionText).assertIsDisplayed()
    }

    @Test
    @DisplayName("Given InfoScreen with empty lists, then it should display gracefully without items.")
    fun screenWithEmptyListsDisplaysGracefully() {
        val emptyUiState = InfoScreenUiState(
            personalInfoItems = emptyList(),
            socialMediaLinks = emptyList(),
            appVersion = "0.0.0-empty"
        )
        launchInfoScreenWithState(emptyUiState)

        // Verify title and app version are still there
        val expectedTitle = targetContext.getString(R.string.info_screen_title)
        composeTestRule.onNodeWithText(expectedTitle).assertIsDisplayed()
        val expectedVersionText = targetContext.getString(R.string.info_app_version_label, "0.0.0-empty")
        composeTestRule.onNodeWithText(expectedVersionText).assertIsDisplayed()

        // Verify that items from the non-empty list are not present
        personalInfoItemsTestData.forEach { item ->
            // Check for value as label might be generic
            val rawLabel = targetContext.getString(item.labelResId)
            val displayedLabel = "$rawLabel :"
            composeTestRule.onNodeWithText(displayedLabel).assertDoesNotExist()
            composeTestRule.onNodeWithText(item.value).assertDoesNotExist()
        }
        socialMediaLinksTestData.forEach { link ->
            val contentDescription = targetContext.getString(link.contentDescriptionResId)
            composeTestRule.onNodeWithContentDescription(contentDescription).assertDoesNotExist()
        }
    }
}
