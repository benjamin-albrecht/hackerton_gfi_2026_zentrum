import { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import { getExtraction, verifyExtraction } from "../api/client";
import type { ExtractionDetailResponse } from "../api/types";
import BerufAccordion from "./BerufAccordion";
import VerificationBadge from "./VerificationBadge";
import VerificationIssues from "./VerificationIssues";
import Spinner from "./Spinner";

export default function ExtractionDetail() {
  const { id } = useParams<{ id: string }>();
  const [data, setData] = useState<ExtractionDetailResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [verifying, setVerifying] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    setError(null);
    getExtraction(id)
      .then(setData)
      .catch((err) =>
        setError(err instanceof Error ? err.message : "Failed to load"),
      )
      .finally(() => setLoading(false));
  }, [id]);

  const handleVerify = async () => {
    if (!id) return;
    setVerifying(true);
    setError(null);
    try {
      setData(await verifyExtraction(id));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Verification failed");
    } finally {
      setVerifying(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center gap-2 py-12 text-gray-500">
        <Spinner /> Loading extraction...
      </div>
    );
  }

  if (error && !data) {
    return (
      <div className="space-y-4">
        <Link
          to="/"
          className="text-sm text-blue-600 hover:text-blue-700"
        >
          &larr; Back to list
        </Link>
        <div className="rounded border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
          {error}
        </div>
      </div>
    );
  }

  if (!data) return null;

  return (
    <div className="space-y-6">
      <Link
        to="/"
        className="inline-block text-sm text-blue-600 hover:text-blue-700"
      >
        &larr; Back to list
      </Link>

      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-xl font-bold text-gray-900">
            {data.sourceFileName}
          </h1>
          <p className="mt-1 text-sm text-gray-500">
            Extracted {new Date(data.extractedAt).toLocaleString("de-DE")}
          </p>
        </div>
        <div className="flex items-center gap-3">
          <VerificationBadge verification={data.verification} />
          <button
            type="button"
            onClick={handleVerify}
            disabled={verifying}
            className="flex items-center gap-1.5 rounded bg-blue-600 px-4 py-2 text-sm text-white hover:bg-blue-700 disabled:opacity-50"
          >
            {verifying && <Spinner className="h-4 w-4" />}
            Verify
          </button>
        </div>
      </div>

      {error && (
        <div className="rounded border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
          {error}
        </div>
      )}

      {data.verification && data.verification.issues.length > 0 && (
        <VerificationIssues issues={data.verification.issues} />
      )}

      <div className="space-y-3">
        <h2 className="text-sm font-semibold text-gray-700">
          Berufe ({data.berufe.length})
        </h2>
        {data.berufe.map((b, i) => (
          <BerufAccordion key={i} beruf={b} index={i} />
        ))}
      </div>
    </div>
  );
}
