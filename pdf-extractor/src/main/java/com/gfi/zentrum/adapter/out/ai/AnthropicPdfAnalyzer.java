package com.gfi.zentrum.adapter.out.ai;

import com.gfi.zentrum.domain.model.Beruf;
import com.gfi.zentrum.domain.port.out.AiPdfAnalyzerPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AnthropicPdfAnalyzer implements AiPdfAnalyzerPort {

    private static final Logger log = LoggerFactory.getLogger(AnthropicPdfAnalyzer.class);

    private static final String SYSTEM_PROMPT = """
            You are a specialist for extracting structured data from German IHK (Industrie- und Handelskammer) \
            vocational training examination PDFs ("Berufe-Prüfungstermine").

            Extract ALL vocational professions (Berufe) with their examination details from the provided text.

            For each Beruf, extract:
            - beschreibung: The full name of the profession (e.g. "Anlagenmechaniker/-in")
            - berufNr: List of numeric profession codes (e.g. [210, 211])
            - pruefungsBereich: List of examination areas, each containing:
              - name: Name of the examination area (e.g. "Schriftliche Prüfung", "Praktische Prüfung")
              - aufgaben: List of tasks/subjects within this area, each with:
                - name: Task name (e.g. "Schriftliche Aufgabenstellungen", "Arbeitsaufgabe")
                - struktur: Description of the task structure and content
                - termin: Examination schedule with:
                  - datum: Date in ISO format (YYYY-MM-DD)
                  - uhrzeitVon: Start time (HH:MM)
                  - uhrzeitBis: End time (HH:MM)
                  - dauer: Duration in minutes
                - hilfmittel: Allowed aids/tools (e.g. "Taschenrechner", "keine")

            Rules:
            - Extract ALL professions found in the text, not just the first one
            - If a date is not explicitly stated, use 2026-01-01 as default
            - If times are not specified, use 00:00 as default
            - If duration is in hours (Std), convert to minutes
            - Keep German text as-is, do not translate
            - Return the result as a JSON array of Beruf objects
            """;

    private final ChatClient chatClient;

    public AnthropicPdfAnalyzer(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .build();
    }

    @Override
    public List<Beruf> analyze(String pdfText) {
        log.info("Analyzing PDF text with AI ({} characters)", pdfText.length());

        List<Beruf> berufe = chatClient.prompt()
                .user(pdfText)
                .call()
                .entity(new ParameterizedTypeReference<>() {});

        log.info("AI extracted {} Berufe", berufe.size());
        return berufe;
    }
}
