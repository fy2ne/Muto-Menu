import { useEffect, useState } from "react";
import { BrowserRouter, Navigate, Route, Routes, useLocation } from "react-router-dom";
import { Navbar } from "./components/Navbar";
import { Footer } from "./components/Footer";
import { MenuLanding } from "./pages/MenuLanding";
import { DocsPage } from "./pages/DocsPage";
import { MENU_DOC_SECTIONS } from "./data/menuDocs";

const MENU_FOOTER_COLUMNS = [
  {
    title: "Documentation",
    items: [
      { label: "Overview", href: "/docs/overview" },
      { label: "Installation", href: "/docs/installation" },
      { label: "ModMenu Cards & Badges", href: "/docs/modmenu-integration" },
      { label: "Dependency Protection", href: "/docs/dependency-warnings" },
    ],
  },
  {
    title: "Automation",
    items: [
      { label: "Folder Auto-Watcher", href: "/docs/folder-watcher" },
      { label: "In-World Reload & Rejoin", href: "/docs/world-rejoin" },
      { label: "History & Dev Screen", href: "/docs/history-dev" },
      { label: "muto-menu.json Config", href: "/docs/configuration" },
    ],
  },
  {
    title: "Runtime Engine",
    items: [
      { label: "Muto Runtime ↗", href: "https://muto.fy2ne.me" },
      { label: "Runtime Docs ↗", href: "https://muto.fy2ne.me/docs" },
      { label: "MutoApi Reference ↗", href: "https://muto.fy2ne.me/docs/muto-api" },
    ],
  },
  {
    title: "Source",
    items: [
      { label: "GitHub Repository ↗", href: "https://github.com/fy2ne/Muto-Menu" },
      { label: "Issue Tracker ↗", href: "https://github.com/fy2ne/Muto-Menu/issues" },
      { label: "Muto Runtime GitHub ↗", href: "https://github.com/fy2ne/Muto_runtime" },
    ],
  },
];

function AppShell() {
  const { pathname } = useLocation();
  const inDocs = pathname.startsWith("/docs");
  const [theme, setTheme] = useState(() => {
    try {
      return localStorage.getItem("mutomenu-theme") || "dark";
    } catch {
      return "dark";
    }
  });

  useEffect(() => {
    document.documentElement.setAttribute("data-theme", theme);
    document.documentElement.classList.toggle("dark", theme === "dark");
    document.documentElement.classList.toggle("light", theme !== "dark");
    try {
      localStorage.setItem("mutomenu-theme", theme);
    } catch {
      // ignore storage failure
    }
  }, [theme]);

  useEffect(() => {
    if (!inDocs) {
      document.title = "Muto Menu — In-Game Mod Toggling for Fabric";
    }
  }, [inDocs]);

  const toggleTheme = () => setTheme((prev) => (prev === "dark" ? "light" : "dark"));

  return (
    <div className="flex min-h-screen flex-col justify-between bg-[var(--color-canvas-default)] text-[var(--color-fg-default)]">
      <Navbar
        brandName="Muto Menu"
        logoSrc="/logo-nobg.png"
        docsPath="/docs"
      />

      <Routes>
        <Route path="/" element={<MenuLanding />} />
        <Route
          path="/docs"
          element={
            <DocsPage
              sections={MENU_DOC_SECTIONS}
              baseTitle="Muto Menu Documentation"
              heroSubtitle="In-game ModMenu integration, dependency checks, folder watching, and config reference for Muto Menu."
              eyebrow="DOCUMENTATION // MUTO MENU GUIDE"
            />
          }
        />
        <Route
          path="/docs/:slug"
          element={
            <DocsPage
              sections={MENU_DOC_SECTIONS}
              baseTitle="Muto Menu Documentation"
              heroSubtitle="In-game ModMenu integration, dependency checks, folder watching, and config reference for Muto Menu."
              eyebrow="DOCUMENTATION // MUTO MENU GUIDE"
            />
          }
        />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>

      <Footer
        wordmark="Muto Menu"
        columns={MENU_FOOTER_COLUMNS}
        theme={theme}
        onToggleTheme={toggleTheme}
        licenseLabel="Requires Muto Runtime (~0.1.0-beta)"
        companionHref="https://muto.fy2ne.me"
        companionLabel="Muto Runtime ↗"
      />
    </div>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <AppShell />
    </BrowserRouter>
  );
}
