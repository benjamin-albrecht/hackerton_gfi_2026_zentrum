import {
  createContext,
  useContext,
  useState,
  useCallback,
  type ReactNode,
} from "react";
import type { AppSettings } from "../api/types";

const SETTINGS_KEY = "zentrum-settings";

const defaults: AppSettings = { anthropicApiKey: "", anthropicBaseUrl: "" };

function load(): AppSettings {
  try {
    const raw = localStorage.getItem(SETTINGS_KEY);
    if (raw) return JSON.parse(raw) as AppSettings;
  } catch {
    /* ignore */
  }
  return { ...defaults };
}

interface SettingsContextValue {
  settings: AppSettings;
  save: (s: AppSettings) => void;
  clear: () => void;
  hasApiKey: boolean;
}

const Ctx = createContext<SettingsContextValue | null>(null);

export function SettingsProvider({ children }: { children: ReactNode }) {
  const [settings, setSettings] = useState<AppSettings>(load);

  const save = useCallback((s: AppSettings) => {
    localStorage.setItem(SETTINGS_KEY, JSON.stringify(s));
    setSettings(s);
  }, []);

  const clear = useCallback(() => {
    localStorage.removeItem(SETTINGS_KEY);
    setSettings({ ...defaults });
  }, []);

  const hasApiKey = settings.anthropicApiKey.length > 0;

  return (
    <Ctx.Provider value={{ settings, save, clear, hasApiKey }}>
      {children}
    </Ctx.Provider>
  );
}

export function useSettings() {
  const ctx = useContext(Ctx);
  if (!ctx) throw new Error("useSettings must be inside SettingsProvider");
  return ctx;
}
