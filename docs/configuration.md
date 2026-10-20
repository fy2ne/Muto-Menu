# Muto Menu Configuration

Muto Menu automatically creates and manages its settings in:
```
.minecraft/config/muto-menu.json
```

## Options

```json
{
  "autoWatchFolder": true,
  "autoReloadResources": true,
  "warnDependencies": true
}
```

- `autoWatchFolder`: When `true`, dropping or deleting mod jars in the `.minecraft/mods` directory triggers an automatic reload when on the title screen.
- `autoReloadResources`: When `true`, reloading reloads client resource packs and textures.
- `warnDependencies`: When `true`, prompts you if disabling a mod would break other enabled mods.

## Controls

The default keybinding is **`O`**. You can rebind it under:
`Options > Controls > Key Binds > Misc > Open Muto Menu`.
