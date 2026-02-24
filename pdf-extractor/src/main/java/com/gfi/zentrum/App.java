package com.gfi.zentrum;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.gfi.zentrum.model.Root;
import com.gfi.zentrum.parser.PdfParser;

import java.io.File;

public class App {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java -jar pdf-extractor.jar <path-to-pdf> [output-path]");
            System.exit(1);
        }

        String inputFilePath = args[0];
        String outputFilePath = args.length > 1 ? args[1] : null;

        System.out.println("Processing PDF: " + inputFilePath);

        try {
            PdfParser parser = new PdfParser();

            try (org.apache.pdfbox.pdmodel.PDDocument document = org.apache.pdfbox.Loader
                    .loadPDF(new File(inputFilePath))) {
                org.apache.pdfbox.text.PDFTextStripper stripper = new org.apache.pdfbox.text.PDFTextStripper();
                stripper.setSortByPosition(true);
                String text = stripper.getText(document);
                java.nio.file.Files.writeString(java.nio.file.Path.of("raw_text.txt"), text);
                System.out.println("Wrote raw text to raw_text.txt");
            }

            java.util.List<com.gfi.zentrum.model.Root> examData = parser.parsePdf(inputFilePath);

            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);

            if (outputFilePath != null) {
                mapper.writeValue(new File(outputFilePath), examData);
                System.out.println("Successfully wrote JSON to " + outputFilePath);
            } else {
                String json = mapper.writeValueAsString(examData);
                System.out.println("--- Extracted JSON ---");
                System.out.println(json);
            }

        } catch (Exception e) {
            System.err.println("Error processing PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
