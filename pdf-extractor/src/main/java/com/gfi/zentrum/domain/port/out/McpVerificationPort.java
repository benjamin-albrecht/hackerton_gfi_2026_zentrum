package com.gfi.zentrum.domain.port.out;

import com.gfi.zentrum.domain.model.Beruf;
import com.gfi.zentrum.domain.model.VerificationResult;

import java.util.List;

public interface McpVerificationPort {

    VerificationResult verify(List<Beruf> berufe);
}
