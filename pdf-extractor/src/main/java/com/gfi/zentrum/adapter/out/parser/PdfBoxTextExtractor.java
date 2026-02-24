package com.gfi.zentrum.adapter.out.parser;

import com.gfi.zentrum.domain.model.PdfParsingException;
import com.gfi.zentrum.domain.port.out.PdfTextExtractorPort;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class PdfBoxTextExtractor implements PdfTextExtractorPort {

    @Override
    public String extractText(InputStream pdfStream) {
        try (PDDocument document = Loader.loadPDF(pdfStream.readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(document);
        } catch (IOException e) {
            throw new PdfParsingException("Failed to extract text from PDF", e);
        }
    }
}
