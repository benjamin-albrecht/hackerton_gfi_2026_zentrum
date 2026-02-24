package com.gfi.zentrum.adapter.in.cli;

import com.gfi.zentrum.domain.model.Beruf;
import com.gfi.zentrum.domain.model.ExtractionResult;
import com.gfi.zentrum.domain.port.in.ExtractPdfUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

@Component
@Profile("cli")
public class CliRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CliRunner.class);

    private final ExtractPdfUseCase extractPdfUseCase;

    public CliRunner(ExtractPdfUseCase extractPdfUseCase) {
        this.extractPdfUseCase = extractPdfUseCase;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<String> nonOptionArgs = args.getNonOptionArgs();
        if (nonOptionArgs.isEmpty()) {
            System.err.println("Usage: java -jar pdf-extractor.jar --spring.profiles.active=cli <path-to-pdf> [output-path]");
            return;
        }

        String inputPath = nonOptionArgs.get(0);
        String outputPath = nonOptionArgs.size() > 1 ? nonOptionArgs.get(1) : null;

        log.info("Processing PDF: {}", inputPath);

        try (InputStream is = new FileInputStream(inputPath)) {
            ExtractionResult result = extractPdfUseCase.extract(is, Path.of(inputPath).getFileName().toString());
            List<Beruf> berufe = result.berufe();

            var mapper = JsonMapper.builder()
                    .enable(SerializationFeature.INDENT_OUTPUT)
                    .build();

            if (outputPath != null) {
                mapper.writeValue(Path.of(outputPath).toFile(), berufe);
                log.info("Wrote JSON to {}", outputPath);
            } else {
                String json = mapper.writeValueAsString(berufe);
                System.out.println(json);
            }
        }
    }
}
