# Changelog

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
