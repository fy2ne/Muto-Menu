import { useEffect, useMemo, useRef, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import {
  ArrowLeft,
  ArrowRight,
  ChevronRight,
  Menu,
  Search,
  X,
} from "lucide-react";
import { Markdown } from "../components/Markdown";

function extractToc(markdown) {
  const headings = [];
  const lines = markdown.split("\n");
  let inFence = false;
  for (const line of lines) {
    if (line.trim().startsWith("```")) {
      inFence = !inFence;
      continue;
    }
    if (inFence) continue;
    const m = /^(#{2,3})\s+(.+?)\s*$/.exec(line);
    if (!m) continue;
    const level = m[1].length;
    const text = m[2].replace(/`([^`]+)`/g, "$1").trim();
    const id = text
      .toLowerCase()
      .replace(/[^\w\s-]/g, "")
      .trim()
      .replace(/\s+/g, "-");
    headings.push({ level, text, id });
  }
  return headings;
}

export function DocsPage({
  sections,
  baseTitle = "Muto Runtime Documentation",
  heroSubtitle = "JVM bytecode retransformation, Knot classloader manipulation, registry thawing, and public developer contracts for Fabric 26.x.",
  eyebrow = "DOCUMENTATION // MUTO ENGINE REFERENCE",
}) {
  const { slug } = useParams();
  const navigate = useNavigate();
  const [searchOpen, setSearchOpen] = useState(false);
  const [query, setQuery] = useState("");
  const [mobileNavOpen, setMobileNavOpen] = useState(false);
  const [activeId, setActiveId] = useState("");
  const articleRef = useRef(null);

  const flatPages = useMemo(
    () =>
      sections.flatMap((sec) =>
        sec.pages.map((p) => ({
          ...p,
          sectionId: sec.id,
          sectionTitle: sec.title,
        }))
      ),
    [sections]
  );

  const currentIdx = slug
    ? flatPages.findIndex((p) => p.slug === slug)
    : -1;
  const activePage = currentIdx >= 0 ? flatPages[currentIdx] : null;
  const prevPage = currentIdx > 0 ? flatPages[currentIdx - 1] : null;
  const nextPage =
    currentIdx >= 0 && currentIdx < flatPages.length - 1
      ? flatPages[currentIdx + 1]
      : null;

  const toc = useMemo(
    () => (activePage ? extractToc(activePage.body) : []),
    [activePage]
  );

  useEffect(() => {
    window.scrollTo({ top: 0, behavior: "auto" });
    setMobileNavOpen(false);
    setActiveId(toc[0]?.id || "");
  }, [slug, toc]);

  useEffect(() => {
    const onKey = (e) => {
      if ((e.metaKey || e.ctrlKey) && e.key.toLowerCase() === "k") {
        e.preventDefault();
        setSearchOpen(true);
      } else if (e.key === "Escape") {
        setSearchOpen(false);
      }
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, []);

  useEffect(() => {
    if (!activePage) return;
    document.title = `${activePage.title} — ${baseTitle}`;
  }, [activePage, baseTitle]);

  useEffect(() => {
    if (!activePage || !toc.length) return;
    const onScroll = () => {
      let current = toc[0]?.id || "";
      for (const h of toc) {
        const el = document.getElementById(h.id);
        if (!el) continue;
        if (el.getBoundingClientRect().top <= 120) {
          current = h.id;
        }
      }
      setActiveId(current);
    };
    window.addEventListener("scroll", onScroll, { passive: true });
    return () => window.removeEventListener("scroll", onScroll);
  }, [activePage, toc]);

  const searchResults = useMemo(() => {
    const q = query.trim().toLowerCase();
    if (!q) return flatPages.slice(0, 7);
    return flatPages.filter(
      (p) =>
        p.title.toLowerCase().includes(q) ||
        p.abstract.toLowerCase().includes(q) ||
        p.body.toLowerCase().includes(q)
    );
  }, [flatPages, query]);

  const sidebarContent = (
    <div className="space-y-6">
      <button
        type="button"
        onClick={() => setSearchOpen(true)}
        className="flex w-full items-center justify-between rounded-lg border border-[var(--color-border-default)] bg-[var(--color-surface-elevated)] px-3 py-2 text-xs text-[var(--color-fg-muted)] transition-colors hover:border-[var(--color-border-strong)] hover:text-[var(--color-fg-default)] cursor-pointer"
      >
        <span className="inline-flex items-center gap-2">
          <Search className="size-3.5" />
          <span>Search docs…</span>
        </span>
        <kbd className="rounded border border-[var(--color-border-default)] bg-[var(--color-surface-default)] px-1.5 py-0.5 font-mono text-[10px] text-[var(--color-fg-subtle)]">
          ⌘K
        </kbd>
      </button>

      <nav aria-label="Documentation sections" className="space-y-6">
        {sections.map((sec) => (
          <div key={sec.id}>
            <div className="mb-2 px-2.5 font-mono text-[10.5px] uppercase tracking-[0.12em] text-[var(--color-fg-subtle)]">
              {sec.title}
            </div>
            <ul className="m-0 list-none space-y-0.5 p-0">
              {sec.pages.map((p) => {
                const isActive = activePage?.slug === p.slug;
                return (
                  <li key={p.slug}>
                    <Link
                      to={`/docs/${p.slug}`}
                      className={`flex items-center justify-between rounded-lg px-2.5 py-1.5 text-[13.5px] no-underline transition-colors ${
                        isActive
                          ? "bg-[var(--color-brand-soft)] font-medium text-[var(--color-fg-default)]"
                          : "text-[var(--color-fg-muted)] hover:bg-[var(--color-surface-hover)] hover:text-[var(--color-fg-default)]"
                      }`}
                    >
                      <span className="truncate">{p.title}</span>
                      {isActive && (
                        <span className="h-1.5 w-1.5 shrink-0 rounded-full bg-[var(--color-brand-default)]" />
                      )}
                    </Link>
                  </li>
                );
              })}
            </ul>
          </div>
        ))}
      </nav>
    </div>
  );

  return (
    <div className="min-h-[calc(100vh-56px)] bg-[var(--color-canvas-default)]">
      {/* Mobile top bar */}
      <div className="flex items-center justify-between border-b border-[var(--color-border-default)] px-4 py-2.5 lg:hidden">
        <button
          type="button"
          onClick={() => setMobileNavOpen(true)}
          className="inline-flex items-center gap-2 rounded-md border border-[var(--color-border-default)] px-2.5 py-1 text-xs text-[var(--color-fg-default)] cursor-pointer"
        >
          <Menu className="size-3.5" />
          <span>{activePage ? activePage.title : "Docs Index"}</span>
        </button>
        <button
          type="button"
          onClick={() => setSearchOpen(true)}
          className="inline-flex items-center gap-1.5 rounded-md border border-[var(--color-border-default)] px-2.5 py-1 text-xs text-[var(--color-fg-muted)] cursor-pointer"
        >
          <Search className="size-3" />
          <span>Search</span>
        </button>
      </div>

      {mobileNavOpen && (
        <div className="fixed inset-0 z-50 flex lg:hidden">
          <div
            className="fixed inset-0 bg-black/60 backdrop-blur-sm"
            onClick={() => setMobileNavOpen(false)}
          />
          <aside className="relative z-10 h-full w-72 overflow-y-auto border-r border-[var(--color-border-default)] bg-[var(--color-canvas-default)] p-5">
            <div className="mb-4 flex items-center justify-between">
              <span className="font-mono text-xs uppercase tracking-[0.1em] text-[var(--color-fg-subtle)]">
                Navigation
              </span>
              <button
                type="button"
                onClick={() => setMobileNavOpen(false)}
                className="rounded p-1 text-[var(--color-fg-muted)] cursor-pointer"
              >
                <X className="size-4" />
              </button>
            </div>
            {sidebarContent}
          </aside>
        </div>
      )}

      <div className="docs-shell">
        <aside className="docs-sidebar hidden lg:block">{sidebarContent}</aside>

        <div className="docs-main">
          <main ref={articleRef} className="docs-article-wrap">
            {!activePage ? (
              <div>
                <div className="unified-eyebrow mb-4">
                  <span className="unified-eyebrow-dot" />
                  <span>{eyebrow}</span>
                </div>
                <h1 className="doc-title-serif">{baseTitle}</h1>
                <p className="mb-8 max-w-xl text-[15.5px] leading-relaxed text-[var(--color-fg-muted)]">
                  {heroSubtitle}
                </p>

                <button
                  type="button"
                  onClick={() => setSearchOpen(true)}
                  className="mb-10 flex w-full items-center justify-between rounded-xl border border-[var(--color-border-default)] bg-[var(--color-surface-elevated)] px-4 py-3 text-sm text-[var(--color-fg-muted)] transition-colors hover:border-[var(--color-border-strong)] cursor-pointer"
                >
                  <span className="inline-flex items-center gap-2.5">
                    <Search className="size-4 text-[var(--color-fg-subtle)]" />
                    <span>Search guides, subsystems, or Java symbols…</span>
                  </span>
                  <kbd className="rounded border border-[var(--color-border-default)] bg-[var(--color-surface-default)] px-2 py-0.5 font-mono text-[11px] text-[var(--color-fg-subtle)]">
                    Ctrl K
                  </kbd>
                </button>

                <div className="space-y-10">
                  {sections.map((sec) => (
                    <section key={sec.id}>
                      <div className="mb-3 font-mono text-[11px] uppercase tracking-[0.12em] text-[var(--color-fg-subtle)]">
                        {sec.title}
                      </div>
                      <div className="grid gap-3 sm:grid-cols-2">
                        {sec.pages.map((p) => (
                          <Link
                            key={p.slug}
                            to={`/docs/${p.slug}`}
                            className="group flex flex-col justify-between rounded-xl border border-[var(--color-border-default)] bg-[var(--color-surface-elevated)] p-5 no-underline transition-colors hover:border-[var(--color-border-strong)] hover:bg-[var(--color-surface-default)]"
                          >
                            <div>
                              <div className="flex items-center justify-between gap-2">
                                <h3 className="m-0 font-sans text-[15px] font-semibold tracking-tight text-[var(--color-fg-default)]">
                                  {p.title}
                                </h3>
                                <ChevronRight className="size-4 text-[var(--color-fg-subtle)] transition-transform group-hover:translate-x-0.5 group-hover:text-[var(--color-brand-default)]" />
                              </div>
                              <p className="mt-2 line-clamp-2 text-[13px] leading-relaxed text-[var(--color-fg-muted)]">
                                {p.abstract}
                              </p>
                            </div>
                          </Link>
                        ))}
                      </div>
                    </section>
                  ))}
                </div>
              </div>
            ) : (
              <article>
                <div className="mb-4 flex items-center gap-2 font-mono text-[12px] text-[var(--color-fg-subtle)]">
                  <Link
                    to="/docs"
                    className="text-[var(--color-fg-muted)] no-underline hover:text-[var(--color-fg-default)]"
                  >
                    Docs
                  </Link>
                  <span>/</span>
                  <span>{activePage.sectionTitle}</span>
                  <span>/</span>
                  <span className="text-[var(--color-fg-default)]">
                    {activePage.title}
                  </span>
                </div>

                <div className="unified-eyebrow mb-4">
                  <span className="unified-eyebrow-dot" />
                  <span>
                    {activePage.sectionTitle} // {activePage.tag || "Reference"}
                  </span>
                </div>

                <h1 className="doc-title-serif">{activePage.title}</h1>

                <div className="doc-abstract">
                  <div className="doc-abstract-label">Summary</div>
                  <p>{activePage.abstract}</p>
                </div>

                <Markdown source={activePage.body} />

                <nav
                  aria-label="Article pagination"
                  className="mt-14 grid gap-3 border-t border-[var(--color-border-default)] pt-7 sm:grid-cols-2"
                >
                  {prevPage ? (
                    <Link
                      to={`/docs/${prevPage.slug}`}
                      className="group flex flex-col gap-1 rounded-xl border border-[var(--color-border-default)] bg-[var(--color-surface-elevated)] p-4 no-underline transition-colors hover:border-[var(--color-border-strong)]"
                    >
                      <span className="inline-flex items-center gap-1.5 font-mono text-[10.5px] uppercase tracking-[0.1em] text-[var(--color-fg-subtle)]">
                        <ArrowLeft className="size-3" /> Previous
                      </span>
                      <span className="font-sans text-sm font-medium text-[var(--color-fg-default)]">
                        {prevPage.title}
                      </span>
                    </Link>
                  ) : (
                    <div />
                  )}

                  {nextPage ? (
                    <Link
                      to={`/docs/${nextPage.slug}`}
                      className="group flex flex-col items-end gap-1 rounded-xl border border-[var(--color-border-default)] bg-[var(--color-surface-elevated)] p-4 text-right no-underline transition-colors hover:border-[var(--color-border-strong)]"
                    >
                      <span className="inline-flex items-center gap-1.5 font-mono text-[10.5px] uppercase tracking-[0.1em] text-[var(--color-fg-subtle)]">
                        Next <ArrowRight className="size-3" />
                      </span>
                      <span className="font-sans text-sm font-medium text-[var(--color-fg-default)]">
                        {nextPage.title}
                      </span>
                    </Link>
                  ) : (
                    <div />
                  )}
                </nav>
              </article>
            )}
          </main>

          {activePage && toc.length > 0 && (
            <aside className="docs-toc" aria-label="On this page">
              <div className="mb-3 font-mono text-[10.5px] uppercase tracking-[0.12em] text-[var(--color-fg-subtle)]">
                On this page
              </div>
              <ul className="m-0 list-none space-y-1.5 border-l border-[var(--color-border-default)] p-0 pl-3">
                {toc.map((h) => (
                  <li key={h.id}>
                    <a
                      href={`#${h.id}`}
                      className={`block text-[12.5px] leading-snug no-underline transition-colors ${
                        h.level === 3 ? "pl-3 text-[12px]" : ""
                      } ${
                        activeId === h.id
                          ? "font-medium text-[var(--color-brand-default)]"
                          : "text-[var(--color-fg-muted)] hover:text-[var(--color-fg-default)]"
                      }`}
                    >
                      {h.text}
                    </a>
                  </li>
                ))}
              </ul>
            </aside>
          )}
        </div>
      </div>

      {/* Ctrl+K Search Modal */}
      {searchOpen && (
        <div className="fixed inset-0 z-50 flex items-start justify-center pt-[14vh] px-4">
          <div
            className="fixed inset-0 bg-black/65 backdrop-blur-sm"
            onClick={() => setSearchOpen(false)}
          />
          <div className="relative z-10 w-full max-w-lg overflow-hidden rounded-2xl border border-[var(--color-border-strong)] bg-[var(--color-surface-default)] shadow-2xl">
            <div className="flex items-center gap-3 border-b border-[var(--color-border-default)] px-4 py-3">
              <Search className="size-4 text-[var(--color-fg-subtle)]" />
              <input
                autoFocus
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === "Enter" && searchResults[0]) {
                    navigate(`/docs/${searchResults[0].slug}`);
                    setSearchOpen(false);
                    setQuery("");
                  }
                }}
                placeholder="Search documentation…"
                className="flex-1 bg-transparent text-sm text-[var(--color-fg-default)] outline-none placeholder:text-[var(--color-fg-subtle)]"
              />
              <button
                type="button"
                onClick={() => setSearchOpen(false)}
                className="rounded border border-[var(--color-border-default)] px-1.5 py-0.5 font-mono text-[10px] text-[var(--color-fg-subtle)] cursor-pointer"
              >
                Esc
              </button>
            </div>
            <div className="max-h-80 overflow-y-auto p-2">
              {searchResults.length === 0 ? (
                <div className="py-10 text-center text-xs text-[var(--color-fg-muted)]">
                  No matches for &ldquo;{query}&rdquo;
                </div>
              ) : (
                searchResults.map((item) => (
                  <button
                    key={item.slug}
                    type="button"
                    onClick={() => {
                      navigate(`/docs/${item.slug}`);
                      setSearchOpen(false);
                      setQuery("");
                    }}
                    className="flex w-full items-center justify-between gap-3 rounded-lg px-3 py-2.5 text-left transition-colors hover:bg-[var(--color-surface-hover)] cursor-pointer"
                  >
                    <div className="min-w-0">
                      <div className="truncate text-[13.5px] font-medium text-[var(--color-fg-default)]">
                        {item.title}
                      </div>
                      <div className="truncate text-xs text-[var(--color-fg-muted)]">
                        {item.abstract}
                      </div>
                    </div>
                    <span className="shrink-0 font-mono text-[10px] uppercase text-[var(--color-fg-subtle)]">
                      {item.sectionTitle}
                    </span>
                  </button>
                ))
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
