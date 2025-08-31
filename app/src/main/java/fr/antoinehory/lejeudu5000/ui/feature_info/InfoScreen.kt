package fr.antoinehory.lejeudu5000.ui.feature_info

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember // Added for remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext // Added for context
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel // Standard Hilt import for Compose
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import fr.antoinehory.lejeudu5000.R
import fr.antoinehory.lejeudu5000.ui.common.CommonTopAppBar
import fr.antoinehory.lejeudu5000.ui.theme.LeJeuDu5000Theme
import fr.antoinehory.lejeudu5000.domain.utils.AppVersionProvider
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Composable function for the Info screen.
 * It displays information about the application and the developer.
 * This screen is designed to be scrollable and uses Material 3 components.
 *
 * @param navController The NavController used for navigation.
 * @param viewModel The InfoViewModel, injected by Hilt, to provide data for the screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen(
    navController: NavController,
    viewModel: InfoViewModel = hiltViewModel() // Hilt ViewModel injection
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current // Get context for resource lookup

    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = stringResource(id = R.string.info_screen_title),
                navController = navController
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.info_section_personal_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            uiState.personalInfoItems.forEach { infoItem ->
                val resolvedValue = if (infoItem.valueIsResId && infoItem.value.isNotBlank()) {
                    // Get resource ID by name. Returns 0 if not found.
                    val resourceIdByName = remember(infoItem.value) { // Remember based on resource name
                        context.resources.getIdentifier(infoItem.value, "string", context.packageName)
                    }
                    if (resourceIdByName != 0) {
                        stringResource(id = resourceIdByName)
                    } else {
                        infoItem.value // Fallback to value if resource ID lookup fails
                    }
                } else {
                    infoItem.value
                }

                InfoItemDisplay(
                    label = stringResource(id = infoItem.labelResId),
                    value = resolvedValue,
                    isLink = infoItem.isLink,
                    linkUri = infoItem.linkUri
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.info_section_social_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                uiState.socialMediaLinks.forEach { socialLink ->
                    SocialIconLink(
                        iconResId = socialLink.iconResId,
                        contentDescription = stringResource(id = socialLink.contentDescriptionResId),
                        url = socialLink.url
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.appVersion.isNotBlank()) {
                Text(
                    text = stringResource(R.string.info_app_version_label, uiState.appVersion),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }
        }
    }
}

/**
 * A private composable function to display a single information item (label and value).
 * The value can be a hyperlink.
 *
 * @param label The label text for the information item.
 * @param value The value text for the information item.
 * @param modifier Modifier for this composable. Should be the first optional parameter.
 * @param isLink Whether the value should be displayed as a hyperlink. Defaults to false.
 * @param linkUri The URI to open if [isLink] is true and the item is clicked. Defaults to null.
 */
@Composable
private fun InfoItemDisplay(
    label: String,
    value: String,
    modifier: Modifier = Modifier, // Modifier is now the first optional parameter
    isLink: Boolean = false,
    linkUri: String? = null
) {
    val uriHandler = LocalUriHandler.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = "$label :",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))

        val valueStyle = MaterialTheme.typography.bodyMedium
        val valueColor = if (isLink) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

        Text(
            text = value,
            style = valueStyle,
            color = valueColor,
            textDecoration = if (isLink) TextDecoration.Underline else null,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .then(if (isLink && linkUri != null) Modifier.clickable { uriHandler.openUri(linkUri) } else Modifier)
        )
    }
}

/**
 * A private composable function to display a social media icon that acts as a link.
 *
 * @param iconResId The drawable resource ID for the icon.
 * @param contentDescription The accessibility content description for the icon.
 * @param url The URL to open when the icon is clicked.
 * @param modifier Modifier for this composable. Defaults to Modifier.
 */
@Composable
private fun SocialIconLink(
    iconResId: Int,
    contentDescription: String,
    url: String,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    IconButton(
        onClick = { uriHandler.openUri(url) },
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = contentDescription,
            tint = Color.Unspecified, // Use Color.Unspecified for multi-color vector drawables or logos
            modifier = Modifier.size(40.dp)
        )
    }
}

/**
 * Preview function for the InfoScreen.
 * Provides a design-time view of the InfoScreen with mock data.
 */
@Preview(showBackground = true, name = "Info Screen Portrait")
@Composable
fun InfoScreenPreview() {
    LeJeuDu5000Theme {
        val mockAppVersionProvider = object : AppVersionProvider {
            override fun getVersionName(): String = "1.0.0-preview"
        }
        val mockViewModel = object : InfoViewModel(mockAppVersionProvider) { // ViewModel is open, can be extended
            override val uiState = MutableStateFlow( // uiState is open, can be overridden
                InfoScreenUiState(
                    personalInfoItems = listOf(
                        InfoItemContent(R.string.info_developer_label, value = "Preview Developer Name", valueIsResId = false, isLink = false, linkUri = null),
                        InfoItemContent(R.string.info_website_label, value = "developer-preview.com", valueIsResId = false, isLink = true, linkUri = "https://example.com"),
                        InfoItemContent(R.string.info_email_label, value = "contact@developer-preview.com", valueIsResId = false, isLink = true, linkUri = "mailto:example@example.com"),
                        InfoItemContent(R.string.info_donate_label, value = "paypal.me/preview-dev", valueIsResId = false, isLink = true, linkUri = "https://example.com/donate")
                    ),
                    socialMediaLinks = listOf(
                        // For preview, ensure R.drawable.ic_launcher_foreground exists or use other placeholder drawables
                        SocialMediaLinkContent(R.drawable.ic_launcher_foreground, R.string.info_cd_linkedin, "https://linkedin.com"),
                        SocialMediaLinkContent(R.drawable.ic_launcher_foreground, R.string.info_cd_behance, "https://behance.com")
                    ),
                    appVersion = "1.0.0-preview"
                )
            )
        }
        InfoScreen(navController = rememberNavController(), viewModel = mockViewModel)
    }
}

/**
 * Preview function for the InfoScreen in landscape mode.
 * Provides a design-time view of the InfoScreen with mock data.
 */
@Preview(showBackground = true, widthDp = 720, heightDp = 360, name = "Info Screen Landscape")
@Composable
fun InfoScreenPreviewLandscape() {
    LeJeuDu5000Theme {
        val mockAppVersionProvider = object : AppVersionProvider {
            override fun getVersionName(): String = "1.0.0-preview-landscape"
        }
        val mockViewModel = object : InfoViewModel(mockAppVersionProvider) {
            override val uiState = MutableStateFlow(
                InfoScreenUiState(
                    personalInfoItems = listOf(
                        InfoItemContent(R.string.info_developer_label, value = "developer_preview_landscape", valueIsResId = false, isLink = false, linkUri = null),
                        InfoItemContent(R.string.info_website_label, value = "website_preview_landscape", valueIsResId = false, isLink = true, linkUri = "https://example.com")
                    ),
                    socialMediaLinks = listOf(
                        SocialMediaLinkContent(R.drawable.ic_launcher_foreground, R.string.info_cd_linkedin, "https://linkedin.com")
                    ),
                    appVersion = "1.0.0-preview-landscape"
                )
            )
        }
        InfoScreen(navController = rememberNavController(), viewModel = mockViewModel)
    }
}