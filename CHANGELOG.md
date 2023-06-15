# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.3] - 2023-06-15
### Changed
- Update Kotlin to 1.8.22
### Fixed
- `NoSuchElementException` in ContactSchemeSwitchAction (issue #3)

## [1.0.2] - 2023-06-07
### Fixed
- `NoSuchElementException` when clicked twice on the scheme switch action without reselecting the primitive (0c0f27bc)

## [1.0.1] - 2023-05-04
### Fixed
- Icon path in the MANIFEST.MF file (GH #1)
### Changed
- Translation files moved to `src/main/resources/data/i18n`

## [1.0.0] - 2023-05-03 - First public release! 🎉
### Changed
- Values deduped on contact scheme switch action
- Decouple libphonenumber dependency and add a workaround until a final solution
- Update Gradle to 8.1.1
- Update Kotlin to 1.8.21

<details>
  <summary>Internal development releases</summary>

## [0.16.2] - 2023-04-04
### Added
- Dependabot auto dependency check for github-actions
### Changed
- Refined Overpass timeout handling
- Update Kotlin to 1.8.20

## [0.16.1] - 2023-02-25
### Fixed
- ContactSchemeSwitchAction
  - now the undo action is working
  - data layer remove no longer breaks the menu
- GitHub release process
### Changed
- Update Kotlin to 1.8.10
- Update Gradle to 7.6.1
- Bump JOSM compile version to r18678

## [0.16.0] - 2023-01-01
### Added
- Hungarian translation
### Changed
- Fixed the name of the plugin, it's called `phonenumber` from now on
- Update Kotlin to 1.8.0

## [0.15.0] - 2022-11-06
### Added
- Contact scheme switch action to tag menu
### Changed
- Update project dependencies
- Bump JOSM compile version to r18583
- Update Kotlin to 1.7.20

## [0.14.3] - 2022-06-06
### Changed
- Update project dependencies
- Bump JOSM compile version to r18463

## [0.14.2] - 2022-04-07
### Changed
- Update Kotlin to 1.6.20

## [0.14.1] - 2022-04-02
### Changed
- Update project dependencies
- Bump JOSM compile version to r18387

## [0.14.0] - 2022-02-22
### Added
- Suffixed key check (e.g. `phone_2`) - 0c7ec9a1

## [0.13.0] - 2022-01-26
### Changed
- Enabled forceful contact scheme switch
  - Added BooleanProperty for behaviour setting

## [0.12.1] - 2022-01-19
### Changed
- Disable JOSM bug #21446 workaround as the proposed fix works

## [0.12.0] - 2022-01-02
### Added
- A nice download icon
- git commit hash to the version number
### Changed
- The download action is fixed in the toolbar, reappears after restart even if removed.
  This is a workaround for a JOSM bug, see [#21446](https://josm.openstreetmap.de/ticket/21446)
- Switched to Oracle Linux 8 [GitlabCI]

## [0.11.0] - 2022-01-01
### Added
- Download action to the toolbar for downloading objects in the current
  map view

## [0.10.2] - 2021-12-24
### Security
- Gradle wrapper updated to 7.3.3 (fix Log4j vulnerabilities)

## [0.10.1] - 2021-12-14
### Changed
- Kotlin upgraded to 1.6.10
- Packed Kotlin runtime into JAR for compatibility

## [0.10.0] - 2021-12-05
### Added
- duplicate removal (see #2)
- tagging scheme change ability to `contact:` prefix scheme
### Changed
- reworked the internals to make it more robust
- Gradle wrapper updated to 7.3.1
- Kotlin upgraded to 1.6.0

## [0.6.3] - 2021-04-03
### Changed
- Tweaked number matching regex
- Kotlin upgraded to 1.4.32
- Gradle wrapper updated to 6.8.3
- TestNG updated to 7.4.0

## [0.6.2] - 2021-02-16
### Changed
- Kotlin upgraded to 1.4.30

## [0.6.1] - 2021-01-13
### Fixed
- Number keys splitting
- Autofix preference loading

## [0.6.0] - 2021-01-11
### Changed
- Code has completely rewritten in Kotlin
- Only show beautify warning when autofix explicitly enabled
### Fixed
- Autofix preference saving
- Autofix tag deletion bug

### Removed
- Apache Commons dependency
- JetBrains Annotations dependency

## [0.5.4] - 2021-01-10
### Added
- Version string at the end of the JAR file
- README.md
- This changelog file
- MIT license
### Changed
- Migrated Gradle build script from Groovy to Kotlin
</details>
