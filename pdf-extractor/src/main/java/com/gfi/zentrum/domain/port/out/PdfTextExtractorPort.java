package com.gfi.zentrum.domain.port.out;

import java.io.InputStream;

public interface PdfTextExtractorPort {

    String extractText(InputStream pdfStream);
}
