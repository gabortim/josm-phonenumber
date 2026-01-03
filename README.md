# <img src="src/main/resources/images/icon.svg" width=40px> Google libphonenumber plugin for JOSM

This JOSM plugin leverages the Google [libphonenumber](https://github.com/google/libphonenumber) library to provide
advanced phone number validation and formatting.

The code repository contains only the necessary glue code between the parser library and JOSM. The repacked
libphonenumber library can be found at [gabortim/josm-libphonenumber](https://github.com/gabortim/josm-libphonenumber).

For a detailed list of changes, see [CHANGELOG.md](CHANGELOG.md).

## Features

The plugin enhances the JOSM validator with several tests and quick fixes:

- **Validation:** Validates numbers against their location (note: localization precision is limited by JOSM's internal
  territories dataset).
- **Format Correction:** Fixes wrongly separated numbers (e.g., converting commas to semicolons).
- **Tagging Scheme Migration:** Facilitates switching to the newer `contact:phone` and `contact:mobile` tagging schemes
  via the validator or the properties dialog popup menu.
- **Mobile Number Identification:** Automatically moves mobile numbers to the `contact:mobile` tag.
- **Easy Download:** Adds an Overpass-type download action to the toolbar, allowing you to download all objects with
  phone-related keys in the current view.
- **Autofix (BETA):** Provides convenient autofixes for the issues mentioned above.

## Installation

Install the **phonenumber** plugin directly from the JOSM plugin preferences.

**Requirement:** Java 17 or newer.

## Usage

1. Run the JOSM validator as usual.
2. If any phone number issues are found, they will appear in the validator results.

### Enabling Autofix (BETA)

To activate the autofix capability:

1. Enable **Expert Mode** (see [JOSM Wiki](https://josm.openstreetmap.de/wiki/Help/ExpertMode#EnablingExpertmode)).
2. In the [Validator Preferences](https://josm.openstreetmap.de/wiki/Help/Preferences/Validator), check the **autofix**
   checkbox for the phonenumber tests.

## Contribution

Contributions are welcome! If you encounter a bug or have an idea for an improvement, please report it via:

- [GitHub Issues](https://github.com/gabortim/josm-phonenumber/issues)
- [JOSM Trac](https://josm.openstreetmap.de/newticket?component=Plugin%20phonenumber)

### Project Setup

To synchronize project dependencies after cloning, you'll need to configure GitHub Packages access:

1. Go to your GitHub [Personal Access Tokens (classic)](https://github.com/settings/tokens).
2. Generate a new token with the `read:packages` permission.
3. Create a `local.properties` file in the project root.
4. Add your credentials to `local.properties`:
   ```properties
   GITHUB_ACTOR=<your-github-username>
   GITHUB_PACKAGE_REPO_TOKEN=<your-access-token>
   ```

### Translation

1. Download the latest `.pot` file from the CI/CD artifacts.
2. Translate it locally.
3. Create a Pull Request with the new/updated file in the [
   `src/main/resources/data/i18n`](/src/main/resources/data/i18n) directory.