"""Shared-core packaging contract used by both platform verifiers."""

import argparse
from functools import lru_cache
from pathlib import Path
import zipfile

ROOT = Path(__file__).resolve().parent.parent
SOURCE = ROOT / "shared/src/main/java"


@lru_cache(maxsize=1)
def shared_entries():
    return {source.relative_to(SOURCE).with_suffix(".class").as_posix()
            for source in SOURCE.rglob("*.java")}


def is_shared_class(name):
    outer = name.split("$", 1)[0].removesuffix(".class") + ".class"
    return name.endswith(".class") and outer in shared_entries()


def verify_shared_core(jar):
    entries = jar.namelist()
    if len(entries) != len(set(entries)):
        raise ValueError("Duplicate JAR entries")
    missing = shared_entries() - set(entries)
    if missing:
        raise ValueError("Missing shared core classes: " + ", ".join(sorted(missing)))
    for name in entries:
        if "/fabric/driver/" in name:
            raise ValueError("Bundled test driver: " + name)
        if is_shared_class(name):
            data = jar.read(name)
            if any(symbol in data for symbol in (b"org/bukkit/", b"io/papermc/", b"net/minecraft/",
                                                  b"net/fabricmc/", b"net/minecraftforge/", b"net/neoforged/")):
                raise ValueError("Platform reference in shared core: " + name)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("jar", type=Path)
    args = parser.parse_args()
    with zipfile.ZipFile(args.jar) as jar:
        verify_shared_core(jar)
