# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## 3.3.0 - 2024-04-26
### Fixed
- Images without an alt text are now getting a default alt text instead

### Changed
- Relative URLs of an image get removed, so that the client will not try to load the image, which will fail anyways

## 3.2.0 - 2024-04-09
### Added
- Each summarized email now contains the timestamp of their creation time

## 3.1.0 - 2024-03-14
### Added
- Checkbox to always use configured sender address (instead of user address)

### Fixed
- Position in configuration menu

## 3.0.0 - 2024-02-27
### Added
- Added API to summarize emails into one
- Added documentation for plugin configuration, permissions and testing

## 2.10.1 - 2023-04-12
### Fixed
- Missing encryption in the audit log
- Masking of passwort network layer

## 2.10.0 - 2023-02-15
### Changed
- Remove embedded image and change css-styles to support more email clients.

## 2.9.0 - 2022-06-29
### Changed
- exchange logo image, reduce superfluous CSS, harmonize font-stack ([#44](https://github.com/scm-manager/scm-mail-plugin/pull/44))

## 2.8.0 - 2022-06-28
### Changed
- enhance email template, remove superfluous CSS ([#43](https://github.com/scm-manager/scm-mail-plugin/pull/43))

## 2.7.2 - 2022-05-16
### Fixed
- Use address from current user correctly ([#39](https://github.com/scm-manager/scm-mail-plugin/pull/39))

## 2.7.1 - 2021-06-10
### Fixed
- Usage of mail api ([#24](https://github.com/scm-manager/scm-mail-plugin/pull/24))

## 2.7.0 - 2021-06-08
### Changed
- Use ssl socket factory from custom ssl context ([#20](https://github.com/scm-manager/scm-mail-plugin/pull/20))

## 2.6.0 - 2021-06-04
### Added
- Notifications for health checks ([#18](https://github.com/scm-manager/scm-mail-plugin/pull/18))

### Fixed
- Correct mail language selection ([#22](https://github.com/scm-manager/scm-mail-plugin/pull/22))

## 2.5.0 - 2021-03-01
### Added
- Link to import log in mail ([#12](https://github.com/scm-manager/scm-mail-plugin/pull/12))

## 2.4.0 - 2020-12-04
### Added
- Send mail for successful or failed repository import ([#9](https://github.com/scm-manager/scm-mail-plugin/pull/9))

## 2.3.0 - 2020-11-20
### Added
- Tracing of calls to the mail server ([#8](https://github.com/scm-manager/scm-mail-plugin/pull/8))

## 2.2.1 - 2020-10-27
### Fixed
- Handle users without mail address correctly ([#7](https://github.com/scm-manager/scm-mail-plugin/pull/7))

## 2.2.0 - 2020-08-13
### Changed
- Prevent anonymous user from changing the mail config ([#6](https://github.com/scm-manager/scm-mail-plugin/pull/6))

## 2.1.0 - 2020-07-03
### Added
- Add topics for mails, so that users can select what kind of mails they want to receive ([#4](https://github.com/scm-manager/scm-mail-plugin/pull/4))

## 2.0.0 - 2020-06-04
### Changed
- Changeover to MIT license ([#3](https://github.com/scm-manager/scm-mail-plugin/pull/3))
- Rebuild for api changes from core

## 2.0.0-rc2 - 2020-03-13
### Added
- Add swagger rest annotations to generate openAPI specs for the scm-openapi-plugin. ([#2](https://github.com/scm-manager/scm-mail-plugin/pull/2))

### Changed
- Configuration layout ([#1](https://github.com/scm-manager/scm-mail-plugin/pull/1))

## 2.0.0-rc1 - 2020-03-13
### Added
- First public release candidate for SCM-Manager 2

