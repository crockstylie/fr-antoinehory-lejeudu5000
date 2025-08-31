package fr.antoinehory.lejeudu5000.ui.feature_info

/**
 * Data class representing a social media link to be displayed with an icon.
 *
 * @property iconResId The drawable resource ID for the social media icon.
 * @property contentDescriptionResId The string resource ID for the icon's content description.
 * @property url The URL string for the social media profile/link.
 */
data class SocialMediaLinkContent(
    val iconResId: Int,
    val contentDescriptionResId: Int,
    val url: String
)