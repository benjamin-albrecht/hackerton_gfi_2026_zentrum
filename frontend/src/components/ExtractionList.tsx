import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { listExtractions, deleteExtraction } from "../api/client";
import type { ExtractionSummaryResponse } from "../api/types";
import Spinner from "./Spinner";

function formatDate(iso: string) {
  return new Date(iso).toLocaleString("de-DE");
}

export default function ExtractionList() {
  const [items, setItems] = useState<ExtractionSummaryResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = async () => {
    setLoading(true);
    setError(null);
    try {
      setItems(await listExtractions());
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const handleDelete = async (id: string) => {
    if (!confirm("Delete this extraction?")) return;
    try {
      await deleteExtraction(id);
      setItems((prev) => prev.filter((e) => e.id !== id));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Delete failed");
    }
  };

  if (loading) {
    return (
      <div className="flex items-center gap-2 py-12 text-gray-500">
        <Spinner /> Loading extractions...
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-bold text-gray-900">Extractions</h1>
        <Link
          to="/upload"
          className="rounded bg-blue-600 px-4 py-2 text-sm text-white hover:bg-blue-700"
        >
          Upload PDF
        </Link>
      </div>

      {error && (
        <div className="rounded border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
          {error}
        </div>
      )}

      {items.length === 0 ? (
        <div className="rounded border border-gray-200 bg-white px-6 py-12 text-center text-sm text-gray-500">
          No extractions yet. Upload a PDF to get started.
        </div>
      ) : (
        <div className="overflow-hidden rounded-lg border border-gray-200 bg-white">
          <table className="w-full text-sm">
            <thead className="border-b border-gray-200 bg-gray-50">
              <tr>
                <th className="px-4 py-3 text-left font-medium text-gray-600">
                  File Name
                </th>
                <th className="px-4 py-3 text-left font-medium text-gray-600">
                  Date
                </th>
                <th className="px-4 py-3 text-right font-medium text-gray-600">
                  Berufe
                </th>
                <th className="px-4 py-3 text-right font-medium text-gray-600">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {items.map((e) => (
                <tr key={e.id} className="hover:bg-gray-50">
                  <td className="px-4 py-3 font-medium text-gray-900">
                    {e.sourceFileName}
                  </td>
                  <td className="px-4 py-3 text-gray-600">
                    {formatDate(e.extractedAt)}
                  </td>
                  <td className="px-4 py-3 text-right text-gray-600">
                    {e.berufeCount}
                  </td>
                  <td className="px-4 py-3 text-right">
                    <div className="flex items-center justify-end gap-2">
                      <Link
                        to={`/extractions/${e.id}`}
                        className="rounded border border-gray-300 px-3 py-1 text-xs text-gray-700 hover:bg-gray-100"
                      >
                        View
                      </Link>
                      <button
                        type="button"
                        onClick={() => handleDelete(e.id)}
                        className="rounded border border-red-200 px-3 py-1 text-xs text-red-600 hover:bg-red-50"
                      >
                        Delete
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
