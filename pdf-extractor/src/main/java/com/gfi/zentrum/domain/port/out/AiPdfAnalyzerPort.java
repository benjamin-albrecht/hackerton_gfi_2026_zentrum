package com.gfi.zentrum.domain.port.out;

import com.gfi.zentrum.domain.model.Beruf;

import java.util.List;

public interface AiPdfAnalyzerPort {

    List<Beruf> analyze(String pdfText);
}
