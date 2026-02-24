package com.gfi.zentrum.domain.port.out;

import com.gfi.zentrum.domain.model.Beruf;

import java.io.InputStream;
import java.util.List;

public interface PdfParserPort {

    List<Beruf> parse(InputStream pdfStream);
}
