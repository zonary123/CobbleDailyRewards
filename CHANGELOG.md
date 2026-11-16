# Changelog

## [1.2.0] - 2026-11-16

### Added

- **Custom Menu & Rotom Phone Support**: Introduced a pluggable menu system allowing companion mods (like Rotom Phone, custom graphical interfaces, or web dashboards) to replace the default reward chest screens with their own custom menus seamlessly.
- **Headless Daily Reward Actions**: Added support for claiming rewards or checking cooldowns directly in the background without needing to open chest menus.
- **Developer API & Artifacts**: Published a dedicated, lightweight `-api.jar` artifact for developers and third-party modders to integrate with CobbleDailyRewards.

### Changed

- Internal reward commands now route through the unified API and menu registry.
- Automated release workflow now publishes all release artifacts including developer API jars.

## [1.1.7] - 2026-10-18

### Added

- **Built-in Documentation (WIKI.md)**: Added an automatic wiki guide that extracts into `config/cobbledailyrewards/` when the server starts up, explaining all commands, permissions, and configuration options.

### Changed

- Updated CobbleUtils dependency to version 1.3.8.

## [1.1.6] - 2026-10-12

### Changed

- **License**: Changed project license to CC-BY-NC-4.0 (Non-Commercial).

## [1.1.5] - 2026-09-13

### Fixed

- **Thread-Safety and Concurrency**: Fixed a `ConcurrentModificationException` crash when saving rewards and alerts in background tasks.
- **Race Condition Prevention**: Converted internal player maps and reward lists to thread-safe collections to prevent desyncs during player joins and reloads.

## [1.1.4] - 2025-12-03

### Features

- N\A

### Bug Fixes

- Fix rewards can claimed without wait when the player is new to the server.

### Optimizations

- N\A
