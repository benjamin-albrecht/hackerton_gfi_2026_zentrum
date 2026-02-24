# hackerton_gfi_2026

## PDF Extractor

The `pdf-extractor` module is a Spring Boot REST API that extracts structured vocational training exam data from IHK (Industrie- und Handelskammer) PDF documents. It uses AI-powered analysis (Anthropic Claude) with a regex-based fallback parser, and optionally verifies extracted data against an external MCP server.

### Prerequisites

- Java 25+
- Maven 3.x
- Anthropic API key (for AI-powered extraction)
- MCP verification server (optional, for data validation)

### Build & Run

```bash
cd pdf-extractor

# Compile
mvn clean compile

# Run tests
mvn test

# Start the REST API server
ANTHROPIC_API_KEY=your-key-here mvn spring-boot:run
```

The server starts on `http://localhost:8080`. Swagger UI is available at `http://localhost:8080/swagger-ui.html`.

### Configuration

Set via environment variables:

| Variable | Default | Description |
|---|---|---|
| `ANTHROPIC_API_KEY` | (required) | Anthropic API key for AI extraction |
| `ANTHROPIC_BASE_URL` | `https://api.anthropic.com/` | Anthropic API base URL |
| `MCP_VERIFICATION_URL` | `http://localhost:3000` | MCP verification server URL |

### REST API

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/extractions` | Upload a PDF (multipart/form-data) and extract data |
| `GET` | `/api/v1/extractions` | List all extractions |
| `GET` | `/api/v1/extractions/{id}` | Get extraction details (includes verification result) |
| `POST` | `/api/v1/extractions/{id}/verify` | Re-verify extraction against MCP server |
| `GET` | `/api/v1/extractions/{id}/berufe` | List all Berufe from an extraction |
| `GET` | `/api/v1/extractions/{id}/berufe/{index}` | Get a specific Beruf by index |
| `DELETE` | `/api/v1/extractions/{id}` | Delete an extraction |

#### Example: Upload a PDF

```bash
curl -X POST http://localhost:8080/api/v1/extractions \
  -F "file=@gesamt-bpue-w25-data.pdf"
```

#### Example: Re-verify an extraction

```bash
curl -X POST http://localhost:8080/api/v1/extractions/{id}/verify
```

### CLI Mode

Run as a command-line tool without starting the web server:

```bash
java -jar target/pdf-extractor-1.0-SNAPSHOT.jar \
  --spring.profiles.active=cli \
  path/to/input.pdf [output.json]
```

### Architecture

The project follows hexagonal (ports & adapters) architecture with DDD:

```
domain/
  model/         Beruf, ExtractionResult, VerificationResult, etc.
  port/in/       Use case interfaces (Extract, Verify, Get, List, Delete)
  port/out/      Port interfaces (AI analyzer, parser, repository, MCP verifier)

application/
  service/       ExtractionService orchestrates the workflow

adapter/
  in/rest/       REST controller, DTOs, mapper
  in/cli/        CLI runner
  out/ai/        Anthropic Claude integration
  out/parser/    PDFBox text extraction + regex fallback parser
  out/persistence/  File-system JSON storage
  out/verification/ MCP client verification adapter
```

### Data Model

```
ExtractionResult
  ├── ExtractionId
  ├── sourceFileName
  ├── extractedAt
  ├── List<Beruf>
  │     ├── beschreibung (profession name)
  │     ├── berufNr (profession codes)
  │     └── List<PruefungsBereich>
  │           ├── name (exam area)
  │           └── List<Aufgabe>
  │                 ├── name, struktur, hilfmittel
  │                 └── Termin (datum, uhrzeitVon, uhrzeitBis, dauer)
  └── VerificationResult (nullable)
        ├── valid
        ├── List<VerificationIssue> (severity, field, message)
        └── verifiedAt
```

### MCP Verification

When an MCP verification server is available, extracted Beruf data is automatically validated after extraction. The server should expose a `validate_berufe` tool via SSE transport. If the server is unavailable, extraction succeeds without verification (the `verification` field is `null`).

You can re-trigger verification at any time via `POST /api/v1/extractions/{id}/verify`.
