import { Link, useLocation } from "react-router-dom";
import { BookOpen, ChevronRight } from "lucide-react";

export function Navbar({
  brandName = "Muto",
  brandTag = "RUNTIME",
  logoSrc = "/logo-runtime.png",
  logoClass = "",
  docsPath = "/docs",
}) {
  const { pathname } = useLocation();
  const inDocs = pathname.startsWith("/docs");

  return (
    <header className="sticky top-0 z-40 h-14 w-full border-b border-[var(--color-border-default)] bg-[var(--color-canvas-default)]/80 backdrop-blur-xl">
      <div className="mx-auto flex h-full max-w-[1440px] items-center justify-between px-5 sm:px-8 lg:px-10">
        <Link
          to="/"
          className="group inline-flex items-center gap-2.5 text-[var(--color-fg-default)] no-underline"
        >
          <img
            src={logoSrc}
            alt={brandName}
            className={`h-7 w-7 object-contain transition-transform duration-200 group-hover:scale-105 ${logoClass}`}
          />
          <span className="font-sans text-[15px] font-semibold tracking-[-0.02em] text-[var(--color-fg-default)]">
            {brandName}
          </span>
          {brandTag && (
            <span className="rounded border border-[var(--color-border-default)] bg-[var(--color-surface-elevated)] px-1.5 py-0.5 font-mono text-[10px] tracking-[0.08em] text-[var(--color-fg-muted)]">
              {brandTag}
            </span>
          )}
        </Link>

        <nav aria-label="Primary navigation">
          <Link
            to={docsPath}
            className={`inline-flex items-center gap-2 rounded-lg px-3.5 py-1.5 text-[13px] font-medium transition-all duration-150 no-underline ${
              inDocs
                ? "border border-[var(--color-brand-default)]/40 bg-[var(--color-brand-soft)] text-[var(--color-fg-default)]"
                : "bg-[var(--color-brand-default)] text-white hover:bg-[var(--color-brand-hover)] shadow-sm"
            }`}
          >
            <BookOpen className="size-3.5" />
            <span>Documentation</span>
            {!inDocs && <ChevronRight className="size-3.5 opacity-80" />}
          </Link>
        </nav>
      </div>
    </header>
  );
}
