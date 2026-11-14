export const MENU_DOC_SECTIONS = [
  {
    id: "getting-started",
    title: "Getting Started",
    pages: [
      {
        slug: "overview",
        title: "Overview",
        tag: "Player Guide",
        abstract:
          "Muto Menu is the native in-game ModMenu interface for Muto Runtime — letting you enable, disable, stage, and hot-swap Fabric 26.x mods without restarting Minecraft.",
        body: `
Muto Menu brings [Muto Runtime](https://muto.fy2ne.me)'s zero-restart JVM engine directly inside **ModMenu's** native \`ModsScreen\`.

> [!IMPORTANT]
> Muto Menu requires **Muto Runtime** (\`~0.1.0-beta\`), **Fabric Loader >= 0.16.0**, **Fabric API >= 0.144.1**, **ModMenu**, and **Minecraft 26.1 – 26.3** (\`26.x\`).

## Key Capabilities

- **Native ModMenu Integration**: Toggle active, disabled (\`.jar.disabled\`), and hot-loaded mods right alongside existing ModMenu cards with crisp visual dimming and detail-header status badges.
- **Persistent Disk State**: Disabling a mod renames \`example.jar\` to \`example.jar.disabled\` via \`JarUnlocker\`, so disabled mods stay disabled across full client restarts.
- **Two-Click Dependency Protection**: Warned before disabling a library or dependency still used by active mods.
- **Folder Auto-Watcher**: Automatically triggers hot-swaps when you drop or remove JARs inside \`.minecraft/mods\` from your file explorer.
- **In-World Reload & Rejoin**: Swap Mixin or Gameplay mods even while playing singleplayer or multiplayer — Muto cleanly parks, swaps, and rejoins automatically.
`,
      },
      {
        slug: "installation",
        title: "Installation & Requirements",
        tag: "Setup",
        abstract:
          "Install Muto Runtime, Muto Menu, and ModMenu into your Fabric 26.x instance in under a minute.",
        body: `
## Required Mods

Place the following JARs into your \`.minecraft/mods/\` folder:

1. **Fabric API** (\`>=0.144.1\`)
2. **ModMenu** (\`26.x\` compatible build)
3. **Muto Runtime** (\`muto-0.1.0-beta+26.x.jar\`)
4. **Muto Menu** (\`muto-menu-0.1.0-beta+26.x.jar\`)

> [!TIP]
> Ensure **Java 25** is selected in your launcher profile. Muto Runtime uses Java 25 classfile instrumentation and JVMTI retransformation internally.

## Opening Muto Controls

- Click **Mods** from the Title Screen or Pause Menu as normal — Muto Menu injects controls directly into ModMenu via \`ModMenuScreenMixin\`.
- Access Muto Menu Settings anytime through ModMenu's **Configure** button (\`MutoModMenuPlugin\`) or configure your hotkey in **Controls > Misc** (default: \`O\`).
`,
      },
    ],
  },
  {
    id: "usage",
    title: "Managing Mods Live",
    pages: [
      {
        slug: "modmenu-integration",
        title: "ModMenu Cards & Badges",
        tag: "UI Mechanics",
        abstract:
          "How Muto Menu dims disabled cards, displays Pending and Disabled pills cleanly in the detail header, and integrates native filters.",
        body: `
Muto Menu injects seamlessly into \`com.terraformersmc.modmenu.gui.ModsScreen\` rather than spawning an unfamiliar disconnected window.

## Card States & Badges

- **Active Mods**: Rendered at full brightness inside the ModMenu scroll list.
- **Disabled Mods (\`.jar.disabled\`)**: Synchronized into ModMenu by \`ModMenuDiskAdapter\`. Cards render cleanly translucent/dimmed so you can spot inactive mods at a glance without visual noise.
- **Pending Changes**: Staging a disable or enable renders an amber **\`Pending: Off\`** / **\`Pending: On\`** pill right beside the mod name in the detail header and increments the bottom **\`Save (N)\`** counter.
- **Quick Filter Pills**: Filter the list cleanly between **All**, **Active**, **Disabled**, and **Unloaded** states directly from the ModMenu top toolbar (\`MutoModFilter\`).

## Reviewing Pending Changes (\`MutoModChangesScreen\`)

Clicking **\`Save (N)\`** opens \`MutoModChangesScreen\`, summarizing every queued enable/disable action before touching disk:

- **Discard**: Clears all staged changes and restores prior card states.
- **Back**: Returns to ModMenu without applying yet.
- **Apply & Reload**: Suppresses false folder-watcher triggers temporarily, executes atomic \`.jar\` <-> \`.jar.disabled\` file renames via \`ModStateManager\`, and runs \`MutoApi.reloadWithBoundary\`.
`,
      },
      {
        slug: "dependency-warnings",
        title: "Dependency Safety & Toggling",
        tag: "Safety",
        abstract:
          "How Muto Menu checks reverse mod dependencies before disabling a library and supports two-click overrides.",
        body: `
Unloading a shared library like \`cloth-config\` or a core dependency while downstream mods still reference its classes can break rendering or gameplay.

## Two-Step Safety Prompt

When **Missing Dep Warnings** (\`warnDependencies\`) is enabled in \`MutoConfigScreen\`:

1. Attempting to stage-disable a mod required by active mods displays a warning listing dependents (e.g. \`used by: [ModA, ModB] (click again to disable)\`).
2. Clicking a second time confirms you intentionally want to override the dependency guard and stage the disable anyway.

> [!NOTE]
> Disabling protected core entries (\`muto\`, \`muto_menu\`, \`fabricloader\`, \`fabric-api\`, \`minecraft\`, \`java\`) is blocked automatically so you cannot accidentally unload the hot-swap engine beneath yourself.
`,
      },
      {
        slug: "folder-watcher",
        title: "Mods Folder Auto-Watcher",
        tag: "Automation",
        abstract:
          "Watch .minecraft/mods/ continuously with ModsFolderWatcher so externally added or deleted JARs hot-load automatically.",
        body: `
Download a utility mod in your browser and drag the JAR into \`.minecraft/mods/\` while Minecraft is open — \`ModsFolderWatcher\` handles the rest.

## How \`ModsFolderWatcher\` Works

- Uses Java NIO \`WatchService\` pointed at your active \`mods/\` directory to detect \`ENTRY_CREATE\`, \`ENTRY_DELETE\`, and \`ENTRY_MODIFY\` events.
- Debounces incomplete file writes so large mod JARs finish copying before \`ReloadEngine\` parses them.
- Automatically pauses (\`ModsFolderWatcher.suppressForMs(...)\`) while \`MutoModChangesScreen\` performs intentional batch renames, preventing duplicate reloads.

Toggle the watcher live inside **Muto Menu Settings** (\`Auto-Watch Mods Folder\`) or via \`config/muto-menu.json\`.
`,
      },
      {
        slug: "world-rejoin",
        title: "In-World Reload & Rejoin",
        tag: "Boundary Flow",
        abstract:
          "Safely commit mod swaps even when connected to a singleplayer save or multiplayer server.",
        body: `
When you confirm pending mod swaps while inside a world, clicking **Apply & Reload** delegates through \`MutoApi.reloadWithBoundary(parent)\`:

1. Muto snapshots whether you are inside an integrated singleplayer world or connected to a remote server address.
2. Cleanly disconnects you to \`MutoReconnectingScreen\`, pausing world rendering and ticking threads.
3. Unlocks JARs, retransforms mixins, updates registries, and dispatches \`MutoEvents.RELOAD_FINISH\` back to \`MenuMod\` so \`ModMenuDiskAdapter\` refreshes cached mod counts and entries.
4. Automatically reconnects you back into the exact world or server.
`,
      },
    ],
  },
  {
    id: "tools",
    title: "Configuration & Diagnostics",
    pages: [
      {
        slug: "history-dev",
        title: "History & Dev Screen",
        tag: "Diagnostics",
        abstract:
          "Audit every hot-swap timestamp, inspect live muto-dev.log output, and dry-run reload simulations inside Minecraft.",
        body: `
Muto Menu exposes built-in observability screens for modpack creators and developers.

## Reload History (\`ReloadHistoryScreen\`)

Inspect every executed swap in the current session, including:
- Exact wall-clock execution duration in milliseconds
- Added, removed, enabled, and disabled mod diffs
- Success vs automatic rollback state

## Dev Diagnostics & Simulator (\`MutoDevScreen\`)

From **Muto Menu Settings**, open the Dev Diagnostics screen to access:
- **DevMode Toggle**: Enables verbose telemetry logging (\`MutoApi.setDevMode(...)\`).
- **Simulate Reload**: Calls \`MutoApi.runReloadSimulation()\` to dry-run dependency resolution and Mixin checks without mutating live classes.
- **View Dev Logs**: Streams recent entries directly from \`muto-dev.log\` inside an in-game console view.
`,
      },
      {
        slug: "configuration",
        title: "Configuration (`muto-menu.json`)",
        tag: "Config",
        abstract:
          "Persistent JSON schema stored in config/muto-menu.json and synchronized live with Muto Runtime.",
        body: `
Settings adjusted in \`MutoConfigScreen\` persist immediately via atomic temp-file writes to \`config/muto-menu.json\`:

\`\`\`json
{
  "autoWatchFolder": true,
  "autoReloadResources": true,
  "warnDependencies": true
}
\`\`\`

## Keys

| Option | Default | Effect |
| :--- | :--- | :--- |
| \`autoWatchFolder\` | \`true\` | Enables \`ModsFolderWatcher\` monitoring of \`.minecraft/mods\` |
| \`autoReloadResources\` | \`true\` | Syncs client resource pack refresh cleanly via \`MutoApi.setAutoReloadResources\` |
| \`warnDependencies\` | \`true\` | Displays reverse-dependency confirmation prompts before disabling mods |

Changing options inside the GUI calls \`MenuConfig.save()\` and \`MenuConfig.syncToRuntime()\` immediately with zero client restart needed.
`,
      },
    ],
  },
];
