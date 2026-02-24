package com.gfi.zentrum.application.service;

import com.gfi.zentrum.domain.model.*;
import com.gfi.zentrum.domain.port.in.*;
import com.gfi.zentrum.domain.port.out.ExtractionRepository;
import com.gfi.zentrum.domain.port.out.PdfParserPort;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;

@Service
public class ExtractionService implements ExtractPdfUseCase, GetExtractionUseCase,
        ListExtractionsUseCase, DeleteExtractionUseCase {

    private final PdfParserPort parser;
    private final ExtractionRepository repository;

    public ExtractionService(PdfParserPort parser, ExtractionRepository repository) {
        this.parser = parser;
        this.repository = repository;
    }

    @Override
    public ExtractionResult extract(InputStream pdfStream, String fileName) {
        List<Beruf> berufe = parser.parse(pdfStream);
        ExtractionResult result = new ExtractionResult(
                ExtractionId.generate(),
                fileName,
                Instant.now(),
                berufe
        );
        return repository.save(result);
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
}
