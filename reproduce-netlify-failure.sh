#!/usr/bin/env bash
#
# Reproduce the failed Netlify build of docs.asciidoctor.org locally.
# The build runs in the Antora Docker image.
# See REPRODUCE_NETLIFY_FAILURE.adoc for the background.
#
# Usage: ./reproduce-netlify-failure.sh [setup|broken|fixed|all|clean]
#
#   setup   Prepare the work directory (source clone, extension install).
#   broken  Build the docs from the broken commit.
#   fixed   Build the docs from the fixed commit.
#   all     Run setup, broken and fixed, then print a summary. This is the default.
#   clean   Delete the work directory.
#
# Environment variables:
#   BROKEN_REF     Commit with the broken symlinks.      Default: a1332cf2
#   FIXED_REF      Commit with the fix.                  Default: HEAD of this repository
#   SOURCE_REPO    AsciidoctorJ repository to read from. Default: the folder of this script
#   ANTORA_IMAGE   Antora Docker image.                  Default: docker.io/antora/antora:latest
#   TABS_VERSION   Version of @asciidoctor/tabs.        Default: 1.0.0-beta.6 (as in the playbook)
#   FAILURE_LEVEL  Antora log failure level.             Default: warn (same as Netlify)
#   WORK_DIR       Folder for all generated files.       Default: $TMPDIR/asciidoctorj-docs-repro

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SOURCE_REPO="${SOURCE_REPO:-$SCRIPT_DIR}"
BROKEN_REF="${BROKEN_REF:-a1332cf2}"
FIXED_REF="${FIXED_REF:-HEAD}"
ANTORA_IMAGE="${ANTORA_IMAGE:-docker.io/antora/antora:latest}"
TABS_VERSION="${TABS_VERSION:-1.0.0-beta.6}"
FAILURE_LEVEL="${FAILURE_LEVEL:-warn}"
WORK_DIR="${WORK_DIR:-${TMPDIR:-/tmp}/asciidoctorj-docs-repro}"

# Paths on the host.
SITE_DIR="$WORK_DIR/site"
SOURCE_CLONE="$WORK_DIR/asciidoctorj-src"
RESULT_DIR="$WORK_DIR/results"

# The same folders, as seen inside the container.
CONTAINER_WORK="/work"
CONTAINER_SITE="$CONTAINER_WORK/site"
CONTAINER_SOURCE="$CONTAINER_WORK/asciidoctorj-src"

log() { printf '\n==> %s\n' "$*"; }
die() { printf 'ERROR: %s\n' "$*" >&2; exit 1; }

check_tools() {
  local tool
  for tool in git docker; do
    command -v "$tool" >/dev/null 2>&1 || die "'$tool' is not installed."
  done
  docker info >/dev/null 2>&1 || die "The Docker daemon is not running or you cannot access it."
}

# Run a command in the Antora image.
# The work folder is mounted and the container runs as your user.
# Arguments: <entrypoint> <arguments...>
docker_run() {
  local entrypoint="$1"
  shift
  docker run --rm \
    --user "$(id -u):$(id -g)" \
    --env HOME="$CONTAINER_WORK/home" \
    --volume "$WORK_DIR:$CONTAINER_WORK" \
    --workdir "$CONTAINER_SITE" \
    --entrypoint "$entrypoint" \
    "$ANTORA_IMAGE" "$@"
}

setup_site() {
  log "Preparing the site folder"
  mkdir -p "$SITE_DIR" "$WORK_DIR/home"
  echo "Work folder: $WORK_DIR"
  echo "Site folder: $SITE_DIR"
  cat >"$SITE_DIR/package.json" <<EOF
{
  "name": "asciidoctorj-docs-repro",
  "private": true
}
EOF

  log "Pulling the Antora image: $ANTORA_IMAGE"
  docker pull --quiet "$ANTORA_IMAGE" >/dev/null

  log "Installing @asciidoctor/tabs $TABS_VERSION in the image"
  docker_run npm install --no-audit --no-fund "@asciidoctor/tabs@$TABS_VERSION"
  echo "Antora version in the image: $(docker_run antora --version | head -n 1)"
}

