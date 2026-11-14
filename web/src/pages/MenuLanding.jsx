import { useState } from "react";
import { Link } from "react-router-dom";
import {
  AlertTriangle,
  ArrowRight,
  ArrowUpRight,
  CheckCircle2,
  FolderSync,
  History,
  Power,
  RefreshCw,
} from "lucide-react";
import { FrameCard } from "../components/FrameCard";

const CALLOUTS = [
  {
    id: "disabled",
    index: "01",
    title: "Disabled Card Dimming",
    summary:
      "Mods renamed to .jar.disabled appear cleanly in ModMenu with a red [Disabled] pill and translucent card styling — persisting across restarts.",
    pos: { x: 20, y: 20.5 },
  },
  {
    id: "pending",
    index: "02",
    title: "Staged Toggle Pills",
    summary:
      "Clicking Disable stages an amber [Pending: Off] pill beside the mod header so you can batch multiple mod swaps together before reloading.",
    pos: { x: 64.5, y: 15 },
  },
  {
    id: "commit",
    index: "03",
    title: "Atomic Save Bar",
    summary:
      "Save (1) opens MutoModChangesScreen to audit your queued renames and execute Reload & Rejoin without leaving Minecraft.",
    pos: { x: 50, y: 95.5 },
  },
  {
    id: "filters",
    index: "04",
    title: "Mod State Filters",
    summary:
      "Filter ModMenu instantaneously by All, Active, Disabled, or Unloaded JARs directly from the search toolbar.",
    pos: { x: 44, y: 8.5 },
  },
];

const WORKFLOW_STEPS = [
  {
    step: "STEP 01",
    title: "Toggle inside ModMenu",
    body: "Open Mods like you always do. Select any mod to stage Enable, Disable, or Load without hunting through folders.",
    badge: "Native UI",
  },
  {
    step: "STEP 02",
    title: "Verify dependency safety",
    body: "Muto Menu inspects reverse dependencies automatically — warning you with used by: [ModA] before disabling shared libraries.",
    badge: "Dep Guard",
  },
  {
    step: "STEP 03",
    title: "Commit or Auto-Rejoin",
    body: "Review diffs in MutoModChangesScreen, rename .jar.disabled atomically via JarUnlocker, and hot-swap even mid-world.",
    badge: "Boundary Safe",
  },
];

