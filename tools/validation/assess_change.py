#!/usr/bin/env python3
"""Report advisory impact for the commits a change adds to its merge base."""
import argparse
from fnmatch import fnmatchcase
import hashlib
import json
from pathlib import Path
import subprocess
import sys

MAP = Path(__file__).with_name("capabilities.json")


def git(*args):
    return subprocess.check_output(["git", *args], text=True, encoding="utf-8", errors="surrogateescape")


def matches(path, patterns):
    return any(fnmatchcase(path, pattern) for pattern in patterns)


def changed_files(base, head):
    fields = iter(git("diff", "--no-ext-diff", "--name-status", "-z", "-M", base, head, "--").split("\0"))
    for status in fields:
        if not status:
            break
        paths = [next(fields)]
        if status.startswith(("R", "C")):
            paths.append(next(fields))
        yield {"status": status, "paths": paths}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("base", help="Target branch or base commit")
    parser.add_argument("head", help="Change branch or head commit")
    args = parser.parse_args()
    mapping_bytes = MAP.read_bytes()
    mapping = json.loads(mapping_bytes)
    base = git("rev-parse", "--verify", "--end-of-options", f"{args.base}^{{commit}}").strip()
    head = git("rev-parse", "--verify", "--end-of-options", f"{args.head}^{{commit}}").strip()
    merge_base = git("merge-base", base, head).strip()
    changes = list(changed_files(merge_base, head))
    paths = sorted({path for change in changes for path in change["paths"]})
    impacted = []
    covered = set()
    for capability in mapping["capabilities"]:
        evidence = [path for path in paths if matches(path, capability["paths"])]
        if evidence:
            covered.update(evidence)
            impacted.append({key: value for key, value in capability.items() if key != "paths"} | {"matched_paths": evidence})
    shared = [path for path in paths if matches(path, mapping["shared_paths"])]
    unmapped = sorted(set(paths) - covered)
    needs_broad = bool(shared or unmapped)
    selection = mapping["baseline"] + [
        test for capability in impacted for test in capability["tests"]
    ] + (mapping["broad"] if needs_broad else [])
    report = {
        "advisory": True,
        "base": base,
        "merge_base": merge_base,
        "head": head,
        "map_sha256": hashlib.sha256(mapping_bytes).hexdigest(),
        "diff_summary": git("diff", "--no-ext-diff", "--shortstat", merge_base, head, "--").strip(),
        "changed_files": changes,
        "potential_impact": impacted,
        "shared_paths": shared,
        "unmapped_paths": unmapped,
        "validation_scope": "broad" if needs_broad else "baseline plus matched capabilities",
        "suggested_validation": list(dict.fromkeys(selection)),
        "executed_validation": [],
        "remaining_uncertainty": [
            "Test execution evidence is absent for this analyzed head.",
            "Capability matches are hypotheses from path rules and historical examples. Review dependencies and behavior changes in the diff.",
        ],
    }
    if unmapped:
        report["remaining_uncertainty"].append("Unmapped paths may affect additional capabilities.")
    json.dump(report, sys.stdout, indent=2)
    print()


if __name__ == "__main__":
    main()
