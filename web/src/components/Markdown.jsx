import React from "react";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import rehypeSlug from "rehype-slug";
import { AlertCircle, AlertTriangle, Info, Lightbulb } from "lucide-react";
import { CodeBlock } from "./CodeBlock";

const ALERT_RE = /^\s*\[!(NOTE|TIP|IMPORTANT|WARNING|CAUTION|INFO)\]\s*/i;

const ALERTS = {
  NOTE: { title: "Note", icon: Info, accent: "var(--color-fg-muted)" },
  INFO: { title: "Info", icon: Info, accent: "var(--color-fg-muted)" },
  TIP: { title: "Tip", icon: Lightbulb, accent: "var(--color-brand-default)" },
  IMPORTANT: { title: "Important", icon: AlertCircle, accent: "var(--color-brand-default)" },
  WARNING: { title: "Warning", icon: AlertTriangle, accent: "#f59e0b" },
  CAUTION: { title: "Caution", icon: AlertTriangle, accent: "#ef4444" },
};

function parseAlert(children) {
  const arr = React.Children.toArray(children);
  const firstIdx = arr.findIndex((c) => typeof c !== "string" || c.trim().length > 0);
  if (firstIdx === -1) return null;
  const first = arr[firstIdx];
  if (!React.isValidElement(first) || !first.props?.children) return null;
  const inner = React.Children.toArray(first.props.children);
  if (typeof inner[0] !== "string") return null;
  const match = inner[0].match(ALERT_RE);
  if (!match) return null;
  const kind = match[1].toUpperCase();
  const restText = inner[0].slice(match[0].length);
  const newInner = [restText, ...inner.slice(1)].filter(Boolean);
  const nextFirst = newInner.length ? React.cloneElement(first, {}, ...newInner) : null;
  const clean = [...(nextFirst ? [nextFirst] : []), ...arr.slice(firstIdx + 1)];
  return { meta: ALERTS[kind] || ALERTS.NOTE, clean };
}

export function Markdown({ source }) {
  return (
    <div className="md-prose">
      <ReactMarkdown
        remarkPlugins={[remarkGfm]}
        rehypePlugins={[rehypeSlug]}
        components={{
          pre({ children }) {
            return <>{children}</>;
          },
          code({ className, children, ...props }) {
            const match = /language-(\w+)/.exec(className || "");
            const raw = String(children).replace(/\n$/, "");
            if (match || raw.includes("\n")) {
              return <CodeBlock code={raw} lang={match ? match[1] : "text"} />;
            }
            return (
              <code className={className} {...props}>
                {children}
              </code>
            );
          },
          blockquote({ children }) {
            const alert = parseAlert(children);
            if (!alert) {
              return (
                <div className="doc-alert" style={{ "--alert-accent": "var(--color-brand-default)" }}>
                  <div className="doc-alert-body">{children}</div>
                </div>
              );
            }
            const Icon = alert.meta.icon;
            return (
              <div className="doc-alert" style={{ "--alert-accent": alert.meta.accent }}>
                <div className="doc-alert-label">
                  <Icon className="size-3.5" />
                  <span>{alert.meta.title}</span>
                </div>
                <div className="doc-alert-body">{alert.clean}</div>
              </div>
            );
          },
          a({ href, children, ...props }) {
            const ext = href && (href.startsWith("http://") || href.startsWith("https://"));
            return (
              <a
                href={href}
                {...(ext ? { target: "_blank", rel: "noopener noreferrer" } : {})}
                {...props}
              >
                {children}
              </a>
            );
          },
        }}
      >
        {source}
      </ReactMarkdown>
    </div>
  );
}
