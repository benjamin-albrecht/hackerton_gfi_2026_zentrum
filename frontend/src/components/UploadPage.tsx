import { useState } from "react";
import { useNavigate } from "react-router-dom";
import DropZone from "./DropZone";
import Spinner from "./Spinner";
import { uploadPdf } from "../api/client";

export default function UploadPage() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleFile = async (file: File) => {
    setLoading(true);
    setError(null);
    try {
      const res = await uploadPdf(file);
      navigate(`/extractions/${res.id}`);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Upload failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-bold text-gray-900">Upload PDF</h1>
      <DropZone onFile={handleFile} disabled={loading} />
      {loading && (
        <div className="flex items-center gap-2 text-sm text-gray-600">
          <Spinner />
          Extracting data from PDF...
        </div>
      )}
      {error && (
        <div className="rounded border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
          {error}
        </div>
      )}
    </div>
  );
}
