CI integration for `validate_berufe.py`

Purpose
- Show how to run the extractor and validator in CI to ensure `doc/berufe/berufe.json` matches `schema.json`.

Local commands
```bash
python -m pip install -r requirements.txt
python scripts/pdf_to_json.py --input doc/berufe --output doc/berufe/berufe.json
python scripts/validate_berufe.py --schema schema.json --input doc/berufe/berufe.json
```

GitHub Actions (example)
Create `.github/workflows/validate-berufe.yml` with the following job:
```yaml
name: Validate berufe.json
on: [push, pull_request]
jobs:
  validate:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-python@v4
        with:
          python-version: '3.x'
      - name: Install deps
        run: python -m pip install -r requirements.txt
      - name: Generate berufe.json
        run: python scripts/pdf_to_json.py --input doc/berufe --output doc/berufe/berufe.json
      - name: Validate JSON
        run: python scripts/validate_berufe.py --schema schema.json --input doc/berufe/berufe.json
```

GitLab CI (example)
```yaml
stages:
  - validate

validate_berufe:
  image: python:3.11-slim
  stage: validate
  script:
    - pip install -r requirements.txt
    - python scripts/pdf_to_json.py --input doc/berufe --output doc/berufe/berufe.json
    - python scripts/validate_berufe.py --schema schema.json --input doc/berufe/berufe.json
```

Notes
- The validator exits non-zero if any item fails; CI will fail accordingly.
- If PDFs are large or CI runners are slow, consider caching the generated JSON or running validation only on changed pages.
