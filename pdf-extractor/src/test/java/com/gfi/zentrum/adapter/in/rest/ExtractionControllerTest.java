package com.gfi.zentrum.adapter.in.rest;

import com.gfi.zentrum.adapter.in.rest.dto.*;
import com.gfi.zentrum.adapter.in.rest.mapper.ExtractionDtoMapper;
import com.gfi.zentrum.domain.model.*;
import com.gfi.zentrum.domain.port.in.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExtractionController.class)
class ExtractionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExtractPdfUseCase extractPdfUseCase;

    @MockitoBean
    private GetExtractionUseCase getExtractionUseCase;

    @MockitoBean
    private ListExtractionsUseCase listExtractionsUseCase;

    @MockitoBean
    private DeleteExtractionUseCase deleteExtractionUseCase;

    @MockitoBean
    private VerifyExtractionUseCase verifyExtractionUseCase;

    @MockitoBean
    private ExtractionDtoMapper dtoMapper;

    @Test
    void listAllReturns200() throws Exception {
        when(listExtractionsUseCase.listAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/extractions"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void getByIdReturnsExtraction() throws Exception {
        ExtractionId id = ExtractionId.of("550e8400-e29b-41d4-a716-446655440000");
        ExtractionResult result = new ExtractionResult(id, "test.pdf", Instant.parse("2026-01-15T10:00:00Z"), List.of());
        when(getExtractionUseCase.getById(id)).thenReturn(result);
        when(dtoMapper.toDetailResponse(result)).thenCallRealMethod();
        when(dtoMapper.toVerificationResponse(null)).thenCallRealMethod();

        mockMvc.perform(get("/api/v1/extractions/550e8400-e29b-41d4-a716-446655440000"))
                .andExpect(status().isOk());
    }

    @Test
    void getByIdReturns404WhenNotFound() throws Exception {
        ExtractionId id = ExtractionId.of("550e8400-e29b-41d4-a716-446655440000");
        when(getExtractionUseCase.getById(id))
                .thenThrow(new ExtractionNotFoundException(id));

        mockMvc.perform(get("/api/v1/extractions/550e8400-e29b-41d4-a716-446655440000"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteReturns204() throws Exception {
        mockMvc.perform(delete("/api/v1/extractions/550e8400-e29b-41d4-a716-446655440000"))
                .andExpect(status().isNoContent());

        verify(deleteExtractionUseCase).delete(ExtractionId.of("550e8400-e29b-41d4-a716-446655440000"));
    }

    @Test
    void verifyReturnsUpdatedExtraction() throws Exception {
        ExtractionId id = ExtractionId.of("550e8400-e29b-41d4-a716-446655440000");
        VerificationResult verification = new VerificationResult(true, List.of(), Instant.parse("2026-01-15T10:00:00Z"));
        ExtractionResult result = new ExtractionResult(id, "test.pdf", Instant.parse("2026-01-15T10:00:00Z"), List.of(), verification);
        ExtractionDetailResponse response = new ExtractionDetailResponse(
                id.toString(), "test.pdf", Instant.parse("2026-01-15T10:00:00Z"), List.of(),
                new VerificationResultResponse(true, List.of(), Instant.parse("2026-01-15T10:00:00Z"))
        );

        when(verifyExtractionUseCase.verify(id)).thenReturn(result);
        when(dtoMapper.toDetailResponse(result)).thenReturn(response);

        mockMvc.perform(post("/api/v1/extractions/550e8400-e29b-41d4-a716-446655440000/verify"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verification.valid").value(true));
    }

    @Test
    void verifyReturns404WhenNotFound() throws Exception {
        ExtractionId id = ExtractionId.of("550e8400-e29b-41d4-a716-446655440000");
        when(verifyExtractionUseCase.verify(id))
                .thenThrow(new ExtractionNotFoundException(id));

        mockMvc.perform(post("/api/v1/extractions/550e8400-e29b-41d4-a716-446655440000/verify"))
                .andExpect(status().isNotFound());
    }
}
