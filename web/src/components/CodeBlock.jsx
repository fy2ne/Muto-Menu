import { useState } from "react";
import { Check, Copy } from "lucide-react";

const TOKEN_RE =
  /(\/\/[^\n]*|\/\*[\s\S]*?\*\/|#[^\n]*)|("(?:\\.|[^"\\])*"|'(?:\\.|[^'\\])*')|\b(public|private|protected|static|final|void|boolean|int|long|return|import|package|class|interface|var|val|if|else|for|try|catch|new|implementation|compileOnly|dependencies|repositories|maven)\b|\b(MutoApi|MutoEvents|ReloadEngine|ReloadBoundaryManager|DynamicMixinEngine|MutoClassFileTransformer|MutoAgentBootstrap|JarUnlocker|RegistryFreezer|DiContainerGuard|ResourceHotMount|KnotInjector|ModTier|ReloadPlan|ReloadResult|ModDiff|CompletableFuture|FabricLoader|Minecraft|Path|Screen|ModsFolderWatcher|MenuConfig|ModStateManager|true|false|null)\b|\b(\d+(?:\.\d+)?)\b/g;

function highlightLine(line) {
  const out = [];
  let last = 0;
  TOKEN_RE.lastIndex = 0;
  let m;
  while ((m = TOKEN_RE.exec(line)) !== null) {
    if (m.index > last) out.push(line.slice(last, m.index));
    const text = m[0];
    let cls = "";
    if (m[1]) cls = "tok-cm";
    else if (m[2]) cls = "tok-str";
    else if (m[3]) cls = "tok-kw";
    else if (m[4]) cls = "tok-type";
    else if (m[5]) cls = "tok-num";
    out.push(
      <span key={`${m.index}-${text}`} className={cls}>
        {text}
      </span>
    );
    last = TOKEN_RE.lastIndex;
  }
  if (last < line.length) out.push(line.slice(last));
  return out.length ? out : "\u00a0";
}

export function CodeBlock({ code = "", lang = "text", filename }) {
  const [copied, setCopied] = useState(false);
  const lines = String(code).replace(/\n$/, "").split("\n");

  const copy = async () => {
    try {
      await navigator.clipboard.writeText(String(code).trimEnd());
      setCopied(true);
      setTimeout(() => setCopied(false), 1800);
    } catch {
      // clipboard blocked
    }
  };

  return (
    <div className="my-5 overflow-hidden rounded-xl border border-[var(--code-border)] bg-[var(--code-bg)]">
      <div className="flex items-center justify-between border-b border-[var(--code-border)] bg-[var(--code-header-bg)] px-4 py-2">
        <span className="font-mono text-[11px] uppercase tracking-[0.08em] text-[var(--color-fg-subtle)]">
          {filename || lang || "code"}
        </span>
        <button
          type="button"
          onClick={copy}
          className="inline-flex items-center gap-1.5 rounded-md border border-[var(--color-border-default)] bg-transparent px-2 py-0.5 font-mono text-[11px] text-[var(--color-fg-muted)] transition-colors hover:border-[var(--color-border-strong)] hover:text-[var(--color-fg-default)] cursor-pointer"
        >
          {copied ? (
            <>
              <Check className="size-3 text-[var(--color-brand-default)]" />
              <span>Copied</span>
            </>
          ) : (
            <>
              <Copy className="size-3" />
              <span>Copy</span>
            </>
          )}
        </button>
      </div>
      <div className="overflow-x-auto px-4 py-3.5 font-mono text-[13px] leading-[1.68] text-[var(--color-fg-default)]">
        <pre className="m-0 bg-transparent p-0 font-mono">
          <code>
            {lines.map((line, i) => (
              <div key={i} className="whitespace-pre">
                {highlightLine(line)}
              </div>
            ))}
          </code>
        </pre>
      </div>
    </div>
  );
}
