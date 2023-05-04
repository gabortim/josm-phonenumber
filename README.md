# <img src="src/main/resources/images/icon.svg" width=40px> Google libphonenumber plugin for JOSM

The code repository contains only the needed parts for the connection between
the parser library and JOSM. Please find the repacked libphonenumber under
[gabortim/josm-libphonenumber](https://github.com/gabortim/josm-libphonenumber).

For changes, see [Changelog.md](CHANGELOG.md).

## Features
The plugin gives the JOSM validator many tests and quick fixes:
- validate numbers against location, although the localization is rough as it's
  a limitation of the JOSM internal territories dataset
- fixes wrongly separated numbers (comma used instead of semicolon)
- able to change tagging scheme to the newer `contact:` prefix one via the validator
  and via the properties' dialog popup menu
- moves mobile numbers to `mobile` tag
- adds an easy, overpass type download action to the toolbar for downloading
  all objects with any phone key in the current view
- (BETA) provides autofix for all the above for a more convenient experience

## Installation
Download the **phonenumber** plugin from JOSM as usual.

## Usage
Just run the validator, the messages will pop up.

To activate the autofix capability:
1. Enable expert mode - see [JOSM wiki](https://josm.openstreetmap.de/wiki/Help/ExpertMode#EnablingExpertmode)
2. Check the autofix combobox in the [validator tab](https://josm.openstreetmap.de/wiki/Help/Preferences/Validator).

## Contribution
Contributions are welcomed. When you encounter an issue
or have an improvement idea, don't hesitate to report it to [GitHub](https://github.com/gabortim/josm-phonenumber/issues/new)
or [JOSM Trac](https://josm.openstreetmap.de/newticket?component=Plugin%20phonenumber) :)

### Translation
Download the latest `.pot` file from the CI/CD artefacts and translate it locally.
After the translation is done, create a Merge Request with the file to the
[i18n](/src/main/resources/data/i18n) directory.

Existing translations can be modified in the same way.