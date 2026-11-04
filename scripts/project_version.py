#!/usr/bin/env python3
"""Read the single checked-in WorldSafe version without starting a build tool."""

import argparse
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parent.parent
VERSION = re.compile(r"[0-9A-Za-z][0-9A-Za-z._-]*")


def validate(value):
    if not isinstance(value, str) or not VERSION.fullmatch(value):
        raise ValueError("Invalid WorldSafe version: " + repr(value))
    return value


def parse_properties(text):
    values = re.findall(r"^[ \t]*version[ \t]*=([^\r\n]*)", text, re.MULTILINE)
    if len(values) != 1:
        raise ValueError("Expected exactly one version= entry in root gradle.properties")
    return validate(values[0].strip())


def read_version(root=ROOT):
    return parse_properties((Path(root) / "gradle.properties").read_text(encoding="utf-8"))


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", type=Path, default=ROOT)
    args = parser.parse_args()
    try:
        print(read_version(args.root))
    except (OSError, ValueError) as error:
        parser.exit(1, str(error) + "\n")


if __name__ == "__main__":
    main()
