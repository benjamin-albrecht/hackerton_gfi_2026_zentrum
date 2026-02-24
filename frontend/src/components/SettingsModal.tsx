import { useState } from "react";
import { useSettings } from "../context/SettingsContext";

interface Props {
  open: boolean;
  onClose: () => void;
}

export default function SettingsModal({ open, onClose }: Props) {
  const { settings, save, clear } = useSettings();
  const [apiKey, setApiKey] = useState(settings.anthropicApiKey);
  const [baseUrl, setBaseUrl] = useState(settings.anthropicBaseUrl);

  if (!open) return null;

  const handleSave = () => {
    save({ anthropicApiKey: apiKey, anthropicBaseUrl: baseUrl });
    onClose();
  };

  const handleClear = () => {
    clear();
    setApiKey("");
    setBaseUrl("");
  };

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/40"
      onClick={onClose}
    >
      <div
        className="w-full max-w-md rounded-lg bg-white p-6 shadow-xl"
        onClick={(e) => e.stopPropagation()}
      >
        <h2 className="mb-4 text-lg font-semibold text-gray-800">Settings</h2>

        <label className="mb-1 block text-sm font-medium text-gray-700">
          Anthropic API Key
        </label>
        <input
          type="password"
          value={apiKey}
          onChange={(e) => setApiKey(e.target.value)}
          placeholder="sk-ant-..."
          className="mb-4 w-full rounded border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
        />

        <label className="mb-1 block text-sm font-medium text-gray-700">
          Base URL
        </label>
        <input
          type="text"
          value={baseUrl}
          onChange={(e) => setBaseUrl(e.target.value)}
          placeholder="https://api.anthropic.com/"
          className="mb-6 w-full rounded border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
        />

        <div className="flex items-center justify-between">
          <button
            type="button"
            onClick={handleClear}
            className="text-sm text-red-600 hover:text-red-700"
          >
            Clear Settings
          </button>
          <div className="flex gap-2">
            <button
              type="button"
              onClick={onClose}
              className="rounded border border-gray-300 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50"
            >
              Cancel
            </button>
            <button
              type="button"
              onClick={handleSave}
              className="rounded bg-blue-600 px-4 py-2 text-sm text-white hover:bg-blue-700"
            >
              Save
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
