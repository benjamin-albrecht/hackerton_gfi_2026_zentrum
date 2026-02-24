package com.gfi.zentrum.parser;

import com.gfi.zentrum.model.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PdfParser {

    public List<Root> parsePdf(String filePath) throws IOException {
        try (PDDocument document = org.apache.pdfbox.Loader.loadPDF(new File(filePath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);
            return parseText(text);
        }
    }

    private List<Root> parseText(String text) {
        List<Root> roots = new ArrayList<>();

        String[] lines = text.split("\\r?\\n");
        Beruf currentVocation = null;
        PruefungsBereich currentSection = null;
        Aufgabe currentSubject = null;
        StringBuilder pendingStruktur = new StringBuilder();

        Pattern vocationPattern = Pattern.compile("^(.*?)\\s*\\((.*?)\\)$");

        List<String> bereichTriggers = Arrays.asList("Schriftliche Prüfung", "Praktische Prüfung",
                "Wirtschafts- und Sozialkunde", "WISO", "Wirtschafts- und Betriebslehre", "Eisenbahnbetrieb",
                "Störungen im Eisenbahnbetrieb", "Abweichungen vom Regelbetrieb", "Prüfen von Triebfahrzeugen",
                "Zug- und Rangierfahrten");
        List<String> aufgabeTriggers = Arrays.asList("Schriftliche Aufgabenstellungen", "Arbeitsaufgabe",
                "Betrieblicher Auftrag", "Prüfungsstück", "Arbeitsprobe", "Systementwurf",
                "Funktions- und Systemanalyse");
        List<String> hilfsmittelTriggers = Arrays.asList("keine", "Taschenrechner", "Dritten", "Formelsammlung",
                "Tabellenbuch", "Zeichenwerkzeuge", "Wörterbuch", "Hilfsmittel");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty() || line.startsWith("Stand:") || line.startsWith("\"Berufe-")
                    || line.startsWith("***1***"))
                continue;

            if (line.startsWith("Ausbildungsberuf:")) {
                if (currentVocation != null && currentVocation.getBeschreibung() != null) {
                    roots.add(new Root(currentVocation));
                }
                currentVocation = new Beruf();
                currentVocation.setPruefungsBereich(new ArrayList<>());
                currentSection = null;
                currentSubject = null;
                pendingStruktur.setLength(0);

                String rest = line.substring("Ausbildungsberuf:".length()).trim();
                if (!rest.isEmpty()) {
                    Matcher m = vocationPattern.matcher(rest);
                    if (m.find()) {
                        currentVocation.setBeschreibung(m.group(1).trim());
                        String codesStr = m.group(2).trim();
                        List<Integer> codes = new ArrayList<>();
                        for (String c : codesStr.split(",\\s*")) {
                            try {
                                codes.add(Integer.parseInt(c));
                            } catch (Exception e) {
                            }
                        }
                        currentVocation.setBerufNr(codes);
                    } else {
                        currentVocation.setBeschreibung(rest);
                        currentVocation.setBerufNr(new ArrayList<>());
                    }
                }
                continue;
            }

            if (currentVocation == null)
                continue;

            boolean isBereich = false;
            for (String trigger : bereichTriggers) {
                if (line.contains(trigger)) {
                    isBereich = true;
                    break;
                }
            }

            if (isBereich) {
                currentSection = new PruefungsBereich();
                currentSection.setName(line);
                currentSection.setAufgaben(new ArrayList<>());
                currentVocation.getPruefungsBereich().add(currentSection);
                currentSubject = null;
                pendingStruktur.setLength(0);
                continue;
            }

            if (currentSection != null) {
                // Ignore headers
                if (line.contains("Prüfungsbereich") || line.contains("Gewichtung") || line.contains("Uhrzeit")
                        || line.contains("Prüfungsteils")
                        || line.contains("Anzahl der Aufgaben") || line.equals("Faches")) {
                    continue;
                }

                boolean isAufgabe = false;
                for (String trigger : aufgabeTriggers) {
                    if (line.startsWith(trigger)) {
                        isAufgabe = true;
                        break;
                    }
                }

                if (isAufgabe) {
                    currentSubject = new Aufgabe();
                    currentSubject.setName(line);
                    currentSubject.setStruktur(pendingStruktur.toString().trim());
                    currentSubject.setHilfmittel("");
                    currentSubject.setTermin(new Termin());
                    currentSection.getAufgaben().add(currentSubject);
                    pendingStruktur.setLength(0); // Reset for next
                    continue;
                }

                if (currentSubject != null) {
                    boolean isHilfsmittel = false;
                    for (String trigger : hilfsmittelTriggers) {
                        if (line.contains(trigger)) {
                            isHilfsmittel = true;
                            break;
                        }
                    }

                    if (line.contains("Min") || line.contains("min") || line.contains("Std")
                            || line.matches(".*\\d{2}:\\d{2}.*") || line.matches(".*\\d{2}\\.\\d{2}\\.\\d{4}.*")) {
                        extractDateAndTime(line, currentSubject.getTermin());
                        currentSubject.setStruktur((currentSubject.getStruktur() + " " + line).trim());
                    } else if (isHilfsmittel) {
                        currentSubject.setHilfmittel((currentSubject.getHilfmittel() + " " + line).trim());
                    } else {
                        currentSubject.setStruktur((currentSubject.getStruktur() + " " + line).trim());
                    }
                } else {
                    pendingStruktur.append(line).append(" ");
                }
            }
        }

        if (currentVocation != null && currentVocation.getBeschreibung() != null) {
            roots.add(new Root(currentVocation));
        }

        // Final pass to trim fields and set defaults
        for (Root root : roots) {
            Beruf beruf = root.getBeruf();
            for (PruefungsBereich bereich : beruf.getPruefungsBereich()) {
                for (Aufgabe aufgabe : bereich.getAufgaben()) {
                    if (aufgabe.getStruktur() != null)
                        aufgabe.setStruktur(aufgabe.getStruktur().replaceAll("\\s+", " ").trim());
                    if (aufgabe.getHilfmittel() != null)
                        aufgabe.setHilfmittel(aufgabe.getHilfmittel().replaceAll("\\s+", " ").trim());
                    Termin termin = aufgabe.getTermin();
                    if (termin.getDatum() == null)
                        termin.setDatum("2026-01-01");
                    if (termin.getUhrzeitvon() == null)
                        termin.setUhrzeitvon("00:00");
                    if (termin.getUhrzeitbis() == null)
                        termin.setUhrzeitbis("00:00");
                    if (termin.getDauer() == null)
                        termin.setDauer(0);
                }
            }
        }

        return roots;
    }

    private void extractDateAndTime(String text, Termin termin) {
        Pattern pDate = Pattern.compile("(\\d{2})\\.(\\d{2})\\.(\\d{4})");
        Matcher mDate = pDate.matcher(text);
        if (mDate.find() && termin.getDatum() == null) {
            termin.setDatum(mDate.group(3) + "-" + mDate.group(2) + "-" + mDate.group(1)); // format: YYYY-MM-DD
        }

        Pattern pTime = Pattern.compile("(\\d{2}:\\d{2})\\s*-\\s*(\\d{2}:\\d{2})");
        Matcher mTime = pTime.matcher(text);
        if (mTime.find()) {
            termin.setUhrzeitvon(mTime.group(1).replace(" ", ""));
            termin.setUhrzeitbis(mTime.group(2).replace(" ", ""));
        }

        Pattern pDuration = Pattern.compile("(\\d+)\\s*(Min|Std|min)");
        Matcher mDur = pDuration.matcher(text);
        if (mDur.find() && (termin.getDauer() == null || termin.getDauer() == 0)) {
            int d = Integer.parseInt(mDur.group(1));
            if (mDur.group(2).equalsIgnoreCase("Std"))
                d *= 60;
            termin.setDauer(d);
        }
    }
}
