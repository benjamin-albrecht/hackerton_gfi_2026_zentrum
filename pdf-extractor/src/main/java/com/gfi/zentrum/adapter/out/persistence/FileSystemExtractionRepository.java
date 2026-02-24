package com.gfi.zentrum.adapter.out.persistence;

import com.gfi.zentrum.adapter.out.persistence.entity.ExtractionEntity;
import com.gfi.zentrum.adapter.out.persistence.mapper.ExtractionEntityMapper;
import com.gfi.zentrum.config.StorageProperties;
import com.gfi.zentrum.domain.model.ExtractionId;
import com.gfi.zentrum.domain.model.ExtractionResult;
import com.gfi.zentrum.domain.port.out.ExtractionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class FileSystemExtractionRepository implements ExtractionRepository {

    private static final Logger log = LoggerFactory.getLogger(FileSystemExtractionRepository.class);

    private final Path storageDir;
    private final ObjectMapper objectMapper;
    private final ExtractionEntityMapper mapper;

    public FileSystemExtractionRepository(StorageProperties properties,
                                          ObjectMapper objectMapper,
                                          ExtractionEntityMapper mapper) {
        this.storageDir = Path.of(properties.getPath());
        this.objectMapper = objectMapper;
        this.mapper = mapper;
        ensureDirectoryExists();
    }

    @Override
    public ExtractionResult save(ExtractionResult result) {
        ExtractionEntity entity = mapper.toEntity(result);
        Path file = resolveFile(result.id());
        objectMapper.writeValue(file.toFile(), entity);
        log.info("Saved extraction {} to {}", result.id(), file);
        return result;
    }

    @Override
    public Optional<ExtractionResult> findById(ExtractionId id) {
        Path file = resolveFile(id);
        if (!Files.exists(file)) {
            return Optional.empty();
        }
        ExtractionEntity entity = objectMapper.readValue(file.toFile(), ExtractionEntity.class);
        return Optional.of(mapper.toDomain(entity));
    }

    @Override
    public List<ExtractionResult> findAll() {
        List<ExtractionResult> results = new ArrayList<>();
        if (!Files.exists(storageDir)) {
            return results;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(storageDir, "*.json")) {
            for (Path file : stream) {
                try {
                    ExtractionEntity entity = objectMapper.readValue(file.toFile(), ExtractionEntity.class);
                    results.add(mapper.toDomain(entity));
                } catch (Exception e) {
                    log.warn("Skipping unreadable file: {}", file, e);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to list extractions", e);
        }
        return results;
    }

    @Override
    public void deleteById(ExtractionId id) {
        Path file = resolveFile(id);
        try {
            Files.deleteIfExists(file);
            log.info("Deleted extraction {}", id);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to delete extraction " + id, e);
        }
    }

    private Path resolveFile(ExtractionId id) {
        return storageDir.resolve(id.toString() + ".json");
    }

    private void ensureDirectoryExists() {
        try {
            Files.createDirectories(storageDir);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to create storage directory: " + storageDir, e);
        }
    }
}
