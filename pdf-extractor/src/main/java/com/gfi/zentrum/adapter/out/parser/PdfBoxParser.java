package com.gfi.zentrum.adapter.out.parser;

import com.gfi.zentrum.domain.model.*;
import com.gfi.zentrum.domain.port.out.PdfParserPort;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PdfBoxParser implements PdfParserPort {

    private static final Pattern VOCATION_PATTERN = Pattern.compile("^(.*?)\\s*\\((.*?)\\)$");
    private static final Pattern DATE_PATTERN = Pattern.compile("(\\d{2})\\.(\\d{2})\\.(\\d{4})");
    private static final Pattern TIME_PATTERN = Pattern.compile("(\\d{2}:\\d{2})\\s*-\\s*(\\d{2}:\\d{2})");
    private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+)\\s*(Min|Std|min)");

    private static final List<String> BEREICH_TRIGGERS = List.of(
            "Schriftliche Prüfung", "Praktische Prüfung",
            "Wirtschafts- und Sozialkunde", "WISO",
            "Wirtschafts- und Betriebslehre", "Eisenbahnbetrieb",
            "Störungen im Eisenbahnbetrieb", "Abweichungen vom Regelbetrieb",
            "Prüfen von Triebfahrzeugen", "Zug- und Rangierfahrten"
    );

    private static final List<String> AUFGABE_TRIGGERS = List.of(
            "Schriftliche Aufgabenstellungen", "Arbeitsaufgabe",
            "Betrieblicher Auftrag", "Prüfungsstück", "Arbeitsprobe",
            "Systementwurf", "Funktions- und Systemanalyse"
    );

    private static final List<String> HILFSMITTEL_TRIGGERS = List.of(
            "keine", "Taschenrechner", "Dritten", "Formelsammlung",
            "Tabellenbuch", "Zeichenwerkzeuge", "Wörterbuch", "Hilfsmittel"
    );

    @Override
    public List<Beruf> parse(InputStream pdfStream) {
        try (PDDocument document = Loader.loadPDF(pdfStream.readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);
            return parseText(text);
        } catch (IOException e) {
            throw new PdfParsingException("Failed to parse PDF", e);
        }
    }

    private List<Beruf> parseText(String text) {
        List<MutableBeruf> berufe = new ArrayList<>();
        String[] lines = text.split("\\r?\\n");

        MutableBeruf currentVocation = null;
        MutableBereich currentSection = null;
        MutableAufgabe currentSubject = null;
        StringBuilder pendingStruktur = new StringBuilder();

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("Stand:") || line.startsWith("\"Berufe-")
                    || line.startsWith("***1***")) {
                continue;
            }

            if (line.startsWith("Ausbildungsberuf:")) {
                if (currentVocation != null && currentVocation.beschreibung != null) {
                    berufe.add(currentVocation);
                }
                currentVocation = new MutableBeruf();
                currentSection = null;
                currentSubject = null;
                pendingStruktur.setLength(0);

                String rest = line.substring("Ausbildungsberuf:".length()).trim();
                if (!rest.isEmpty()) {
                    Matcher m = VOCATION_PATTERN.matcher(rest);
                    if (m.find()) {
                        currentVocation.beschreibung = m.group(1).trim();
                        String codesStr = m.group(2).trim();
                        for (String c : codesStr.split(",\\s*")) {
                            try {
                                currentVocation.berufNr.add(Integer.parseInt(c));
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    } else {
                        currentVocation.beschreibung = rest;
                    }
                }
                continue;
            }

            if (currentVocation == null) {
                continue;
            }

            if (matchesTrigger(line, BEREICH_TRIGGERS, false)) {
                currentSection = new MutableBereich();
                currentSection.name = line;
                currentVocation.bereiche.add(currentSection);
                currentSubject = null;
                pendingStruktur.setLength(0);
                continue;
            }

            if (currentSection == null) {
                continue;
            }

            if (line.contains("Prüfungsbereich") || line.contains("Gewichtung")
                    || line.contains("Uhrzeit") || line.contains("Prüfungsteils")
                    || line.contains("Anzahl der Aufgaben") || line.equals("Faches")) {
                continue;
            }

            if (matchesTrigger(line, AUFGABE_TRIGGERS, true)) {
                currentSubject = new MutableAufgabe();
                currentSubject.name = line;
                currentSubject.struktur = pendingStruktur.toString().trim();
                currentSection.aufgaben.add(currentSubject);
                pendingStruktur.setLength(0);
                continue;
            }

            if (currentSubject != null) {
                if (containsTimeData(line)) {
                    extractDateAndTime(line, currentSubject);
                    currentSubject.struktur = (currentSubject.struktur + " " + line).trim();
                } else if (matchesTrigger(line, HILFSMITTEL_TRIGGERS, false)) {
                    currentSubject.hilfmittel = (currentSubject.hilfmittel + " " + line).trim();
                } else {
                    currentSubject.struktur = (currentSubject.struktur + " " + line).trim();
                }
            } else {
                pendingStruktur.append(line).append(" ");
            }
        }

        if (currentVocation != null && currentVocation.beschreibung != null) {
            berufe.add(currentVocation);
        }

        return berufe.stream().map(this::toImmutable).toList();
    }

    private boolean matchesTrigger(String line, List<String> triggers, boolean startsWith) {
        for (String trigger : triggers) {
            if (startsWith ? line.startsWith(trigger) : line.contains(trigger)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsTimeData(String line) {
        return line.contains("Min") || line.contains("min") || line.contains("Std")
                || line.matches(".*\\d{2}:\\d{2}.*")
                || line.matches(".*\\d{2}\\.\\d{2}\\.\\d{4}.*");
    }

    private void extractDateAndTime(String text, MutableAufgabe aufgabe) {
        Matcher mDate = DATE_PATTERN.matcher(text);
        if (mDate.find() && aufgabe.datum == null) {
            aufgabe.datum = LocalDate.of(
                    Integer.parseInt(mDate.group(3)),
                    Integer.parseInt(mDate.group(2)),
                    Integer.parseInt(mDate.group(1))
            );
        }

        Matcher mTime = TIME_PATTERN.matcher(text);
        if (mTime.find()) {
            aufgabe.uhrzeitVon = LocalTime.parse(mTime.group(1).replace(" ", ""));
            aufgabe.uhrzeitBis = LocalTime.parse(mTime.group(2).replace(" ", ""));
        }

        Matcher mDur = DURATION_PATTERN.matcher(text);
        if (mDur.find() && aufgabe.dauer == 0) {
            int d = Integer.parseInt(mDur.group(1));
            if (mDur.group(2).equalsIgnoreCase("Std")) {
                d *= 60;
            }
            aufgabe.dauer = d;
        }
    }

    private Beruf toImmutable(MutableBeruf mb) {
        List<PruefungsBereich> bereiche = mb.bereiche.stream()
                .map(this::toImmutableBereich)
                .toList();
        return new Beruf(mb.beschreibung, List.copyOf(mb.berufNr), bereiche);
    }

    private PruefungsBereich toImmutableBereich(MutableBereich ms) {
        List<Aufgabe> aufgaben = ms.aufgaben.stream()
                .map(this::toImmutableAufgabe)
                .toList();
        return new PruefungsBereich(ms.name, aufgaben);
    }

    private Aufgabe toImmutableAufgabe(MutableAufgabe ma) {
        String struktur = ma.struktur != null ? ma.struktur.replaceAll("\\s+", " ").trim() : "";
        String hilfmittel = ma.hilfmittel != null ? ma.hilfmittel.replaceAll("\\s+", " ").trim() : "";
        LocalDate datum = ma.datum != null ? ma.datum : LocalDate.of(2026, 1, 1);
        LocalTime von = ma.uhrzeitVon != null ? ma.uhrzeitVon : LocalTime.MIDNIGHT;
        LocalTime bis = ma.uhrzeitBis != null ? ma.uhrzeitBis : LocalTime.MIDNIGHT;
        Termin termin = new Termin(datum, von, bis, ma.dauer);
        return new Aufgabe(ma.name, struktur, termin, hilfmittel);
    }

    private static class MutableBeruf {
        String beschreibung;
        List<Integer> berufNr = new ArrayList<>();
        List<MutableBereich> bereiche = new ArrayList<>();
    }

    private static class MutableBereich {
        String name;
        List<MutableAufgabe> aufgaben = new ArrayList<>();
    }

    private static class MutableAufgabe {
        String name;
        String struktur = "";
        String hilfmittel = "";
        LocalDate datum;
        LocalTime uhrzeitVon;
        LocalTime uhrzeitBis;
        int dauer;
    }
}
