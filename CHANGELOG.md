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

### Changed
- Updated Kotlin and KSP plugin application in project-level `build.gradle.kts`.
- Corrected Android Gradle Plugin (AGP) version in `libs.versions.toml`.
- **Updated `Type.kt` to consistently apply `FontFamily.Monospace` across all Material 3 typography styles, ensuring a harmonized application theme.**
- **Modified `strings.xml` string `info_app_version_label` to use a format string for dynamic version display.**

### Fixed
- Resolved application startup crash related to Hilt initialization. This involved:
  - Creating a custom `Application` class (`LeJeuDu5000App.kt`) annotated with `@HiltAndroidApp`.
  - Declaring the custom `Application` class in `AndroidManifest.xml` using the `android:name` attribute.
- **Resolved `BuildConfig` generation issue by explicitly enabling the `buildConfig` build feature in `app/build.gradle.kts`.**
- **Corrected various IDE-reported issues in `InfoScreen.kt`, including Hilt ViewModel unresolved references (after dependency fix), string resource lookup logic, composable preview setup by making `InfoViewModel` and its `uiState` open, and parameter order for `Modifier`.**
- **Ensured app version now correctly displays on InfoScreen after `strings.xml` and `BuildConfig` fixes.**
- **Ensured `FontFamily.Monospace` is correctly and consistently applied, resolving previous theme inconsistencies on InfoScreen.**

### Removed