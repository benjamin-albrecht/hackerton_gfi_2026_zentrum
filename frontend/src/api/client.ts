import type {
  ExtractionCreatedResponse,
  ExtractionSummaryResponse,
  ExtractionDetailResponse,
  AppSettings,
} from "./types";

const BASE = "/api/v1/extractions";
const SETTINGS_KEY = "zentrum-settings";

function loadSettings(): AppSettings {
  try {
    const raw = localStorage.getItem(SETTINGS_KEY);
    if (raw) return JSON.parse(raw) as AppSettings;
  } catch {
    /* ignore parse errors */
  }
  return { anthropicApiKey: "", anthropicBaseUrl: "" };
}

function authHeaders(): HeadersInit {
  const s = loadSettings();
  const h: Record<string, string> = {};
  if (s.anthropicApiKey) h["X-Anthropic-Api-Key"] = s.anthropicApiKey;
  if (s.anthropicBaseUrl) h["X-Anthropic-Base-Url"] = s.anthropicBaseUrl;
  return h;
}

async function request<T>(url: string, init?: RequestInit): Promise<T> {
  const res = await fetch(url, {
    ...init,
    headers: { ...authHeaders(), ...init?.headers },
  });
  if (!res.ok) {
    const body = await res.json().catch(() => null);
    const msg = body?.message ?? res.statusText;
    throw new Error(msg);
  }
  if (res.status === 204) return undefined as T;
  return res.json() as Promise<T>;
}

export function uploadPdf(file: File): Promise<ExtractionCreatedResponse> {
  const form = new FormData();
  form.append("file", file);
  return request<ExtractionCreatedResponse>(BASE, {
    method: "POST",
    body: form,
  });
}

export function listExtractions(): Promise<ExtractionSummaryResponse[]> {
  return request<ExtractionSummaryResponse[]>(BASE);
}

export function getExtraction(id: string): Promise<ExtractionDetailResponse> {
  return request<ExtractionDetailResponse>(`${BASE}/${id}`);
}

export function verifyExtraction(
  id: string,
): Promise<ExtractionDetailResponse> {
  return request<ExtractionDetailResponse>(`${BASE}/${id}/verify`, {
    method: "POST",
  });
}

export function deleteExtraction(id: string): Promise<void> {
  return request<void>(`${BASE}/${id}`, { method: "DELETE" });
}
