import type { VerificationIssueResponse } from "../api/types";

const severityClasses: Record<string, string> = {
  ERROR: "bg-red-100 text-red-700",
  WARNING: "bg-yellow-100 text-yellow-700",
  INFO: "bg-blue-100 text-blue-700",
};

interface Props {
  issues: VerificationIssueResponse[];
}

export default function VerificationIssues({ issues }: Props) {
  if (issues.length === 0) return null;

  return (
    <div className="rounded-lg border border-gray-200 bg-white p-4">
      <h3 className="mb-3 text-sm font-semibold text-gray-700">
        Verification Issues ({issues.length})
      </h3>
      <ul className="space-y-2">
        {issues.map((issue, i) => (
          <li key={i} className="flex items-start gap-2 text-sm">
            <span
              className={`mt-0.5 shrink-0 rounded px-1.5 py-0.5 text-xs font-medium ${severityClasses[issue.severity] ?? severityClasses.INFO}`}
            >
              {issue.severity}
            </span>
            <div>
              <span className="font-medium text-gray-800">{issue.field}:</span>{" "}
              <span className="text-gray-600">{issue.message}</span>
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
}
