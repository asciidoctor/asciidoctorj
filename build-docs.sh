#!/usr/bin/env bash
#
# Build the AsciidoctorJ docs with the Antora Docker image, to check that the docs are not broken.
# Run the script from the AsciidoctorJ repository (any folder in it).
#
# The script builds the docs of the local copy, from the files in the working tree.
# This includes changes that are not committed. In CI, this is the checked-out commit.
#
# The script checks out the real playbook, antora-playbook.yml, from the
# docs.asciidoctor.org repository. It keeps all settings and extensions of that playbook.
# It changes only these things, so that the build contains only the AsciidoctorJ docs:
#   - content.sources is replaced with the local AsciidoctorJ repository.
#   - site.start_page is removed, because the ROOT component is not in the build.
#
# Usage: ./build-docs.sh [build|clean <work-folder>]
#
# The exit code is 0 if the build passes, and not 0 if the build fails.
# Each build uses a new random work folder. The script prints its path.
# The script does not delete the work folder, so you can read the output and the log.
#
# Environment variables:
#   PLAYBOOK_URL   Repository of the playbook.          Default: upstream docs.asciidoctor.org
#   PLAYBOOK_REF   Branch of the playbook repository.    Default: main
#   INCLUDE_DEV    Set to 1 to keep "-dev" versions.     Default: unset (same as Netlify)
#   ANTORA_IMAGE   Antora Docker image.                  Default: docker.io/antora/antora:latest
#   TABS_VERSION   Version of @asciidoctor/tabs.        Default: 1.0.0-beta.6 (as in the playbook repo)
#   WORK_DIR       Folder for all generated files.       Default: a new random folder in $RUNNER_TEMP or $TMPDIR
#
# NOTE: The playbook uses the extension ext/exclude-dev-version.js.
# It removes versions with a prerelease that ends with "-dev".
# The docs of "main" have the prerelease "-dev", so Netlify does not publish them.
# The extension runs after Antora reads the files, so broken files are still found.
# Set INCLUDE_DEV=1 to remove this extension and to get a site with the docs of this repository.

set -euo pipefail

PLAYBOOK_URL="${PLAYBOOK_URL:-https://github.com/asciidoctor/docs.asciidoctor.org.git}"
PLAYBOOK_REF="${PLAYBOOK_REF:-main}"
INCLUDE_DEV="${INCLUDE_DEV:-}"
ANTORA_IMAGE="${ANTORA_IMAGE:-docker.io/antora/antora:latest}"
TABS_VERSION="${TABS_VERSION:-1.0.0-beta.6}"
WORK_DIR="${WORK_DIR:-}"

# The same folders, as seen inside the container.
CONTAINER_WORK="/work"
CONTAINER_PLAYBOOK_DIR="$CONTAINER_WORK/playbook"
CONTAINER_SOURCE="/asciidoctorj"

log() { printf '\n==> %s\n' "$*"; }
die() { printf 'ERROR: %s\n' "$*" >&2; exit 1; }

check_tools() {
  local tool
  for tool in git docker awk sed; do
    command -v "$tool" >/dev/null 2>&1 || die "'$tool' is not installed."
  done
  docker info >/dev/null 2>&1 || die "The Docker daemon is not running or you cannot access it."
}

# Find the repository of the current folder and check that it has docs.
find_source() {
  SOURCE_REPO="$(git rev-parse --show-toplevel 2>/dev/null)" \
    || die "The current folder is not in a git repository. Run the script from the AsciidoctorJ repository."
  [ -f "$SOURCE_REPO/docs/antora.yml" ] \
    || die "$SOURCE_REPO has no docs/antora.yml. Run the script from the AsciidoctorJ repository."
  echo "AsciidoctorJ repository: $SOURCE_REPO"
  echo "Docs version: $(git -C "$SOURCE_REPO" log -1 --format='%h %s')$(
    [ -z "$(git -C "$SOURCE_REPO" status --porcelain -- docs)" ] || echo ' (docs/ has changes that are not committed)')"
}

# Create the work folder (random name unless WORK_DIR is set) and set the paths on the host.
init_paths() {
  if [ -z "$WORK_DIR" ]; then
    WORK_DIR="$(mktemp -d "${RUNNER_TEMP:-${TMPDIR:-/tmp}}/asciidoctorj-docs-build.XXXXXX")"
  else
    mkdir -p "$WORK_DIR"
  fi
  WORK_DIR="$(cd "$WORK_DIR" && pwd)"
  PLAYBOOK_DIR="$WORK_DIR/playbook"
  OUTPUT_DIR="$WORK_DIR/public"
  LOG_FILE="$WORK_DIR/build.log"
  echo "Work folder: $WORK_DIR"
}

