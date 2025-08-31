package fr.antoinehory.lejeudu5000.ui.feature_info

/**
 * Represents the UI state for the InfoScreen.
 *
 * @property personalInfoItems List of personal information items.
 * @property socialMediaLinks List of social media links.
 * @property appVersion String representing the application version.
 */
data class InfoScreenUiState(
    val personalInfoItems: List<InfoItemContent> = emptyList(),
    val socialMediaLinks: List<SocialMediaLinkContent> = emptyList(),
    val appVersion: String = ""
)