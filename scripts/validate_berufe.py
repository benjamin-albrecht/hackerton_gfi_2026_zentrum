#!/usr/bin/env python3
import argparse
import json
import sys
from pathlib import Path

try:
    import jsonschema
    from jsonschema import Draft7Validator
except Exception:
    jsonschema = None


def validate(schema_path: Path, input_path: Path, max_errors: int = 10):
    if jsonschema is None:
        print("Missing dependency: jsonschema. Install with: python -m pip install jsonschema")
        return 2

    with schema_path.open("r", encoding="utf-8") as fh:
        schema = json.load(fh)

    with input_path.open("r", encoding="utf-8") as fh:
        data = json.load(fh)

    if not isinstance(data, list):
        print(f"Expected an array of objects in {input_path}, got {type(data).__name__}")
        return 2

    validator = Draft7Validator(schema)
    total = len(data)
    valid_count = 0
    invalid_details = []

    for idx, item in enumerate(data, start=1):
        errors = list(validator.iter_errors(item))
        if not errors:
            valid_count += 1
            continue
        # collect concise errors
        err_list = []
        for e in errors[:max_errors]:
            path = ".".join([str(p) for p in e.absolute_path]) or "<root>"
            err_list.append({"path": path, "message": e.message})
        invalid_details.append({"index": idx, "errors": err_list, "error_count": len(errors)})

    # print summary
    print(f"Validated {total} objects: {valid_count} valid, {len(invalid_details)} invalid")
    if invalid_details:
        print("\nErrors (first items):")
        for d in invalid_details[:20]:
            print(f"- Item #{d['index']}: {d['error_count']} error(s)")
            for e in d['errors']:
                print(f"    {e['path']}: {e['message']}")
    return 0 if valid_count == total else 1


if __name__ == '__main__':
    p = argparse.ArgumentParser(description='Validate berufe.json against schema.json')
    p.add_argument('--schema', '-s', default='schema.json', help='Schema JSON file')
    p.add_argument('--input', '-i', default='doc/berufe/berufe.json', help='Input JSON file (array)')
    args = p.parse_args()

    rc = validate(Path(args.schema), Path(args.input))
    sys.exit(rc)
