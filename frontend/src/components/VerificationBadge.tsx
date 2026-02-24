import type { VerificationResultResponse } from "../api/types";

interface Props {
  verification: VerificationResultResponse | null;
}

export default function VerificationBadge({ verification }: Props) {
  if (!verification) {
    return (
      <span className="inline-flex items-center rounded-full bg-gray-100 px-2.5 py-0.5 text-xs font-medium text-gray-600">
        Unverified
      </span>
    );
  }

  if (verification.valid) {
    return (
      <span className="inline-flex items-center rounded-full bg-green-100 px-2.5 py-0.5 text-xs font-medium text-green-700">
        Valid
      </span>
    );
  }

  return (
    <span className="inline-flex items-center rounded-full bg-red-100 px-2.5 py-0.5 text-xs font-medium text-red-700">
      Invalid
    </span>
  );
}
