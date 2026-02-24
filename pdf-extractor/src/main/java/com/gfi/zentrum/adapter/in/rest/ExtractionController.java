package com.gfi.zentrum.adapter.in.rest;

import com.gfi.zentrum.adapter.in.rest.dto.*;
import com.gfi.zentrum.adapter.in.rest.mapper.ExtractionDtoMapper;
import com.gfi.zentrum.domain.model.ExtractionId;
import com.gfi.zentrum.domain.model.ExtractionResult;
import com.gfi.zentrum.domain.port.in.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/extractions")
@Tag(name = "Extractions", description = "PDF extraction management")
public class ExtractionController {

    private final ExtractPdfUseCase extractPdfUseCase;
    private final GetExtractionUseCase getExtractionUseCase;
    private final ListExtractionsUseCase listExtractionsUseCase;
    private final DeleteExtractionUseCase deleteExtractionUseCase;
    private final VerifyExtractionUseCase verifyExtractionUseCase;
    private final ExtractionDtoMapper mapper;

    public ExtractionController(ExtractPdfUseCase extractPdfUseCase,
                                GetExtractionUseCase getExtractionUseCase,
                                ListExtractionsUseCase listExtractionsUseCase,
                                DeleteExtractionUseCase deleteExtractionUseCase,
                                VerifyExtractionUseCase verifyExtractionUseCase,
                                ExtractionDtoMapper mapper) {
        this.extractPdfUseCase = extractPdfUseCase;
        this.getExtractionUseCase = getExtractionUseCase;
        this.listExtractionsUseCase = listExtractionsUseCase;
        this.deleteExtractionUseCase = deleteExtractionUseCase;
        this.verifyExtractionUseCase = verifyExtractionUseCase;
        this.mapper = mapper;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Upload and extract a PDF")
    public ExtractionCreatedResponse uploadAndExtract(@RequestParam("file") MultipartFile file)
            throws IOException {
        ExtractionResult result = extractPdfUseCase.extract(
                file.getInputStream(),
                file.getOriginalFilename()
        );
        return mapper.toCreatedResponse(result);
    }

    @GetMapping
    @Operation(summary = "List all extractions")
    public List<ExtractionSummaryResponse> listAll() {
        return listExtractionsUseCase.listAll().stream()
                .map(mapper::toSummaryResponse)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get extraction details")
    public ExtractionDetailResponse getById(@PathVariable String id) {
        ExtractionResult result = getExtractionUseCase.getById(ExtractionId.of(id));
        return mapper.toDetailResponse(result);
    }

    @PostMapping("/{id}/verify")
    @Operation(summary = "Re-verify an extraction against the MCP server")
    public ExtractionDetailResponse verify(@PathVariable String id) {
        ExtractionResult result = verifyExtractionUseCase.verify(ExtractionId.of(id));
        return mapper.toDetailResponse(result);
    }

    @GetMapping("/{id}/berufe")
    @Operation(summary = "List berufe from an extraction")
    public List<BerufResponse> listBerufe(@PathVariable String id) {
        ExtractionResult result = getExtractionUseCase.getById(ExtractionId.of(id));
        return result.berufe().stream()
                .map(mapper::toBerufResponse)
                .toList();
    }

    @GetMapping("/{id}/berufe/{index}")
    @Operation(summary = "Get a specific beruf by index")
    public BerufResponse getBerufByIndex(@PathVariable String id, @PathVariable int index) {
        ExtractionResult result = getExtractionUseCase.getById(ExtractionId.of(id));
        if (index < 0 || index >= result.berufe().size()) {
            throw new IndexOutOfBoundsException(
                    "Beruf index %d out of range [0, %d)".formatted(index, result.berufe().size()));
        }
        return mapper.toBerufResponse(result.berufe().get(index));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete an extraction")
    public void delete(@PathVariable String id) {
        deleteExtractionUseCase.delete(ExtractionId.of(id));
    }
}
