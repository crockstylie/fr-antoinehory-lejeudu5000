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

### Changed
- Updated Kotlin and KSP plugin application in project-level `build.gradle.kts`.
- Corrected Android Gradle Plugin (AGP) version in `libs.versions.toml`.

### Fixed
- **Resolved application startup crash related to Hilt initialization. This involved:**
    - **Creating a custom `Application` class (`LeJeuDu5000App.kt`) annotated with `@HiltAndroidApp`.**
    - **Declaring the custom `Application` class in `AndroidManifest.xml` using the `android:name` attribute.**

### Removed
