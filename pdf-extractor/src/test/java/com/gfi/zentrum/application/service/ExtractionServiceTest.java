package com.gfi.zentrum.application.service;

import com.gfi.zentrum.domain.model.*;
import com.gfi.zentrum.domain.port.out.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExtractionServiceTest {

    @Mock
    private PdfTextExtractorPort textExtractor;

    @Mock
    private AiPdfAnalyzerPort aiAnalyzer;

    @Mock
    private PdfParserPort fallbackParser;

    @Mock
    private ExtractionRepository repository;

    @Mock
    private McpVerificationPort mcpVerifier;

    private ExtractionService service;

    @BeforeEach
    void setUp() {
        service = new ExtractionService(textExtractor, aiAnalyzer, fallbackParser, repository, mcpVerifier);
    }

    @Test
    void extractUsesAiAnalyzerAndVerifies() {
        String pdfText = "Ausbildungsberuf: Test Beruf (123)";
        List<Beruf> berufe = List.of(
                new Beruf("Test Beruf", List.of(123), List.of())
        );
        VerificationResult verification = new VerificationResult(true, List.of(), Instant.now());

        when(textExtractor.extractText(any(InputStream.class))).thenReturn(pdfText);
        when(aiAnalyzer.analyze(pdfText)).thenReturn(berufe);
        when(mcpVerifier.verify(berufe)).thenReturn(verification);
        when(repository.save(any(ExtractionResult.class))).thenAnswer(inv -> inv.getArgument(0));

        InputStream stream = new ByteArrayInputStream(new byte[0]);
        ExtractionResult result = service.extract(stream, "test.pdf");

        assertNotNull(result.id());
        assertEquals("test.pdf", result.sourceFileName());
        assertEquals(1, result.berufe().size());
        assertEquals("Test Beruf", result.berufe().getFirst().beschreibung());
        assertNotNull(result.verification());
        assertTrue(result.verification().valid());

        verify(textExtractor).extractText(stream);
        verify(aiAnalyzer).analyze(pdfText);
        verify(mcpVerifier).verify(berufe);
        verifyNoInteractions(fallbackParser);
        verify(repository).save(any(ExtractionResult.class));
    }

    @Test
    void extractFallsBackToParserWhenAiFails() {
        String pdfText = "Ausbildungsberuf: Test Beruf (123)";
        List<Beruf> berufe = List.of(
                new Beruf("Test Beruf", List.of(123), List.of())
        );
        when(textExtractor.extractText(any(InputStream.class))).thenReturn(pdfText);
        when(aiAnalyzer.analyze(pdfText)).thenThrow(new RuntimeException("API unavailable"));
        when(fallbackParser.parse(any(InputStream.class))).thenReturn(berufe);
        when(mcpVerifier.verify(berufe)).thenReturn(null);
        when(repository.save(any(ExtractionResult.class))).thenAnswer(inv -> inv.getArgument(0));

        InputStream stream = new ByteArrayInputStream(new byte[0]);
        ExtractionResult result = service.extract(stream, "test.pdf");

        assertNotNull(result.id());
        assertEquals(1, result.berufe().size());

        verify(aiAnalyzer).analyze(pdfText);
        verify(fallbackParser).parse(any(InputStream.class));
    }

    @Test
    void extractContinuesWhenVerificationFails() {
        String pdfText = "Ausbildungsberuf: Test Beruf (123)";
        List<Beruf> berufe = List.of(
                new Beruf("Test Beruf", List.of(123), List.of())
        );
        when(textExtractor.extractText(any(InputStream.class))).thenReturn(pdfText);
        when(aiAnalyzer.analyze(pdfText)).thenReturn(berufe);
        when(mcpVerifier.verify(berufe)).thenThrow(new RuntimeException("MCP server down"));
        when(repository.save(any(ExtractionResult.class))).thenAnswer(inv -> inv.getArgument(0));

        InputStream stream = new ByteArrayInputStream(new byte[0]);
        ExtractionResult result = service.extract(stream, "test.pdf");

        assertNotNull(result.id());
        assertEquals(1, result.berufe().size());
        assertNull(result.verification());

        verify(repository).save(any(ExtractionResult.class));
    }

    @Test
    void verifyReRunsVerificationOnExistingExtraction() {
        ExtractionId id = ExtractionId.generate();
        ExtractionResult existing = new ExtractionResult(id, "test.pdf", Instant.now(), List.of());
        VerificationResult verification = new VerificationResult(true, List.of(), Instant.now());

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(mcpVerifier.verify(existing.berufe())).thenReturn(verification);
        when(repository.save(any(ExtractionResult.class))).thenAnswer(inv -> inv.getArgument(0));

        ExtractionResult result = service.verify(id);

        assertNotNull(result.verification());
        assertTrue(result.verification().valid());
        verify(repository).save(any(ExtractionResult.class));
    }

    @Test
    void verifyThrowsWhenNotFound() {
        ExtractionId id = ExtractionId.generate();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ExtractionNotFoundException.class, () -> service.verify(id));
    }

    @Test
    void getByIdReturnsResult() {
        ExtractionId id = ExtractionId.generate();
        ExtractionResult expected = new ExtractionResult(id, "test.pdf", Instant.now(), List.of());
        when(repository.findById(id)).thenReturn(Optional.of(expected));

        ExtractionResult result = service.getById(id);

        assertEquals(expected, result);
    }

    @Test
    void getByIdThrowsWhenNotFound() {
        ExtractionId id = ExtractionId.generate();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ExtractionNotFoundException.class, () -> service.getById(id));
    }

    @Test
    void deleteRemovesExistingExtraction() {
        ExtractionId id = ExtractionId.generate();
        ExtractionResult existing = new ExtractionResult(id, "test.pdf", Instant.now(), List.of());
        when(repository.findById(id)).thenReturn(Optional.of(existing));

        service.delete(id);

        verify(repository).deleteById(id);
    }

    @Test
    void deleteThrowsWhenNotFound() {
        ExtractionId id = ExtractionId.generate();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ExtractionNotFoundException.class, () -> service.delete(id));
    }

    @Test
    void listAllDelegatesToRepository() {
        List<ExtractionResult> expected = List.of(
                new ExtractionResult(ExtractionId.generate(), "a.pdf", Instant.now(), List.of()),
                new ExtractionResult(ExtractionId.generate(), "b.pdf", Instant.now(), List.of())
        );
        when(repository.findAll()).thenReturn(expected);

        List<ExtractionResult> results = service.listAll();

        assertEquals(2, results.size());
    }
}
