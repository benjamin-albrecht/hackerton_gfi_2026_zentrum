#!/usr/bin/env python3
import argparse
import json
from pathlib import Path
import re

import pdfplumber


def _block_after_keyword(text: str, keyword: str, max_lines: int = 6) -> str:
    idx = text.lower().find(keyword.lower())
    if idx == -1:
        return ""
    tail = text[idx:]
    lines = [ln.strip() for ln in tail.splitlines() if ln.strip()]
    if len(lines) <= 1:
        return ""
    return " ".join(lines[1:1+max_lines]).strip()


def parse_fields(text: str) -> dict:
    parsed = {}
    m = re.search(r"Ausbildungsberuf:\s*(.+)", text)
    if m:
        val = m.group(1).strip()
        if "(" in val and ")" in val:
            name, codes = val.split("(", 1)
            parsed["beruf"] = name.strip()
            parsed["codes"] = codes.strip().rstrip(")").strip()
        else:
            parsed["beruf"] = val

    m = re.search(r"Abschlussprüfung\s*(Teil\s*\d)", text, re.IGNORECASE)
    if m:
        parsed["abschlusssteil"] = m.group(1)

    dates = re.findall(r"\d{2}\.\d{2}\.\d{4}", text)
    if dates:
        parsed["dates"] = sorted(set(dates))

    m = re.search(r"(\d+\s*-?\s*(?:jährige|Jahre|Jahr))", text, re.IGNORECASE)
    if m:
        parsed["ausbildungsdauer"] = m.group(1)

    m = re.search(r"Voraussetz[a-z]*[:\s]*([\s\S]{0,300})", text, re.IGNORECASE)
    if m:
        parsed["voraussetzungen"] = m.group(1).strip().splitlines()[0].strip()
    else:
        m = re.search(r"Zugangsvoraussetzungen[:\s]*([\s\S]{0,300})", text, re.IGNORECASE)
        if m:
            parsed["voraussetzungen"] = m.group(1).strip().splitlines()[0].strip()

    hilf = _block_after_keyword(text, "Hilfsmittel")
    if hilf:
        parsed["hilfsmittel"] = hilf

    lines = [ln.strip() for ln in text.splitlines() if ln.strip()]
    parsed.setdefault("summary", " ".join(lines[:3]))

    return parsed


def extract_per_page(pdf_path: Path):
    records = []
    with pdfplumber.open(pdf_path) as pdf:
        for i, page in enumerate(pdf.pages, start=1):
            text = page.extract_text() or ""
            lines = [ln.strip() for ln in text.splitlines() if ln.strip()]
            title = lines[0] if lines else ""
            parsed = parse_fields(text)
            record = {
                "id": f"{pdf_path.name}#p{i}",
                "source_file": str(pdf_path),
                "page": i,
                "title": title,
                "text": text,
                "parsed": parsed,
            }
            records.append(record)
    return records


def build_schema_object(parsed: dict, text: str) -> dict:
    # Build an object matching schema.json: { "beruf": {beschreibung, berufNr, prüfungsBereich}} 
    beruf = {}
    # beschreibung: use parsed summary or beruf name
    beruf["beschreibung"] = parsed.get("summary") or parsed.get("beruf") or ""

    # berufNr: parse codes into integers
    nums = []
    codes = parsed.get("codes")
    if codes:
        for part in re.split(r"[,;\s]+", codes):
            part = part.strip()
            if part.isdigit():
                nums.append(int(part))
            else:
                # try extract digits
                m = re.search(r"(\d{3,4})", part)
                if m:
                    nums.append(int(m.group(1)))
    beruf["berufNr"] = nums

    def to_iso(d: str) -> str:
        # convert DD.MM.YYYY -> YYYY-MM-DD
        m = re.match(r"(\d{2})\.(\d{2})\.(\d{4})", d or "")
        if m:
            return f"{m.group(3)}-{m.group(2)}-{m.group(1)}"
        return d or ""

    pruefungsBereich = []
    # detect major sections
    for area_name in ("Schriftliche Prüfung", "Praktische Prüfung", "Abschlussprüfung", "Prüfung"):
        if area_name.lower() in text.lower():
            # build a single aufgabe entry for this area
            aufgaben = []
            # try to find a date and time range nearby
            datum = None
            u_von = None
            u_bis = None
            dauer = None
            # date
            dates = parsed.get("dates") or []
            if dates:
                datum = to_iso(dates[0])
            # time range pattern HH:MM - HH:MM
            m = re.search(r"(\d{1,2}:\d{2})\s*-\s*(\d{1,2}:\d{2})", text)
            if m:
                u_von = m.group(1)
                u_bis = m.group(2)
            # duration in minutes
            m2 = re.search(r"(\d{1,3})\s*Min", text)
            if m2:
                try:
                    dauer = int(m2.group(1))
                except Exception:
                    dauer = 0

            aufgabe = {
                "name": area_name,
                "struktur": "unstructured",
                "termin": {
                    "datum": datum or "",
                    "uhrzeitvon": u_von or "",
                    "uhrzeitbis": u_bis or "",
                    "dauer": int(dauer or 0),
                },
            }
            # hilfsmittel
            if parsed.get("hilfsmittel"):
                aufgabe["hilfmittel"] = parsed.get("hilfsmittel")
            aufgaben.append(aufgabe)

            pruefungsBereich.append({"name": area_name, "aufgaben": aufgaben})

    # fallback: if nothing found, create a generic Prüfung block
    if not pruefungsBereich:
        aufgabe = {
            "name": "Prüfung",
            "struktur": "unstructured",
            "termin": {
                "datum": (parsed.get("dates") or [""])[0] if parsed.get("dates") else "",
                "uhrzeitvon": "",
                "uhrzeitbis": "",
                "dauer": 0,
            },
        }
        if parsed.get("hilfsmittel"):
            aufgabe["hilfmittel"] = parsed.get("hilfsmittel")
        pruefungsBereich.append({"name": "Prüfung", "aufgaben": [aufgabe]})

    beruf["prüfungsBereich"] = pruefungsBereich
    return {"beruf": beruf}


def find_pdfs(folder: Path):
    return sorted([p for p in folder.iterdir() if p.suffix.lower() == ".pdf"])


def main():
    p = argparse.ArgumentParser(description="Extract one JSON record per PDF page (one Beruf per page).")
    p.add_argument("--input", "-i", default="doc/berufe", help="Input folder with PDFs")
    p.add_argument("--output", "-o", default="doc/berufe/berufe.json", help="Output JSON file")
    args = p.parse_args()

    input_folder = Path(args.input)
    output_file = Path(args.output)

    if not input_folder.exists():
        print(f"Input folder not found: {input_folder}")
        raise SystemExit(1)

    pdfs = find_pdfs(input_folder)
    all_schema_objects = []
    for pdf in pdfs:
        print(f"Processing {pdf}...")
        recs = extract_per_page(pdf)
        for r in recs:
            obj = build_schema_object(r.get("parsed", {}), r.get("text", ""))
            all_schema_objects.append(obj)

    output_file.parent.mkdir(parents=True, exist_ok=True)
    # write as array of schema objects (each has top-level 'beruf')
    with output_file.open("w", encoding="utf-8") as fh:
        json.dump(all_schema_objects, fh, ensure_ascii=False, indent=2)

    print(f"Wrote {len(all_schema_objects)} records to {output_file}")


if __name__ == "__main__":
    main()
