package com.gfi.zentrum.adapter.out.parser;

import com.gfi.zentrum.domain.model.Beruf;
import com.gfi.zentrum.domain.model.PdfParsingException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PdfBoxParserTest {

    private final PdfBoxParser parser = new PdfBoxParser();

    @Test
    void parseRealPdfExtractsBerufe() throws IOException {
        Path pdfPath = Path.of("../doc/berufe/gesamt-bpue-w25-data.pdf");
        if (!Files.exists(pdfPath)) {
            return; // skip if PDF not available
        }

        try (InputStream is = Files.newInputStream(pdfPath)) {
            List<Beruf> berufe = parser.parse(is);

            assertFalse(berufe.isEmpty(), "Should extract at least one Beruf");

            Beruf first = berufe.getFirst();
            assertNotNull(first.beschreibung());
            assertFalse(first.beschreibung().isBlank());
            assertNotNull(first.berufNr());
            assertNotNull(first.pruefungsBereich());
        }
    }

    @Test
    void parseInvalidInputThrowsPdfParsingException() {
        InputStream invalid = new ByteArrayInputStream("not a pdf".getBytes());
        assertThrows(PdfParsingException.class, () -> parser.parse(invalid));
    }
}
