package fr.antoinehory.lejeudu5000.ui.feature_info

import fr.antoinehory.lejeudu5000.R
import fr.antoinehory.lejeudu5000.domain.utils.AppVersionProvider
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
// No longer mocking BuildConfig, so remove mockkStatic/unmockkStatic imports if they were only for BuildConfig
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for the [InfoViewModel].
 * This class verifies the correct initialization and exposure of UI state,
 * particularly the application version (via a mocked [AppVersionProvider])
 * and predefined information items.
 */
@DisplayName("InfoViewModel Unit Tests")
class InfoViewModelTest {

    @MockK // Use @MockK annotation for the provider
    private lateinit var mockAppVersionProvider: AppVersionProvider

    private lateinit var viewModel: InfoViewModel

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this) // Initialize mocks annotated with @MockK
    }

    @AfterEach
    fun tearDown() {
        // No specific teardown needed for AppVersionProvider mock if re-initialized each time
        // If we were using mockkStatic for other things, we would unmock them here.
    }

    /**
     * Nested class for tests related to the initial state of [InfoViewModel].
     */
    @Nested
    @DisplayName("Given InfoViewModel is initialized")
    inner class ViewModelInitialization {

        /**
         * Verifies that the appVersion in [InfoScreenUiState] is correctly populated
         * using the injected [AppVersionProvider].
         */
        @Test
        @DisplayName("Then appVersion should be correctly sourced from AppVersionProvider")
        fun `appVersion should be correctly sourced from AppVersionProvider`() {
            // Arrange
            val expectedVersion = "1.2.3-test-from-provider"
            every { mockAppVersionProvider.getVersionName() } returns expectedVersion

            // ViewModel must be instantiated after the mock is set up
            viewModel = InfoViewModel(mockAppVersionProvider)

            // Act
            val actualVersion = viewModel.uiState.value.appVersion

            // Assert
            assertEquals(expectedVersion, actualVersion, "App version should match the one from AppVersionProvider")
        }

        /**
         * Verifies that the list of personal information items in [InfoScreenUiState]
         * is populated with the correct predefined data.
         */
        @Test
        @DisplayName("Then personalInfoItems should contain the correct predefined data")
        fun `personalInfoItems should contain correct predefined data`() {
            // Arrange
            // Mock AppVersionProvider as it's accessed during ViewModel init
            every { mockAppVersionProvider.getVersionName() } returns "any_version"
            viewModel = InfoViewModel(mockAppVersionProvider)

            val expectedNumberOfItems = 4 // Based on current ViewModel implementation
            val expectedFirstItem = InfoItemContent(
                labelResId = R.string.info_developer_label,
                value = "Antoine Crock HORY"
            )
            val expectedLastItem = InfoItemContent(
                labelResId = R.string.info_donate_label,
                value = "paypal.me/kuroku",
                isLink = true,
                linkUri = "https://paypal.me/kuroku"
            )

            // Act
            val personalInfoItems = viewModel.uiState.value.personalInfoItems

            // Assert
            assertEquals(expectedNumberOfItems, personalInfoItems.size, "Number of personal info items should match")
            assertTrue(personalInfoItems.contains(expectedFirstItem), "First personal info item should be present and correct")
            assertTrue(personalInfoItems.contains(expectedLastItem), "Last personal info item should be present and correct")
        }

        /**
         * Verifies that the list of social media links in [InfoScreenUiState]
         * is populated with the correct predefined data.
         */
        @Test
        @DisplayName("Then socialMediaLinks should contain the correct predefined data")
        fun `socialMediaLinks should contain correct predefined data`() {
            // Arrange
            // Mock AppVersionProvider as it's accessed during ViewModel init
            every { mockAppVersionProvider.getVersionName() } returns "any_version"
            viewModel = InfoViewModel(mockAppVersionProvider)

            val expectedNumberOfLinks = 6 // Based on current ViewModel implementation
            val expectedFirstLink = SocialMediaLinkContent(
                iconResId = R.drawable.ic_linkedin_logo,
                contentDescriptionResId = R.string.info_cd_linkedin,
                url = "https://www.linkedin.com/in/antoinehory/"
            )
            val expectedLastLink = SocialMediaLinkContent(
                iconResId = R.drawable.ic_steam_logo,
                contentDescriptionResId = R.string.info_cd_steam,
                url = "https://steamcommunity.com/id/crockstylie/"
            )

            // Act
            val socialMediaLinks = viewModel.uiState.value.socialMediaLinks

            // Assert
            assertEquals(expectedNumberOfLinks, socialMediaLinks.size, "Number of social media links should match")
            assertTrue(socialMediaLinks.contains(expectedFirstLink), "First social media link should be present and correct")
            assertTrue(socialMediaLinks.contains(expectedLastLink), "Last social media link should be present and correct")
        }
    }
}
