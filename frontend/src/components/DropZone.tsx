import { useState, useCallback, type DragEvent, useRef } from "react";

interface Props {
  onFile: (file: File) => void;
  disabled?: boolean;
}

export default function DropZone({ onFile, disabled }: Props) {
  const [over, setOver] = useState(false);
  const inputRef = useRef<HTMLInputElement>(null);

  const prevent = (e: DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
  };

  const handleDrop = useCallback(
    (e: DragEvent) => {
      prevent(e);
      setOver(false);
      const file = e.dataTransfer.files[0];
      if (file && file.type === "application/pdf") onFile(file);
    },
    [onFile],
  );

  return (
    <div
      onDragOver={(e) => {
        prevent(e);
        setOver(true);
      }}
      onDragLeave={(e) => {
        prevent(e);
        setOver(false);
      }}
      onDrop={handleDrop}
      onClick={() => !disabled && inputRef.current?.click()}
      className={`flex cursor-pointer flex-col items-center justify-center rounded-lg border-2 border-dashed p-12 transition-colors ${
        disabled
          ? "border-gray-200 bg-gray-50 cursor-not-allowed"
          : over
            ? "border-blue-500 bg-blue-50"
            : "border-gray-300 bg-white hover:border-gray-400"
      }`}
    >
      <svg
        className="mb-3 h-10 w-10 text-gray-400"
        fill="none"
        viewBox="0 0 24 24"
        stroke="currentColor"
        strokeWidth={1.5}
      >
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          d="M3 16.5v2.25A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75V16.5m-13.5-9L12 3m0 0l4.5 4.5M12 3v13.5"
        />
      </svg>
      <p className="text-sm text-gray-600">
        Drag &amp; drop a PDF file here, or click to select
      </p>
      <p className="mt-1 text-xs text-gray-400">PDF files only</p>
      <input
        ref={inputRef}
        type="file"
        accept="application/pdf"
        className="hidden"
        disabled={disabled}
        onChange={(e) => {
          const file = e.target.files?.[0];
          if (file) onFile(file);
          e.target.value = "";
        }}
      />
    </div>
  );
}
