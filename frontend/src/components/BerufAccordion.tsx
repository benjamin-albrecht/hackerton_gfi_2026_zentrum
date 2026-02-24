import { useState } from "react";
import type { BerufResponse } from "../api/types";

interface Props {
  beruf: BerufResponse;
  index: number;
}

function formatDate(d: string) {
  if (!d) return "-";
  return new Date(d + "T00:00:00").toLocaleDateString("de-DE");
}

function formatTime(t: string) {
  return t ?? "-";
}

export default function BerufAccordion({ beruf, index }: Props) {
  const [open, setOpen] = useState(false);

  return (
    <div className="rounded-lg border border-gray-200 bg-white">
      <button
        type="button"
        onClick={() => setOpen(!open)}
        className="flex w-full items-center justify-between px-4 py-3 text-left hover:bg-gray-50"
      >
        <div className="flex items-center gap-2">
          <span className="text-sm font-semibold text-gray-800">
            {index + 1}. {beruf.beschreibung}
          </span>
          {beruf.berufNr.map((nr) => (
            <span
              key={nr}
              className="rounded bg-blue-100 px-1.5 py-0.5 text-xs font-medium text-blue-700"
            >
              #{nr}
            </span>
          ))}
        </div>
        <svg
          className={`h-4 w-4 text-gray-500 transition-transform ${open ? "rotate-180" : ""}`}
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
          strokeWidth={2}
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            d="M19 9l-7 7-7-7"
          />
        </svg>
      </button>

      {open && (
        <div className="border-t border-gray-100 px-4 py-3 space-y-4">
          {beruf.pruefungsBereich.map((pb, pbIdx) => (
            <div key={pbIdx}>
              <h4 className="mb-2 text-sm font-semibold text-gray-700">
                {pb.name}
              </h4>
              <div className="space-y-3">
                {pb.aufgaben.map((a, aIdx) => (
                  <div
                    key={aIdx}
                    className="rounded border border-gray-100 bg-gray-50 p-3 text-sm"
                  >
                    <p className="font-medium text-gray-800">{a.name}</p>
                    {a.struktur && (
                      <p className="mt-1 text-gray-600">{a.struktur}</p>
                    )}
                    <div className="mt-2 flex flex-wrap gap-x-4 gap-y-1 text-xs text-gray-500">
                      {a.termin && (
                        <>
                          <span>
                            Datum: {formatDate(a.termin.datum)}
                          </span>
                          <span>
                            {formatTime(a.termin.uhrzeitVon)} -{" "}
                            {formatTime(a.termin.uhrzeitBis)}
                          </span>
                          <span>{a.termin.dauer} min</span>
                        </>
                      )}
                      {a.hilfmittel && (
                        <span>Hilfsmittel: {a.hilfmittel}</span>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
