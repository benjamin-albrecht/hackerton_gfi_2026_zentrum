# hackerton_gfi_2026

## PDF Extractor

The `pdf-extractor` module is a Java application designed to parse standard IHK vocation PDF files (e.g. `gesamt-bpue-w25-data.pdf`) and extract the data into a strict JSON schema (`pplanzentral.json`).

### Prerequisites
- Java 17 or higher
- Maven 3.x

### Build & Run

1. Navigate to the `pdf-extractor` directory:
   ```bash
   cd pdf-extractor
   ```

2. Compile the project:
   ```bash
   mvn clean compile
   ```

3. Run the extractor with Maven (pass the input PDF and the desired output JSON file as arguments):
   ```bash
   mvn exec:java -Dexec.mainClass="com.gfi.zentrum.App" -Dexec.args="../doc/berufe/gesamt-bpue-w25-data.pdf extracted_data.json"
   ```

   This will process the PDF and generate `extracted_data.json` inside the `pdf-extractor` directory mapping to the `Root -> Beruf -> PruefungsBereich -> Aufgabe -> Termin` schema structure.