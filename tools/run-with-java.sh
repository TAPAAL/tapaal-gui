#!/usr/bin/env bash
set -euo pipefail

project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
jdk_dir="${TAPAAL_JDK_DIR:-${project_root}/.tools/jdk-25}"

if [[ ! -x "${jdk_dir}/bin/java" ]]; then
    command -v curl >/dev/null || { echo "curl is required to download Java 25." >&2; exit 1; }
    command -v tar >/dev/null || { echo "tar is required to unpack Java 25." >&2; exit 1; }

    case "$(uname -s)" in
        Linux) platform="linux" ;;
        Darwin) platform="mac" ;;
        *) echo "Unsupported platform. Install Java 25 or set TAPAAL_JDK_DIR." >&2; exit 1 ;;
    esac

    case "$(uname -m)" in
        x86_64|amd64) architecture="x64" ;;
        aarch64|arm64) architecture="aarch64" ;;
        *) echo "Unsupported architecture. Install Java 25 or set TAPAAL_JDK_DIR." >&2; exit 1 ;;
    esac

    archive_dir="$(mktemp -d)"
    trap 'rm -rf "${archive_dir}"' EXIT
    archive="${archive_dir}/temurin-jdk.tar.gz"
    url="https://api.adoptium.net/v3/binary/latest/25/ga/${platform}/${architecture}/jdk/hotspot/normal/eclipse"

    echo "Downloading Temurin Java 25 into ${jdk_dir}..."
    curl --fail --location --silent --show-error "${url}" --output "${archive}"
    mkdir -p "${project_root}/.tools"
    tar --extract --gzip --file "${archive}" --directory "${archive_dir}"
    extracted_dir="$(find "${archive_dir}" -mindepth 1 -maxdepth 1 -type d -name 'jdk-*' -print -quit)"
    [[ -n "${extracted_dir}" ]] || { echo "The Java 25 archive did not contain a JDK directory." >&2; exit 1; }
    rm -rf "${jdk_dir}"
    mv "${extracted_dir}" "${jdk_dir}"
fi

export JAVA_HOME="${jdk_dir}"
exec "${project_root}/gradlew" "$@"
