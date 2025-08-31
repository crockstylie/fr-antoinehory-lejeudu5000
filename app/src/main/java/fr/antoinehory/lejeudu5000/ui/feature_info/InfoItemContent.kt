package fr.antoinehory.lejeudu5000.ui.feature_info

/**
 * Data class representing an information item to be displayed on the InfoScreen.
 *
 * @property labelResId The string resource ID for the label of the info item.
 * @property value The string value of the info item. Can be a direct string or a resource name if [valueIsResId] is true.
 * @property valueIsResId If true, [value] is treated as a String resource name, otherwise as a direct string.
 * @property isLink Indicates if this item should be treated as a hyperlink.
 * @property linkUri The URI string for the hyperlink if [isLink] is true.
 */
data class InfoItemContent(
    val labelResId: Int,
    val value: String,
    val valueIsResId: Boolean = false,
    val isLink: Boolean = false,
    val linkUri: String? = null
)