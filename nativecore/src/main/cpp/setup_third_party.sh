#!/usr/bin/env bash
# setup_third_party.sh — fetch all third-party libs required by CMakeLists.txt
# Usage: ./setup_third_party.sh [--clean] [--jobs N] [--verbose]

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TP="${SCRIPT_DIR}/third_party"
JOBS=4
VERBOSE=0

# ── argument parsing ──────────────────────────────────────────────────────────

while [[ $# -gt 0 ]]; do
    case "${1}" in
        --clean)   rm -rf "$TP" ;;
        --jobs)    JOBS="${2:?--jobs requires a value}"; shift ;;
        --verbose) VERBOSE=1 ;;
        *)         echo "Unknown option: $1" >&2; exit 1 ;;
    esac
    shift
done

mkdir -p "$TP"

# ── dependency checks ─────────────────────────────────────────────────────────

MISSING=()
for cmd in git curl tar python3; do
    command -v "$cmd" &>/dev/null || MISSING+=("$cmd")
done
[[ ${#MISSING[@]} -eq 0 ]] || { echo "Missing dependencies: ${MISSING[*]}" >&2; exit 1; }

# ── logging ───────────────────────────────────────────────────────────────────

log()  { echo "[$(date +%H:%M:%S)] $*"; }
vlog() { [[ $VERBOSE -eq 1 ]] && echo "  $*" || true; }

# ── helpers ───────────────────────────────────────────────────────────────────

git_clone() {  # <url> <dir> <ref>
    local url="$1" dir="$2" ref="$3"
    local name; name="$(basename "$dir")"
    if [[ -d "$dir/.git" ]]; then
        vlog "skip $name (already cloned)"
        return 0
    fi
    log "→ cloning $name @ $ref"
    # Suppress detached-HEAD advice that pollutes parallel output.
    # --no-single-branch is required for repos where the ref is an annotated
    # tag object (not a commit) — e.g. libwebp v1.4.0 — so git can dereference it.
    git \
        -c advice.detachedHead=false \
        clone --quiet --depth 1 --no-single-branch --branch "$ref" "$url" "$dir" \
        || { echo "ERROR: failed to clone $name from $url" >&2; return 1; }
}

fetch_tar() {  # <url> <dir> [strip=1] [ext=gz]
    local url="$1" dir="$2" strip="${3:-1}" ext="${4:-gz}"
    local name; name="$(basename "$dir")"
    if [[ -n "$(ls -A "$dir" 2>/dev/null)" ]]; then
        vlog "skip $name (already extracted)"
        return 0
    fi
    log "→ downloading $name"
    mkdir -p "$dir"

    # Use $name in the tmp filename so parallel jobs never collide on the suffix.
    local tmp; tmp="$(mktemp "/tmp/tp_${name}_XXXXXX.tar.${ext}")"
    trap 'rm -f "$tmp"' RETURN

    if ! curl -fsSL --retry 3 --retry-delay 2 -o "$tmp" "$url"; then
        echo "ERROR: failed to download $name from $url" >&2
        rm -rf "$dir"
        return 1
    fi
    local tar_flags
    case "$ext" in
        xz)  tar_flags="-xJf" ;;
        bz2) tar_flags="-xjf" ;;
        *)   tar_flags="-xzf" ;;
    esac
    if ! tar "$tar_flags" "$tmp" -C "$dir" --strip-components="$strip"; then
        echo "ERROR: failed to extract $name" >&2
        rm -rf "$dir"
        return 1
    fi
}

assert_file() {  # <path> <msg>
    [[ -f "$1" ]] || { echo "ERROR: $2" >&2; exit 1; }
}

assert_dir() {  # <path> <msg>
    [[ -d "$1" ]] || { echo "ERROR: $2" >&2; exit 1; }
}

# ── parallel job tracking ─────────────────────────────────────────────────────

PIDS=()
NAMES=()
FAILED=()

run_bg() {  # <name> <cmd> [args…]
    local name="$1"; shift
    "$@" &
    PIDS+=($!)
    NAMES+=("$name")
}

