# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Basic navigation structure with HomeScreen, GameScreen, SettingsScreen, and InfoScreen.
- CommonTopAppBar for consistent in-app navigation.
- Placeholder UI for main screens.
- Extraction of initial UI strings to `strings.xml`.
- **InfoScreen UI displaying developer details, social media links, and application information.**
- **`InfoViewModel` and `InfoUiState` for managing InfoScreen data and state.**
- **Dynamic display of application version on InfoScreen, fetched from `BuildConfig`.**
- **New string resources in `strings.xml` for `InfoScreen` content.**
- **UI tests for `InfoScreen` (`InfoScreenTest.kt`) covering content display, click actions, and empty states.**
- **Necessary testing dependencies (JUnit Jupiter API, MockK Android) for instrumented tests.**
- **Comprehensive UI tests for `SettingsScreen` (`SettingsScreenTest.kt`) covering loading and error states, content display, and interactions with all setting items (dropdowns and switches).**
- **Unique test tags to `SwitchSettingItem` composables in `SettingsScreen.kt` to enable robust UI testing.**
- **Helper function `createItemTestTag` in `SettingsScreen.kt` for consistent test tag generation.**
- **`DiceUi.kt` defining the UI representation of a die.**
- **`GameUiState.kt` defining the state for the game screen UI.**
- **Initial `GameViewModel.kt` with Hilt integration, managing `GameUiState`, and handling core game actions (roll, select dice, keep score, bank turn).**
- **`FinalizeTurnUseCase.kt` to encapsulate the logic of finalizing a player's turn, including score validation against opening thresholds and game settings.**
- **`ScoreBoard.kt` Composable to display player scores and turn information.**
- **`DiceView.kt` Composable (with `DiceRow`) to display dice with selection and scoring states.**
- **Initial implementation of `GameScreen.kt` integrating `GameViewModel`, `ScoreBoard`, `DiceRow`, action buttons, and game messages.**
- **Comprehensive string resource extraction to `strings.xml` for `GameScreen`, `ScoreBoard`, `DiceView`, `HomeScreen`, `InfoScreen`, `SettingsScreen`, and `CommonTopAppBar`.**

### Changed
- Updated Kotlin and KSP plugin application in project-level `build.gradle.kts`.
- Corrected Android Gradle Plugin (AGP) version in `libs.versions.toml`.
- **Updated `Type.kt` to consistently apply `FontFamily.Monospace` across all Material 3 typography styles, ensuring a harmonized application theme.**
- **Modified `strings.xml` string `info_app_version_label` to use a format string for dynamic version display.**
- **Refactored `SettingsScreen.kt` previews to use fake dependencies instead of MockK, resolving compilation issues.**
- **Standardized callback parameter names (using `onCheckedChange`) in `SwitchSettingItem` and its previews in `SettingsScreen.kt`.**
- **`GameViewModel.kt` refactored to inject and use `SettingsRepository` for accessing `GameSettings` and `FinalizeTurnUseCase` for turn finalization logic.**
- **Renamed `GameViewModel.bankScore()` to `keepSelectedDiceAndContinueTurn()` for clarity, and introduced `endTurnAndBankAccumulatedScore()` for turn completion.**
- **Refined `canRoll` and `canBank` logic in `GameViewModel.kt` based on game state, player status (opened or not), and game settings (opening threshold).**
- **Updated `strings.xml` to correctly include all necessary strings for newly developed UI components and resolve previous inconsistencies.**

### Fixed
- Resolved application startup crash related to Hilt initialization. This involved:
  - Creating a custom `Application` class (`LeJeuDu5000App.kt`) annotated with `@HiltAndroidApp`.
  - Declaring the custom `Application` class in `AndroidManifest.xml` using the `android:name` attribute.
- **Resolved `BuildConfig` generation issue by explicitly enabling the `buildConfig` build feature in `app/build.gradle.kts`.**
- **Corrected various IDE-reported issues in `InfoScreen.kt`, including Hilt ViewModel unresolved references (after dependency fix), string resource lookup logic, composable preview setup by making `InfoViewModel` and its `uiState` open, and parameter order for `Modifier`.**
- **Ensured app version now correctly displays on InfoScreen after `strings.xml` and `BuildConfig` fixes.**
- **Ensured `FontFamily.Monospace` is correctly and consistently applied, resolving previous theme inconsistencies on InfoScreen.**
- **Resolved build error `INSTALL_FAILED_USER_RESTRICTED` during UI test execution on physical device (MIUI) by disabling MIUI optimizations and enabling developer options for USB install.**
- **Resolved build error due to duplicate `META-INF/LICENSE.md` files from JUnit Jupiter dependencies by adding packaging options to `app/build.gradle.kts`.**
- **Corrected `InfoScreenTest` to accurately assert displayed text for personal info items, aligning with `InfoScreen`'s label formatting.**
- **Resolved UI test failures in `SettingsScreenTest.kt` by correcting switch state assertion logic in `testSwitchInteraction` and ensuring test tags match production code.**
- **Corrected various compilation errors in `SettingsScreen.kt` related to `mutableStateOf`, ViewModel instantiation in previews, and deprecated Kotlin functions (`toLowerCase` replaced with `lowercase`, `Divider` with `HorizontalDivider`, `menuAnchor` updated).**
- **Resolved `Unresolved reference` errors for MockK in `SettingsScreen.kt` previews by switching to fake dependencies.**
- **Corrected type mismatches and unresolved references in `GameViewModel.kt` related to `Player` class properties (UUID for id, `totalScore` vs `currentScore`).**
- **Ensured all referenced string resources in `GameScreen.kt`, `ScoreBoard.kt`, and `DiceView.kt` are correctly defined in `strings.xml`, resolving build errors.**

### Removed

