package com.gfi.zentrum.domain.port.in;

import com.gfi.zentrum.domain.model.ExtractionResult;

import java.util.List;

public interface ListExtractionsUseCase {

    List<ExtractionResult> listAll();
}
