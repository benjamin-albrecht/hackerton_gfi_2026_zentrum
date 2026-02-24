package com.gfi.zentrum.domain.port.in;

import com.gfi.zentrum.domain.model.ExtractionId;
import com.gfi.zentrum.domain.model.ExtractionResult;

public interface VerifyExtractionUseCase {

    ExtractionResult verify(ExtractionId id);
}
