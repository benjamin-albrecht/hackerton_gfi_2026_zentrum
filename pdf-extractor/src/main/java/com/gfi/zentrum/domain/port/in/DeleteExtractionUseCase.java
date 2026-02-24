package com.gfi.zentrum.domain.port.in;

import com.gfi.zentrum.domain.model.ExtractionId;

public interface DeleteExtractionUseCase {

    void delete(ExtractionId id);
}
