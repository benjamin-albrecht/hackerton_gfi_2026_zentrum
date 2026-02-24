package com.gfi.zentrum.adapter.in.rest;

import com.gfi.zentrum.adapter.in.rest.dto.ErrorResponse;
import com.gfi.zentrum.domain.model.ExtractionNotFoundException;
import com.gfi.zentrum.domain.model.PdfParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ExtractionNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(ExtractionNotFoundException e) {
        return new ErrorResponse(404, "Not Found", e.getMessage(), Instant.now());
    }

    @ExceptionHandler(PdfParsingException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErrorResponse handleParsingError(PdfParsingException e) {
        log.error("PDF parsing failed", e);
        return new ErrorResponse(422, "Unprocessable Entity", e.getMessage(), Instant.now());
    }

    @ExceptionHandler(IndexOutOfBoundsException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleIndexOutOfBounds(IndexOutOfBoundsException e) {
        return new ErrorResponse(404, "Not Found", e.getMessage(), Instant.now());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequest(IllegalArgumentException e) {
        return new ErrorResponse(400, "Bad Request", e.getMessage(), Instant.now());
    }
}