setup_source() {
  log "Preparing a local copy of AsciidoctorJ"
  local broken_sha fixed_sha
  broken_sha="$(git -C "$SOURCE_REPO" rev-parse --verify "$BROKEN_REF^{commit}")"
  fixed_sha="$(git -C "$SOURCE_REPO" rev-parse --verify "$FIXED_REF^{commit}")"
  mkdir -p "$WORK_DIR"
  rm -rf "$SOURCE_CLONE"
  git clone --quiet --no-hardlinks "$SOURCE_REPO" "$SOURCE_CLONE"
  git -C "$SOURCE_CLONE" branch --force repro-broken "$broken_sha"
  git -C "$SOURCE_CLONE" branch --force repro-fixed "$fixed_sha"
  echo "AsciidoctorJ clone: $SOURCE_CLONE"
  echo "repro-broken -> $broken_sha"
  echo "repro-fixed  -> $fixed_sha (changes that are not committed are not included)"
}

# Write the playbook to the site folder. Paths in the file are container paths.
write_playbook() {
  local name="$1" branch="$2" file="$SITE_DIR/repro-playbook-$1.yml"
  cat >"$file" <<EOF
runtime:
  cache_dir: $CONTAINER_WORK/cache
  log:
    failure_level: $FAILURE_LEVEL
site:
  title: Asciidoctor Docs (repro $name)
  url: https://docs.asciidoctor.org
git:
  ensure_git_suffix: false
urls:
  latest_version_segment: latest
  latest_version_segment_strategy: redirect:to
content:
  sources:
  - url: $CONTAINER_SOURCE
    branches: $branch
    start_path: docs
asciidoc:
  extensions:
  - '@asciidoctor/tabs'
ui:
  bundle:
    url: https://github.com/asciidoctor/docs.asciidoctor.org-ui/releases/download/prod-latest/ui-bundle.zip
    snapshot: true
EOF
}

run_build() {
  local name="$1" branch="$2" logfile status
  write_playbook "$name" "$branch"
  logfile="$RESULT_DIR/$name.log"
  mkdir -p "$RESULT_DIR"
  log "Building the docs from $branch"
  echo "Docker image: $ANTORA_IMAGE"
  echo "Playbook: $SITE_DIR/repro-playbook-$name.yml"
  echo "Output folder: $WORK_DIR/public-$name"
  echo "Log: $logfile"
  set +e
  # Same options as the Netlify production build, without the Algolia extension.
  docker_run antora \
    --fetch \
    --html-url-extension-style=indexify \
    --redirect-facility=netlify \
    --to-dir "$CONTAINER_WORK/public-$name" \
    "repro-playbook-$name.yml" 2>&1 | tee "$logfile"
  status="${PIPESTATUS[0]}"
  set -e
  echo "$status" >"$RESULT_DIR/$name.status"
  echo "Exit code for $name: $status"
}

read_status() {
  if [ -f "$RESULT_DIR/$1.status" ]; then cat "$RESULT_DIR/$1.status"; else echo "n/a"; fi
}

summary() {
  local broken_status fixed_status
  broken_status="$(read_status broken)"
  fixed_status="$(read_status fixed)"
  log "Summary"
  echo "broken ($BROKEN_REF): exit code $broken_status"
  echo "fixed  ($FIXED_REF): exit code $fixed_status"
  if [ -f "$RESULT_DIR/broken.log" ]; then
    echo
    echo "Lines about symlinks or missing files in the broken build:"
    grep -iE 'symlink|ENOENT|no such file|broken' "$RESULT_DIR/broken.log" || echo "(none)"
  fi
  echo
  if [ "$broken_status" != "0" ] && [ "$broken_status" != "n/a" ] && [ "$fixed_status" = "0" ]; then
    echo "RESULT: failure reproduced. The broken commit fails and the fixed commit passes."
  else
    echo "RESULT: failure not reproduced as expected."
    echo "Compare $RESULT_DIR/broken.log and $RESULT_DIR/fixed.log."
    echo "Warnings about other components (for example, xrefs) can fail both builds."
    echo "This setup builds only AsciidoctorJ. Try FAILURE_LEVEL=error to ignore them."
    return 2
  fi
}

ensure_setup() {
  if [ ! -d "$SITE_DIR/node_modules/@asciidoctor/tabs" ] || [ ! -d "$SOURCE_CLONE/.git" ]; then
    check_tools
    setup_site
    setup_source
  fi
}

main() {
  case "${1:-all}" in
    setup)  check_tools; setup_site; setup_source ;;
    broken) ensure_setup; run_build broken repro-broken; summary ;;
    fixed)  ensure_setup; run_build fixed repro-fixed; summary ;;
    all)
      check_tools; setup_site; setup_source
      run_build broken repro-broken
      run_build fixed repro-fixed
      summary
      ;;
    clean)  rm -rf "$WORK_DIR"; echo "Deleted $WORK_DIR" ;;
    *)      die "Unknown command '$1'. Use setup, broken, fixed, all or clean." ;;
  esac
}

main "$@"
