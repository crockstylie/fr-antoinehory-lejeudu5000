package fr.antoinehory.lejeudu5000.ui.feature_info

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.antoinehory.lejeudu5000.R
import fr.antoinehory.lejeudu5000.domain.utils.AppVersionProvider // New import
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * ViewModel for the InfoScreen.
 *
 * This ViewModel is responsible for preparing and managing the data to be displayed on the InfoScreen.
 * It provides a [StateFlow] of [InfoScreenUiState] that the UI can observe for updates.
 * Hilt is used for dependency injection.
 * The application version is retrieved via an injected [AppVersionProvider] for better testability.
 *
 * This class is marked as 'open' to allow for extension in Previews for mocking purposes.
 */
@HiltViewModel
open class InfoViewModel @Inject constructor(
    private val appVersionProvider: AppVersionProvider // Inject AppVersionProvider
) : ViewModel() {

    // Private MutableStateFlow to hold the UI state.
    private val _uiState = MutableStateFlow(
        InfoScreenUiState(
            personalInfoItems = listOf(
                InfoItemContent(
                    labelResId = R.string.info_developer_label,
                    value = "Antoine Crock HORY"
                ),
                InfoItemContent(
                    labelResId = R.string.info_website_label,
                    value = "antoinehory.fr",
                    isLink = true,
                    linkUri = "https://antoinehory.fr"
                ),
                InfoItemContent(
                    labelResId = R.string.info_email_label,
                    value = "contact@antoinehory.fr",
                    isLink = true,
                    linkUri = "mailto:contact@antoinehory.fr"
                ),
                InfoItemContent(
                    labelResId = R.string.info_donate_label,
                    value = "paypal.me/kuroku",
                    isLink = true,
                    linkUri = "https://paypal.me/kuroku"
                )
            ),
            socialMediaLinks = listOf(
                SocialMediaLinkContent(
                    iconResId = R.drawable.ic_linkedin_logo,
                    contentDescriptionResId = R.string.info_cd_linkedin,
                    url = "https://www.linkedin.com/in/antoinehory/"
                ),
                SocialMediaLinkContent(
                    iconResId = R.drawable.ic_behance_logo,
                    contentDescriptionResId = R.string.info_cd_behance,
                    url = "https://www.behance.net/antoine-hory"
                ),
                SocialMediaLinkContent(
                    iconResId = R.drawable.ic_instagram_logo,
                    contentDescriptionResId = R.string.info_cd_instagram,
                    url = "https://www.instagram.com/antoine.hory.web/"
                ),
                SocialMediaLinkContent(
                    iconResId = R.drawable.ic_facebook_logo,
                    contentDescriptionResId = R.string.info_cd_facebook,
                    url = "https://www.facebook.com/antoinehory/"
                ),
                SocialMediaLinkContent(
                    iconResId = R.drawable.ic_spotify_logo,
                    contentDescriptionResId = R.string.info_cd_spotify,
                    url = "https://open.spotify.com/user/crockstylie"
                ),
                SocialMediaLinkContent(
                    iconResId = R.drawable.ic_steam_logo,
                    contentDescriptionResId = R.string.info_cd_steam,
                    url = "https://steamcommunity.com/id/crockstylie/"
                )
            ),
            // Get app version from the injected provider
            appVersion = appVersionProvider.getVersionName()
        )
    )

    /**
     * Publicly exposed [StateFlow] of [InfoScreenUiState] for the UI to observe.
     * This property is marked as 'open' to allow for overriding in Previews for mocking purposes.
     */
    open val uiState: StateFlow<InfoScreenUiState> = _uiState.asStateFlow()
}

