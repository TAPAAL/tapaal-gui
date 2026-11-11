#!/usr/bin/env bash
# Download the latest successful main-branch builds of verifypn, verifydtapn and
# verifytapn for one platform and store them under the names the GUI expects
# (verifypn64, verifypn64.exe, ...).
#
# Usage: fetch-engines.sh <linux64|win64|osx64|arm64> <output-dir>
# Requires: gh (authenticated via GH_TOKEN), jq
set -euo pipefail

platform="$1"
out="$2"
owner="${ENGINE_OWNER:-TAPAAL}"

case "$platform" in
  linux64) wf=build-linux.yml ;;
  win64)   wf=build-win.yml ;;
  osx64)   wf=build-macos.yml ;;
  arm64)   wf=build-macos-arm.yml ;;
  *) echo "Unknown platform: $platform" >&2; exit 1 ;;
esac

mkdir -p "$out"
tmp=$(mktemp -d)
summary="${GITHUB_STEP_SUMMARY:-/dev/null}"
{
  echo "### Engine builds used ($platform)"
  echo "| Engine | Run | Commit | Date |"
  echo "|---|---|---|---|"
} >> "$summary"

for engine in verifypn verifydtapn verifytapn; do
  repo="$owner/$engine"
  run_json=$(gh run list -R "$repo" --workflow "$wf" --branch main --status success --limit 1 \
             --json databaseId,headSha,createdAt,url)
  run_id=$(jq -r '.[0].databaseId // empty' <<< "$run_json")
  if [ -z "$run_id" ]; then
    echo "::error::No successful run of $wf on main in $repo"
    exit 1
  fi
  sha=$(jq -r '.[0].headSha' <<< "$run_json")
  date=$(jq -r '.[0].createdAt' <<< "$run_json")
  url=$(jq -r '.[0].url' <<< "$run_json")

  dl="$tmp/$engine"
  if ! gh run download "$run_id" -R "$repo" -n "$engine-$platform" -D "$dl"; then
    echo "::error::Could not download artifact $engine-$platform from $url (expired?). Re-run $wf in $repo."
    exit 1
  fi

  src=$(find "$dl" -type f | head -n 1)
  if [ "$platform" = win64 ]; then
    dst="$out/${engine}64.exe"
  else
    dst="$out/${engine}64"
  fi
  mv "$src" "$dst"
  chmod +x "$dst"
  echo "$engine ($platform) <- run $run_id ($sha)"
  echo "| $engine | [$run_id]($url) | \`${sha:0:10}\` | $date |" >> "$summary"
done

rm -rf "$tmp"
ls -l "$out"