export function MenuLanding() {
  const [activeCallout, setActiveCallout] = useState(CALLOUTS[0]);
  const [demoWarned, setDemoWarned] = useState(false);

  return (
    <main className="relative flex-1 overflow-x-clip">
      {/* SECTION 1: CENTERED ESSENTIAL-BLUE SPOTLIGHT + IN-GAME SCREENSHOT */}
      <section
        style={{ background: "var(--menu-spotlight)" }}
        className="relative px-5 pb-20 pt-16 sm:px-8 sm:pt-24 lg:px-10"
      >
        <div className="mx-auto max-w-4xl text-center">
          <div className="unified-eyebrow mb-5 justify-center">
            <span className="unified-eyebrow-dot" />
            <span>ModMenu Companion // Muto v0.1.0-beta</span>
          </div>

          <h1 className="mx-auto max-w-3xl text-balance font-serif text-4xl font-normal leading-[1.05] tracking-[-0.025em] text-[var(--color-fg-default)] sm:text-5xl lg:text-[4.1rem]">
            Enable and disable mods inside ModMenu — zero restarts.
          </h1>

          <p className="mx-auto mt-5 max-w-2xl text-[15.5px] leading-relaxed text-[var(--color-fg-muted)] sm:text-lg">
            Muto Menu plugs directly into Fabric&apos;s <span className="font-mono text-sm text-[var(--color-fg-default)]">ModsScreen</span> with live <span className="font-mono text-sm text-[var(--color-fg-default)]">.jar.disabled</span> state badges, dependency checks, folder auto-watching, and automatic in-world rejoin.
          </p>

          <div className="mt-8 flex flex-wrap items-center justify-center gap-3.5">
            <Link
              to="/docs/overview"
              className="inline-flex items-center gap-2 rounded-lg bg-[var(--color-brand-default)] px-5 py-2.5 text-sm font-medium text-white no-underline transition-colors hover:bg-[var(--color-brand-hover)] shadow-sm"
            >
              <span>Documentation</span>
              <ArrowRight className="size-4" />
            </Link>

            <a
              href="https://muto.fy2ne.me"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 rounded-lg border border-[var(--color-border-strong)] bg-[var(--color-surface-elevated)] px-5 py-2.5 text-sm font-medium text-[var(--color-fg-default)] no-underline transition-colors hover:bg-[var(--color-surface-hover)]"
            >
              <span>Powered by Muto Runtime</span>
              <ArrowUpRight className="size-4 text-[var(--color-brand-default)]" />
            </a>
          </div>
        </div>

        {/* IN-GAME SCREENSHOT FRAME WITH INTERACTIVE HOTSPOTS */}
        <div className="mx-auto mt-14 max-w-[1140px]">
          <FrameCard innerClassName="bg-[#070910] p-2 sm:p-3 shadow-[var(--shadow-card)]">
            <div className="flex items-center justify-between border-b border-white/10 px-3 pb-2.5 pt-1 text-[11px] font-mono text-zinc-400">
              <div className="flex items-center gap-2">
                <span className="inline-block h-2 w-2 rounded-full bg-[var(--color-brand-default)]" />
                <span>Minecraft 26.3 — ModMenu + Muto Menu (Live Session)</span>
              </div>
              <span className="hidden sm:inline text-zinc-500">
                Inspecting: {activeCallout.title}
              </span>
            </div>

            <div className="relative mt-2 overflow-hidden rounded-lg border border-white/10">
              <img
                src="/screenshot-modmenu.png"
                alt="Muto Menu integrated directly inside ModMenu showing Distant Horizons Disabled, EntityCulling Pending Off, and Save (1)"
                className="block h-auto w-full select-none"
              />

              {/* Subtle royal blue focus reticle showing currently selected UI element */}
              <div
                aria-hidden="true"
                style={{
                  left: `${activeCallout.pos.x}%`,
                  top: `${activeCallout.pos.y}%`,
                }}
                className="pointer-events-none absolute -translate-x-1/2 -translate-y-1/2 transition-all duration-300 ease-out"
              >
                <span className="absolute -inset-4 animate-ping rounded-full bg-[var(--color-brand-default)]/35" />
                <span className="relative flex h-6 w-6 items-center justify-center rounded-full border-2 border-white bg-[var(--color-brand-default)] shadow-lg">
                  <span className="h-1.5 w-1.5 rounded-full bg-white" />
                </span>
              </div>
            </div>

            {/* 4-TAB INTERACTIVE CALLOUT SWITCHER */}
            <div className="mt-3 grid gap-2 sm:grid-cols-2 lg:grid-cols-4">
              {CALLOUTS.map((c) => {
                const selected = activeCallout.id === c.id;
                return (
                  <button
                    key={c.id}
                    type="button"
                    onClick={() => setActiveCallout(c)}
                    className={`rounded-xl border p-3.5 text-left transition-all cursor-pointer ${
                      selected
                        ? "border-[var(--color-brand-default)] bg-[var(--color-brand-default)]/15 text-white"
                        : "border-white/10 bg-white/[0.03] text-zinc-400 hover:border-white/20 hover:text-zinc-200"
                    }`}
                  >
                    <div className="flex items-center justify-between font-mono text-[10.5px] uppercase tracking-[0.1em]">
                      <span className={selected ? "text-[#6ea8ff]" : "text-zinc-500"}>
                        {c.index}
                      </span>
                      {selected && (
                        <span className="h-1.5 w-1.5 rounded-full bg-[var(--color-brand-default)]" />
                      )}
                    </div>
                    <div className="mt-1 font-sans text-[13.5px] font-semibold text-white">
                      {c.title}
                    </div>
                    <p className="mt-1 m-0 text-[12px] leading-relaxed text-zinc-400">
                      {c.summary}
                    </p>
                  </button>
                );
              })}
            </div>
          </FrameCard>
        </div>
      </section>

      {/* SECTION 2: THREE-STAGE PLAYER WORKFLOW */}
      <section className="border-t border-[var(--color-border-default)] py-24">
        <div className="mx-auto max-w-[1280px] px-5 sm:px-8 lg:px-10">
          <div className="mb-12 flex flex-col justify-between gap-4 sm:flex-row sm:items-end">
            <div className="max-w-xl">
              <div className="unified-eyebrow mb-3">
                <span className="unified-eyebrow-dot" />
                <span>Player Workflow</span>
              </div>
              <h2 className="m-0 font-serif text-3xl font-normal leading-[1.1] tracking-[-0.02em] text-[var(--color-fg-default)] sm:text-4xl">
                Designed to feel built into vanilla ModMenu
              </h2>
            </div>
            <Link
              to="/docs/modmenu-integration"
              className="word-link shrink-0 text-sm font-medium"
            >
              Read ModMenu UI guide <ArrowUpRight className="size-3.5" />
            </Link>
          </div>

          <div className="grid gap-5 lg:grid-cols-3">
            {WORKFLOW_STEPS.map((s) => (
              <FrameCard key={s.step} innerClassName="h-full p-7">
                <div className="flex items-center justify-between">
                  <span className="font-mono text-[11px] uppercase tracking-[0.12em] text-[var(--color-brand-default)]">
                    {s.step}
                  </span>
                  <span className="rounded border border-[var(--color-border-default)] px-2 py-0.5 font-mono text-[10px] text-[var(--color-fg-subtle)]">
                    {s.badge}
                  </span>
                </div>
                <h3 className="mt-4 font-sans text-lg font-semibold tracking-tight text-[var(--color-fg-default)]">
                  {s.title}
                </h3>
                <p className="mt-2 m-0 text-sm leading-relaxed text-[var(--color-fg-muted)]">
                  {s.body}
                </p>
              </FrameCard>
            ))}
          </div>
        </div>
      </section>

      {/* SECTION 3: ASYMMETRICAL FEATURE SPOTLIGHTS */}
      <section className="mx-auto max-w-[1280px] space-y-12 px-5 pb-24 sm:px-8 lg:px-10">
        {/* Card A: Interactive Dependency Guard Preview */}
        <FrameCard>
          <div className="grid items-center lg:grid-cols-12">
            <div className="border-b border-[var(--color-border-default)] p-8 sm:p-10 lg:col-span-6 lg:border-b-0 lg:border-r">
              <div className="inline-flex items-center gap-2 font-mono text-[11px] uppercase tracking-[0.12em] text-[var(--color-brand-default)]">
                <AlertTriangle className="size-3.5" />
                <span>Dependency Guard</span>
              </div>
              <h3 className="mt-3 font-serif text-2xl font-normal leading-tight tracking-[-0.015em] text-[var(--color-fg-default)] sm:text-3xl">
                No more accidental library crashes
              </h3>
              <p className="mt-3 text-sm leading-relaxed text-[var(--color-fg-muted)]">
                When you click Disable on a dependency used by active mods, Muto Menu intercepts the toggle first and lists downstream consumers before letting you confirm with a second click.
              </p>
              <Link
                to="/docs/dependency-warnings"
                className="word-link mt-6 text-sm font-medium"
              >
                How dependency guards work <ArrowUpRight className="size-3.5" />
              </Link>
            </div>

            <div className="p-8 sm:p-10 lg:col-span-6">
              <div className="rounded-xl border border-[var(--color-border-default)] bg-[var(--color-canvas-default)] p-5">
                <div className="flex items-center justify-between gap-4">
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="font-sans text-sm font-semibold text-[var(--color-fg-default)]">
                        cloth-config
                      </span>
                      <span className="rounded border border-[var(--color-brand-default)]/40 bg-[var(--color-brand-soft)] px-1.5 py-0.5 font-mono text-[10px] text-[var(--color-brand-default)]">
                        {demoWarned ? "Staged Off" : "Active Library"}
                      </span>
                    </div>
                    <p className="mt-1 m-0 font-mono text-xs text-[var(--color-fg-muted)]">
                      {demoWarned
                        ? "used by: [Sodium Extra, Iris] — override confirmed"
                        : "used by: [Sodium Extra, Iris] (click again to disable)"}
                    </p>
                  </div>

                  <button
                    type="button"
                    onClick={() => setDemoWarned((v) => !v)}
                    className="inline-flex shrink-0 items-center gap-1.5 rounded-lg bg-[var(--color-brand-default)] px-3 py-1.5 font-mono text-xs font-medium text-white transition-colors hover:bg-[var(--color-brand-hover)] cursor-pointer"
                  >
                    <Power className="size-3" />
                    <span>{demoWarned ? "Revert" : "Click Disable"}</span>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </FrameCard>

        {/* Card B: Automation & Observability Grid */}
        <div className="grid gap-5 md:grid-cols-3">
          <div className="rounded-2xl border border-[var(--color-border-default)] bg-[var(--color-surface-elevated)] p-7">
            <FolderSync className="size-5 text-[var(--color-brand-default)]" />
            <h3 className="mt-4 font-sans text-base font-semibold text-[var(--color-fg-default)]">
              ModsFolderWatcher
            </h3>
            <p className="mt-2 m-0 text-sm leading-relaxed text-[var(--color-fg-muted)]">
              Drop a fresh build or remove a JAR in Explorer while paused — debounced file events hot-swap changes automatically.
            </p>
          </div>

          <div className="rounded-2xl border border-[var(--color-border-default)] bg-[var(--color-surface-elevated)] p-7">
            <RefreshCw className="size-5 text-[var(--color-brand-default)]" />
            <h3 className="mt-4 font-sans text-base font-semibold text-[var(--color-fg-default)]">
              Reload &amp; Rejoin
            </h3>
            <p className="mt-2 m-0 text-sm leading-relaxed text-[var(--color-fg-muted)]">
              Inside a singleplayer world or server, Apply triggers safe disconnect to MutoReconnectingScreen and reconnects automatically.
            </p>
          </div>

          <div className="rounded-2xl border border-[var(--color-border-default)] bg-[var(--color-surface-elevated)] p-7">
            <History className="size-5 text-[var(--color-brand-default)]" />
            <h3 className="mt-4 font-sans text-base font-semibold text-[var(--color-fg-default)]">
              History &amp; MutoDevScreen
            </h3>
            <p className="mt-2 m-0 text-sm leading-relaxed text-[var(--color-fg-muted)]">
              Audit past reload durations, read muto-dev.log live, or dry-run MutoApi.runReloadSimulation() right inside the client.
            </p>
          </div>
        </div>
      </section>

      {/* BOTTOM CTA */}
      <section className="border-t border-[var(--color-border-default)] py-24 text-center sm:py-28">
        <div className="mx-auto max-w-3xl px-5 sm:px-8">
          <div className="unified-eyebrow mb-4 justify-center">
            <CheckCircle2 className="size-3.5 text-[var(--color-brand-default)]" />
            <span>Fabric 26.1 – 26.3 // ModMenu Native</span>
          </div>
          <h2 className="text-balance font-serif text-3xl font-normal leading-[1.12] tracking-[-0.02em] text-[var(--color-fg-default)] sm:text-5xl">
            Manage mods like resource packs.
          </h2>
          <div className="mt-8 flex flex-wrap items-center justify-center gap-3">
            <Link
              to="/docs/overview"
              className="inline-flex items-center gap-2 rounded-lg bg-[var(--color-brand-default)] px-5 py-2.5 text-sm font-medium text-white no-underline transition-colors hover:bg-[var(--color-brand-hover)]"
            >
              <span>Read Documentation</span>
              <ArrowRight className="size-4" />
            </Link>
            <a
              href="https://github.com/fy2ne/Muto-Menu"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 rounded-lg border border-[var(--color-border-strong)] px-5 py-2.5 text-sm font-medium text-[var(--color-fg-default)] no-underline transition-colors hover:bg-[var(--color-surface-hover)]"
            >
              <span>fy2ne/Muto-Menu</span>
              <ArrowUpRight className="size-4" />
            </a>
          </div>
        </div>
      </section>
    </main>
  );
}
