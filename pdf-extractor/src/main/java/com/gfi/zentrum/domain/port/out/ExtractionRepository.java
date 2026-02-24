package com.gfi.zentrum.domain.port.out;

import com.gfi.zentrum.domain.model.ExtractionId;
import com.gfi.zentrum.domain.model.ExtractionResult;

import java.util.List;
import java.util.Optional;

public interface ExtractionRepository {

    ExtractionResult save(ExtractionResult result);

    Optional<ExtractionResult> findById(ExtractionId id);

    List<ExtractionResult> findAll();

    void deleteById(ExtractionId id);
}