# Run a command in the Antora image.
# The work folder is mounted, and the AsciidoctorJ repository is mounted as read-only.
# The container runs as your user.
# Arguments: <entrypoint> <arguments...>
docker_run() {
  local entrypoint="$1"
  shift
  docker run --rm \
    --user "$(id -u):$(id -g)" \
    --env HOME="$CONTAINER_WORK/home" \
    --volume "$WORK_DIR:$CONTAINER_WORK" \
    --volume "$SOURCE_REPO:$CONTAINER_SOURCE:ro" \
    --workdir "$CONTAINER_PLAYBOOK_DIR" \
    --entrypoint "$entrypoint" \
    "$ANTORA_IMAGE" "$@"
}

setup_playbook() {
  log "Checking out the playbook repository"
  mkdir -p "$WORK_DIR/home"
  git clone --quiet --depth 1 --branch "$PLAYBOOK_REF" "$PLAYBOOK_URL" "$PLAYBOOK_DIR"
  echo "Playbook clone: $PLAYBOOK_DIR"
  echo "Playbook commit: $(git -C "$PLAYBOOK_DIR" log -1 --format='%h %s')"

  log "Pulling the Antora image: $ANTORA_IMAGE"
  docker pull --quiet "$ANTORA_IMAGE" >/dev/null

  # The playbook needs this extension. Install it next to the playbook, so that Antora finds it.
  log "Installing @asciidoctor/tabs $TABS_VERSION in the image"
  echo '{ "name": "asciidoctorj-docs-build", "private": true }' >"$PLAYBOOK_DIR/package.json"
  docker_run npm install --no-audit --no-fund "@asciidoctor/tabs@$TABS_VERSION"
  echo "Antora version in the image: $(docker_run antora --version | head -n 1)"
}

# Create the playbook for this build from the real playbook.
# Paths in the new playbook are container paths.
# "branches: HEAD" is the checked-out branch, or the checked-out commit in CI (detached HEAD).
# Antora reads the files of the checked-out branch from the working tree.
write_playbook() {
  local input="$PLAYBOOK_DIR/antora-playbook.yml"
  local output="$PLAYBOOK_DIR/build-playbook.yml"
  [ -f "$input" ] || die "The playbook repository has no antora-playbook.yml."

  awk -v url="$CONTAINER_SOURCE" '
    /^content:/ {
      print "content:"
      print "  sources:"
      print "  - url: " url
      print "    branches: HEAD"
      print "    start_path: docs"
      skip = 1
      next
    }
    skip && /^[^ #]/ { skip = 0 }
    !skip { print }
  ' "$input" | sed '/^  start_page:/d' >"$output"

  if [ -n "$INCLUDE_DEV" ]; then
    sed -i '/exclude-dev-version\.js/d' "$output"
  fi

  grep -q 'url: '"$CONTAINER_SOURCE" "$output" || die "Could not change content.sources in the playbook."
  echo "Playbook: $output"
}

build() {
  check_tools
  find_source
  init_paths
  setup_playbook
  log "Building the docs"
  write_playbook
  echo "Docker image: $ANTORA_IMAGE"
  echo "Output folder: $OUTPUT_DIR"
  echo "Log: $LOG_FILE"
  local status
  set +e
  # Same options as the Netlify production build, without the Algolia and PDF extensions.
  docker_run antora \
    --fetch \
    --html-url-extension-style=indexify \
    --redirect-facility=netlify \
    --to-dir "$CONTAINER_WORK/public" \
    build-playbook.yml 2>&1 | tee "$LOG_FILE"
  status="${PIPESTATUS[0]}"
  set -e
  echo
  if [ "$status" = "0" ]; then
    echo "RESULT: build passed. Output: $OUTPUT_DIR"
  else
    echo "RESULT: build failed (exit code $status). See $LOG_FILE"
  fi
  return "$status"
}

case "${1:-build}" in
  build) build ;;
  clean)
    [ -n "${2:-}" ] || die "Usage: $0 clean <work-folder>"
    rm -rf "$2"
    echo "Deleted $2"
    ;;
  *)     die "Unknown command '$1'. Use build or clean <work-folder>." ;;
esac