wait_all() {
    local i=0
    for pid in "${PIDS[@]}"; do
        if ! wait "$pid"; then
            FAILED+=("${NAMES[$i]}")
        fi
        (( i++ )) || true
    done
    PIDS=(); NAMES=()
    [[ ${#FAILED[@]} -eq 0 ]] || {
        echo "" >&2
        echo "ERROR: the following fetches failed: ${FAILED[*]}" >&2
        exit 1
    }
}

maybe_wait() {
    while [[ $(jobs -rp | wc -l) -ge $JOBS ]]; do
        sleep 0.2
    done
}

# ── hpdf_config.h.in template ─────────────────────────────────────────────────
# CMakeLists.txt looks for cmake/hpdf_config.h.in to use configure_file().
# Generate it here alongside the script so it's always present.

write_hpdf_template() {
    local out="${SCRIPT_DIR}/cmake/hpdf_config.h.in"
    [[ -f "$out" ]] && { vlog "skip hpdf_config.h.in (exists)"; return 0; }
    log "→ writing cmake/hpdf_config.h.in"
    mkdir -p "${SCRIPT_DIR}/cmake"
    cat > "$out" <<'EOF'
/* Auto-generated for Android — do not edit manually */
#ifndef _HPDF_CONFIG_H
#define _HPDF_CONFIG_H
#cmakedefine LIBHPDF_HAVE_ZLIB
#cmakedefine LIBHPDF_HAVE_LIBPNG
#cmakedefine LIBHPDF_HAVE_NOZLIB
#endif /* _HPDF_CONFIG_H */
EOF
}

# ── fetch ─────────────────────────────────────────────────────────────────────

log "Fetching third-party libraries (parallel jobs: $JOBS)…"

maybe_wait; run_bg eigen        fetch_tar  "https://gitlab.com/libeigen/eigen/-/archive/3.4.0/eigen-3.4.0.tar.gz"                                          "${TP}/eigen"
maybe_wait; run_bg stb          git_clone  "https://github.com/nothings/stb.git"                                                                            "${TP}/stb"           master
maybe_wait; run_bg nanosvg      git_clone  "https://github.com/memononen/nanosvg.git"                                                                       "${TP}/nanosvg"       master
maybe_wait; run_bg tinyxml2     git_clone  "https://github.com/leethomason/tinyxml2.git"                                                                    "${TP}/tinyxml2"      master
maybe_wait; run_bg libyuv       git_clone  "https://chromium.googlesource.com/libyuv/libyuv"                                                                "${TP}/libyuv"        main
maybe_wait; run_bg libwebp      git_clone  "https://chromium.googlesource.com/webm/libwebp"                                                                 "${TP}/libwebp"       v1.4.0
maybe_wait; run_bg libharu      git_clone  "https://github.com/libharu/libharu.git"                                                                         "${TP}/libharu"       v2.4.4
maybe_wait; run_bg freetype     fetch_tar  "https://github.com/freetype/freetype/archive/refs/tags/VER-2-13-2.tar.gz"                                       "${TP}/freetype"
maybe_wait; run_bg libxml2      fetch_tar  "https://github.com/GNOME/libxml2/archive/refs/tags/v2.12.6.tar.gz"                                              "${TP}/libxml2"
maybe_wait; run_bg libjpeg      fetch_tar  "https://github.com/libjpeg-turbo/libjpeg-turbo/releases/download/3.0.3/libjpeg-turbo-3.0.3.tar.gz"             "${TP}/libjpeg-turbo"
maybe_wait; run_bg libpng       fetch_tar  "https://github.com/pnggroup/libpng/archive/refs/tags/v1.6.43.tar.gz"                                            "${TP}/libpng"
maybe_wait; run_bg podofo       fetch_tar  "https://github.com/podofo/podofo/archive/refs/tags/1.0.3.tar.gz"                                                "${TP}/podofo"

wait_all

# ── post-fetch validation ─────────────────────────────────────────────────────
# Sentinels mirror the require_tp() calls in CMakeLists.txt exactly.

log "Validating…"

assert_file "${TP}/eigen/Eigen/Core"                                "Eigen headers missing"
assert_file "${TP}/stb/stb_image.h"                                 "stb_image.h missing"
assert_file "${TP}/nanosvg/src/nanosvg.h"                           "nanosvg.h missing"
assert_file "${TP}/tinyxml2/tinyxml2.cpp"                           "tinyxml2.cpp missing"
assert_dir  "${TP}/libyuv"                                          "libyuv directory missing"
assert_file "${TP}/libwebp/CMakeLists.txt"                          "libwebp CMakeLists.txt missing"
assert_file "${TP}/libharu/src/hpdf_doc.c"                          "libharu hpdf_doc.c missing"
assert_file "${TP}/freetype/include/freetype/freetype.h"            "freetype.h missing"
assert_file "${TP}/libxml2/include/libxml/parser.h"                 "libxml2 parser.h missing"
assert_file "${TP}/libjpeg-turbo/jpeglib.h"                         "jpeglib.h missing"
assert_file "${TP}/libpng/png.h"                                    "png.h missing"
assert_file "${TP}/podofo/CMakeLists.txt"                           "podofo CMakeLists.txt missing"

# ── cmake/ template ───────────────────────────────────────────────────────────

write_hpdf_template

# ── done ──────────────────────────────────────────────────────────────────────

log "All libraries ready. Next: ./gradlew assembleDebug"