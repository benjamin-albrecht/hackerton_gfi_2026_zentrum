package com.gfi.zentrum.application.service;

import com.gfi.zentrum.domain.model.*;
import com.gfi.zentrum.domain.port.in.*;
import com.gfi.zentrum.domain.port.out.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;

@Service
public class ExtractionService implements ExtractPdfUseCase, GetExtractionUseCase,
        ListExtractionsUseCase, DeleteExtractionUseCase, VerifyExtractionUseCase {

    private static final Logger log = LoggerFactory.getLogger(ExtractionService.class);

    private final PdfTextExtractorPort textExtractor;
    private final AiPdfAnalyzerPort aiAnalyzer;
    private final PdfParserPort fallbackParser;
    private final ExtractionRepository repository;
    private final McpVerificationPort mcpVerifier;

    public ExtractionService(PdfTextExtractorPort textExtractor,
                             AiPdfAnalyzerPort aiAnalyzer,
                             PdfParserPort fallbackParser,
                             ExtractionRepository repository,
                             McpVerificationPort mcpVerifier) {
        this.textExtractor = textExtractor;
        this.aiAnalyzer = aiAnalyzer;
        this.fallbackParser = fallbackParser;
        this.repository = repository;
        this.mcpVerifier = mcpVerifier;
    }

    @Override
    public ExtractionResult extract(InputStream pdfStream, String fileName) {
        String text = textExtractor.extractText(pdfStream);
        List<Beruf> berufe;
        try {
            berufe = aiAnalyzer.analyze(text);
        } catch (Exception e) {
            log.warn("AI analysis failed, falling back to regex parser: {}", e.getMessage());
            berufe = fallbackParser.parse(
                    new java.io.ByteArrayInputStream(text.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        }

        VerificationResult verification = runVerification(berufe);

        ExtractionResult result = new ExtractionResult(
                ExtractionId.generate(),
                fileName,
                Instant.now(),
                berufe,
                verification
        );
        return repository.save(result);
    }

    @Override
    public ExtractionResult verify(ExtractionId id) {
        ExtractionResult existing = repository.findById(id)
                .orElseThrow(() -> new ExtractionNotFoundException(id));

        VerificationResult verification = runVerification(existing.berufe());
        ExtractionResult updated = existing.withVerification(verification);
        return repository.save(updated);
    }

    @Override
    public ExtractionResult getById(ExtractionId id) {
        return repository.findById(id)
                .orElseThrow(() -> new ExtractionNotFoundException(id));
    }

    @Override
    public List<ExtractionResult> listAll() {
        return repository.findAll();
    }

    @Override
    public void delete(ExtractionId id) {
        repository.findById(id)
                .orElseThrow(() -> new ExtractionNotFoundException(id));
        repository.deleteById(id);
    }

    private VerificationResult runVerification(List<Beruf> berufe) {
        try {
            return mcpVerifier.verify(berufe);
        } catch (Exception e) {
            log.warn("MCP verification unavailable: {}", e.getMessage());
            return null;
        }
    }
}
