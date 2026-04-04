#!/usr/bin/env python3
"""
Parses docs/methods/*.md, extracts Output data table, and builds OpenAPI response
schemas. Merges them into docs/openapi.json so codegen produces one model per method.
"""
import json
import re
from pathlib import Path
from typing import List, Tuple

DOCS = Path(__file__).resolve().parent.parent / "docs"
METHODS_DIR = DOCS / "methods"
OPENAPI_PATH = DOCS / "openapi.json"

def type_to_jsonschema(typ: str) -> dict:
    typ = (typ or "").strip().lower()
    if "int" in typ or "id" in typ:
        return {"type": "integer"}
    if "bool" in typ:
        return {"type": "boolean"}
    if "float" in typ or "decimal" in typ or "number" in typ:
        return {"type": "number"}
    if "array" in typ:
        return {"type": "array", "items": {"type": "object"}}
    if "object" in typ:
        return {"type": "object", "additionalProperties": True}
    return {"type": "string"}

def parse_output_section(content: str) -> List[Tuple[str, str]]:
    """Returns [(field_name, type_str), ...] from ## Output data table."""
    out = []
    in_output = False
    for line in content.splitlines():
        if line.strip() == "## Output data":
            in_output = True
            continue
        if in_output:
            if line.strip().startswith("## "):
                break
            # Table row: | name | type | desc |
            m = re.match(r"^\|\s*([a-zA-Z_][a-zA-Z0-9_]*)\s*\|\s*([^|]*?)\s*\|", line)
            if m:
                name, typ = m.group(1).strip(), m.group(2).strip()
                if name != "Metoda" and not name.startswith("---"):
                    out.append((name, typ))
    return out

def method_to_schema_name(method: str) -> str:
    return method[0].upper() + method[1:] + "Response"

def main():
    openapi = json.loads(OPENAPI_PATH.read_text(encoding="utf-8"))
    schemas = openapi.setdefault("components", {}).setdefault("schemas", {})

    # Base response (status + error fields)
    schemas["ApiResponseBase"] = {
        "type": "object",
        "required": ["status"],
        "properties": {
            "status": {"type": "string", "enum": ["SUCCESS", "ERROR"], "description": "Call result"},
            "error_message": {"type": "string", "description": "Error description (when status=ERROR)"},
            "error_code": {"type": "string", "description": "Error code (when status=ERROR)"},
        },
        "description": "Base fields of every API response",
    }

    for md_path in sorted(METHODS_DIR.glob("*.md")):
        method = md_path.stem
        content = md_path.read_text(encoding="utf-8")
        rows = parse_output_section(content)
        # Skip status, error_message, error_code (already in base)
        extra = {}
        for name, typ in rows:
            if name in ("status", "error_message", "error_code"):
                continue
            extra[name] = type_to_jsonschema(typ)
        schema_name = method_to_schema_name(method)
        allof_parts = [{"$ref": "#/components/schemas/ApiResponseBase"}]
        if extra:
            allof_parts.append({"type": "object", "properties": extra})
        schemas[schema_name] = {
            "allOf": allof_parts,
            "description": f"Response of method {method}",
        }

    OPENAPI_PATH.write_text(json.dumps(openapi, indent=2, ensure_ascii=False), encoding="utf-8")
    print("Updated", OPENAPI_PATH, "with", len(list(METHODS_DIR.glob("*.md"))), "response schemas.")

if __name__ == "__main__":
    main()
