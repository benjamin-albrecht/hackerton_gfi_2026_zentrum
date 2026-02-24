package com.gfi.zentrum.domain.port.in;

import com.gfi.zentrum.domain.model.ExtractionResult;

import java.io.InputStream;

public interface ExtractPdfUseCase {

    ExtractionResult extract(InputStream pdfStream, String fileName);
}
