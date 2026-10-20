# Muto Menu Overview

Muto Menu is the native in-game interface for [Muto Runtime](https://github.com/fy2ne/muto). It provides a full mods management screen, live enable/disable toggling, hot-swapping, and audit history.

## Features

- **Hotkey & Buttons**: Press `O` (configurable in Controls > Misc) or click the **Muto** button on the Title Screen or Pause Screen.
- **Live Mod Toggling**: Click any mod to toggle between `[on]` and `[off]`. Staged changes show an asterisk `*`.
- **Dependency Warnings**: When disabling a mod that other active mods rely on, Muto Menu alerts: `used by: [ModA, ModB] (click again to disable)`.
- **Auto-Watch Switch**: Toggle the automatic folder watcher on or off directly from the top bar: `Watch: ON / OFF`.
- **Reload History Viewer**: Click `History (N)` to inspect timestamped logs of previous hot-swaps, mod diffs, and execution times.
- **In-World Reload & Rejoin**: While in a singleplayer world or connected to a multiplayer server, the button dynamically changes to `Reload & Rejoin`, ensuring clean session handoff and automatic reconnect.

## Requirements

- **Minecraft**: 26.1 - 26.3 (`26.x`)
- **Fabric Loader**: `>=0.16.0`
- **Fabric API**: `>=0.144.1`
- **Muto Runtime**: `~0.3.0-beta`
