# Muto Menu

In-game mods screen for [Muto Runtime](https://github.com/fy2ne/muto). Toggle mods on and off, and reload after you drop a jar into `mods/`, with no game restart.

- Press `O` (rebindable, Controls > Misc) to open the screen
- `mods/` is watched while you're on the title screen, changes trigger a reload
- Needs Muto Runtime `0.3.x` installed alongside. Menu does not bundle it

Targets Minecraft 26.1 to 26.3 (`26.x` branch). Version matches Runtime: `0.3.0-beta+26.x`.

## Build

Runtime isn't on a public Maven yet, so publish it locally first:

```
cd ../runtime
./gradlew publishToMavenLocal
cd ../menu
./gradlew build
```

## License

Apache-2.0, see `LICENSE`. Muto Runtime is LGPL-3.0 and is used as a separate dependency (not bundled or copied), which keeps the two licenses independent.
