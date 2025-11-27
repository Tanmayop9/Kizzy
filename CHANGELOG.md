# Changelog

All notable changes to Kizzy will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [6.2] - 2025-11-27

### Added
- **24/7 VC Stay** - Stay connected to a Discord voice channel 24/7 without keeping the app open
- **Enhanced User Profile Management** - Change bio, status directly from the app
- **Auto-Start on Boot** - Automatically start RPC when device boots up
- **Battery Saver Mode** - Reduce battery usage by limiting background activity
- **Invisible Mode** - Run RPC while appearing offline to others
- **Random RPC Rotation** - Automatically rotate between your favorite RPC configs
- **Scheduled RPC** - Enable/disable RPC at specific times (coming soon)
- **RPC History** - Track and quickly reuse your recent RPC configurations
- **Favorites System** - Save your favorite RPC configs for quick access
- **Connection Status Indicator** - Show real-time connection status
- **Advanced Settings Screen** - New dedicated screen for power-user features
- **Voice State support** - Join/leave voice channels programmatically via Discord Gateway

### Changed
- Improved Settings Drawer with Advanced Features link
- Enhanced preferences system with new options
- Updated navigation with new routes for all features

### Technical
- Added VoiceState entity for voice channel connections
- Added joinVoiceChannel and leaveVoiceChannel methods to DiscordWebSocket
- Added VCStayService for managing voice channel connections
- Added BootReceiver for auto-start functionality
- Added comprehensive preference storage for new features

## [6.1] - 2025-11-27

### Added
- Android 14 support (targetSdk 34)
- Improved UI with better animations, shadows, and styling
- Enhanced GitHub Actions CI/CD workflows

### Changed
- Updated AGP to version 8.5.0
- Updated Gradle and dependencies
- Improved manifest configurations

### Fixed
- Various bug fixes and stability improvements

## [6.0] - Previous Release

### Features
- Discord Rich Presence manager for Android
- Custom RPC configurations
- Nintendo and Wii RPC presets (300+ options)
- Material You theme support
- Multiple language support
