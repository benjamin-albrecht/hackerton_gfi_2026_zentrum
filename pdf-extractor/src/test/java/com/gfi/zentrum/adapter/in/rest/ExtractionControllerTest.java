package com.gfi.zentrum.adapter.in.rest;

import com.gfi.zentrum.adapter.in.rest.mapper.ExtractionDtoMapper;
import com.gfi.zentrum.domain.model.*;
import com.gfi.zentrum.domain.port.in.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
}
