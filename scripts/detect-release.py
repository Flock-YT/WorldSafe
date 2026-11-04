#!/usr/bin/env python3
"""Compare checked-in versions, including the first migration from Maven."""

import argparse
import os
from pathlib import Path
import re
import subprocess
import xml.etree.ElementTree as ET

from project_version import ROOT, parse_properties, read_version, validate


def git(root, *arguments):
    return subprocess.run(["git", "-C", str(root), *arguments], check=True,
                          stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True).stdout


def previous_version(root, commit):
    if not re.fullmatch(r"[0-9a-fA-F]{40}|[0-9a-fA-F]{64}", commit):
        raise ValueError("Invalid previous commit")
    # Only fall back when the old properties file is absent, never when it is invalid.
    entries = git(root, "ls-tree", "--name-only", commit, "--", "gradle.properties").splitlines()
    if "gradle.properties" in entries:
        return parse_properties(git(root, "show", commit + ":gradle.properties"))
    pom = ET.fromstring(git(root, "show", commit + ":pom.xml"))
    return validate(pom.findtext("{http://maven.apache.org/POM/4.0.0}version"))


def detect(root, event, before, ref):
    current = read_version(root)
    if event == "workflow_dispatch":
        if ref != "refs/heads/master":
            raise ValueError("Manual releases must run from the master branch")
        previous, release = "<manual>", True
    elif event == "push":
        if ref != "refs/heads/master":
            raise ValueError("Automatic releases must run from the master branch")
        if re.fullmatch(r"0{40}|0{64}", before):
            previous, release = "<none>", True
        else:
            previous = previous_version(root, before)
            release = current != previous
    else:
        raise ValueError("Unsupported release event: " + event)
    return {"previous_version": previous, "should_release": str(release).lower(),
            "version": current, "tag": "v" + current, "artifact": "WorldSafe-" + current + ".jar"}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", type=Path, default=ROOT)
    args = parser.parse_args()
    try:
        result = detect(args.root, os.environ.get("EVENT_NAME", ""),
                        os.environ.get("BEFORE_SHA", ""), os.environ.get("GITHUB_REF", ""))
        for key, value in result.items():
            print(key + "=" + value)
        if os.environ.get("GITHUB_OUTPUT"):
            with open(os.environ["GITHUB_OUTPUT"], "a", encoding="utf-8") as output:
                for key, value in result.items():
                    if key != "previous_version":
                        output.write(key + "=" + value + "\n")
    except (OSError, ValueError, ET.ParseError, subprocess.CalledProcessError) as error:
        parser.exit(1, str(error) + "\n")


if __name__ == "__main__":
    main()
