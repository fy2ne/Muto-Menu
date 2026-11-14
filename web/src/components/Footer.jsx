import { useId } from "react";
import { Link } from "react-router-dom";
import { Moon, Sun } from "lucide-react";

export function Footer({
  wordmark = "Muto Runtime",
  columns,
  theme,
  onToggleTheme,
  licenseLabel = "Elastic License 2.0 (ELv2)",
  companionHref = "https://mutomenu.fy2ne.me",
  companionLabel = "Muto Menu ↗",
}) {
  const rawId = useId().replace(/:/g, "");
  const fillId = `wm-fill-${rawId}`;
  const rimId = `wm-rim-${rawId}`;

  return (
    <footer className="border-t border-[var(--color-border-default)] bg-[var(--color-canvas-default)]">
      <div className="lp-footer-wordmark-stage" aria-hidden="true">
        <svg viewBox="0 0 1120 215" className="lp-footer-wordmark-svg" role="presentation">
          <defs>
            <radialGradient id={fillId} cx="90%" cy="38%" r="42%" fx="92%" fy="34%">
              <stop offset="0%" stopColor="var(--wm-glow-0)" />
              <stop offset="40%" stopColor="var(--wm-glow-1)" />
              <stop offset="75%" stopColor="var(--wm-glow-2)" />
              <stop offset="100%" stopColor="var(--wm-glow-3)" />
            </radialGradient>
            <radialGradient id={rimId} cx="94%" cy="34%" r="32%" fx="95%" fy="32%">
              <stop offset="0%" stopColor="var(--wm-rim-0)" />
              <stop offset="42%" stopColor="var(--wm-rim-1)" />
              <stop offset="78%" stopColor="var(--wm-rim-2)" />
              <stop offset="100%" stopColor="transparent" />
            </radialGradient>
          </defs>
          <text
            x="50%"
            y="185"
            textAnchor="middle"
            fill={`url(#${fillId})`}
            stroke={`url(#${rimId})`}
            strokeWidth="1.25"
            className="lp-footer-wordmark-svg-text"
          >
            {wordmark}
          </text>
        </svg>
      </div>

      <div className="border-t border-[var(--color-border-muted)] bg-[var(--color-canvas-subtle)]">
        <div className="mx-auto max-w-[1440px] px-5 py-12 sm:px-8 lg:px-10">
          <div className="grid grid-cols-2 gap-8 sm:grid-cols-4">
            {columns.map((col) => (
              <div key={col.title}>
                <h4 className="mb-3 font-mono text-[11px] uppercase tracking-[0.12em] text-[var(--color-fg-subtle)]">
                  {col.title}
                </h4>
                <ul className="m-0 list-none space-y-2 p-0">
                  {col.items.map((item) => {
                    const ext = item.href.startsWith("http");
                    return (
                      <li key={item.label}>
                        {ext ? (
                          <a
                            href={item.href}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="text-[13px] text-[var(--color-fg-muted)] no-underline transition-colors hover:text-[var(--color-fg-default)]"
                          >
                            {item.label}
                          </a>
                        ) : (
                          <Link
                            to={item.href}
                            className="text-[13px] text-[var(--color-fg-muted)] no-underline transition-colors hover:text-[var(--color-fg-default)]"
                          >
                            {item.label}
                          </Link>
                        )}
                      </li>
                    );
                  })}
                </ul>
              </div>
            ))}
          </div>

          <div className="mt-10 flex flex-wrap items-center justify-between gap-4 border-t border-[var(--color-border-default)] pt-6 text-xs text-[var(--color-fg-subtle)]">
            <div className="flex flex-wrap items-center gap-3">
              <span>{wordmark} © {new Date().getFullYear()} fy2ne</span>
              <span>·</span>
              <a
                href={companionHref}
                target="_blank"
                rel="noopener noreferrer"
                className="text-[var(--color-fg-muted)] no-underline hover:text-[var(--color-fg-default)]"
              >
                {companionLabel}
              </a>
            </div>

            <div className="flex items-center gap-4">
              <span>{licenseLabel}</span>
              <button
                type="button"
                onClick={onToggleTheme}
                aria-label="Toggle color theme"
                className="inline-flex items-center gap-1.5 rounded-md border border-[var(--color-border-default)] bg-[var(--color-surface-default)] px-2.5 py-1 font-mono text-[11px] text-[var(--color-fg-muted)] transition-colors hover:text-[var(--color-fg-default)] cursor-pointer"
              >
                {theme === "dark" ? <Sun className="size-3" /> : <Moon className="size-3" />}
                <span>{theme === "dark" ? "Light" : "Dark"}</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </footer>
  );
}
