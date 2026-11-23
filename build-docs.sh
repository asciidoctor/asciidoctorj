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
#   - content.sources keeps only the sources in KEEP_SOURCES. The source "asciidoctorj" uses
#     the local repository (branches: HEAD) in place of the URL and the branches of the playbook.
#     By default, the build also has the sources for the Maven plugin (the component
#     maven-tools) and for the AsciiDoc language docs (the component asciidoc).
#     The AsciidoctorJ docs link to these docs.
#   - site.start_page is removed, because the ROOT component is not in the build.
#   - runtime.log.failure_level is set to fatal. The script decides about success itself (see below).
#
# Usage: ./build-docs.sh [build|clean <work-folder>]
#
# The build contains docs of other projects, and these docs can have their own problems.
# The script prints all warnings and errors, but only those of the AsciidoctorJ docs fail the build:
#   - Exit code 0: Antora finished, and the AsciidoctorJ docs have no warning and no error.
#     Messages of other sources are marked with "(ignored)".
#   - Exit code 1: the AsciidoctorJ docs have at least one warning or error.
#   - Other exit code: Antora stopped with a fatal error (exit code of Antora).
# Each build uses a new random work folder. The script prints its path.
# The script does not delete the work folder, so you can read the output and the log.
# The terminal shows the Antora messages in a readable format (this needs jq).
# The file build.log in the work folder has the raw JSON messages.
#
# Environment variables:
#   PLAYBOOK_URL   Repository of the playbook.          Default: upstream docs.asciidoctor.org
#   PLAYBOOK_REF   Branch of the playbook repository.    Default: main
#   KEEP_SOURCES   The sources to build.             Default: asciidoctorj,asciidoctor-maven-plugin,asciidoc-lang.git
#                  This is a list of names of the sources of the playbook
#                  (commas or spaces between the names). A name must be exactly the last
#                  part of the source URL. If the URL ends with ".git", the name must also
#                  end with ".git". The source "asciidoctorj" is not taken from the
#                  playbook. The script builds the local repository from HEAD instead.
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
KEEP_SOURCES="${KEEP_SOURCES:-asciidoctorj,asciidoctor-maven-plugin,asciidoc-lang.git}"
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
  for tool in git docker awk sed jq; do
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

# jq function: true if a log entry belongs to the AsciidoctorJ docs (the local source).
# Entries without a source are also counted, because the script cannot know their origin.
JQ_OURS='def ours: (.source == null) or (.source.worktree == $src) or ((.source.local // "") | startswith($src + "/"));
         def problem: (.level | IN("warn", "error", "fatal"));'

# Show the JSON log lines of Antora in a readable format:
#   LEVEL  [source:module] file[:line]: message
# The source is the repository name, and the module is the Antora module of the file.
# A warning or error of another source has "(ignored)" after the level.
# Lines that are not JSON are not changed. The log file always has the raw JSON lines.
format_log() {
  jq -R -r --arg src "$CONTAINER_SOURCE" "$JQ_OURS"'
    (fromjson? // .) as $entry
    | if ($entry | type) == "object" and ($entry.msg != null) then
        ($entry.file.path // "") as $path
        | ($path | capture("modules/(?<module>[^/]+)/(?<rest>.*)")? // {module: "", rest: ($path | sub("^/asciidoctorj/"; ""))}) as $location
        | ($entry.source.url // "" | sub("/+$"; "") | sub(".*/"; "") | sub("\\.git$"; "")) as $source
        | ([$source, $location.module] | map(select(. != "")) | join(":")) as $origin
        | (if ($entry | problem) and (($entry | ours) | not) then "(ignored)" else "" end) as $ignored
        | "\($entry.level | ascii_upcase)\($ignored)  "
          + (if $origin != "" then "[\($origin)] " else "" end)
          + (if $location.rest != "" then $location.rest else "-" end)
          + (if $entry.file.line then ":\($entry.file.line)" else "" end)
          + ": \($entry.msg)"
      else
        $entry | if type == "string" then . else tojson end
      end'
}

# Count the warnings and errors in the log file.
# Argument: "ours" for the AsciidoctorJ docs, or "others" for all other sources.
count_problems() {
  local which="$1" filter
  if [ "$which" = "ours" ]; then filter='select(ours)'; else filter='select(ours | not)'; fi
  jq -R -r --arg src "$CONTAINER_SOURCE" "$JQ_OURS"'
    fromjson? | select(type == "object" and problem) | '"$filter"' | 1' "$LOG_FILE" | wc -l | tr -d ' '
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

  awk -v url="$CONTAINER_SOURCE" -v keep="$KEEP_SOURCES" '
    BEGIN {
      n = split(keep, names, /[ ,|]+/)
      for (i = 1; i <= n; i++) if (names[i] != "") wanted[names[i]] = 1
    }
    # The repository name is the last part of the URL (this includes ".git" if the URL has it).
    function repository(line,   name) {
      name = line
      sub(/^  - url:[ \t]*/, "", name)
      sub(/[ \t]+$/, "", name)
      sub(/\/+$/, "", name)
      sub(/.*\//, "", name)
      return name
    }
    # Print the source if it is in the list. For asciidoctorj, use the local repository from HEAD.
    function flush(   i, count, lines) {
      if (block != "" && (block_name in wanted)) {
        if (block_name == "asciidoctorj") {
          count = split(block, lines, "\n")
          for (i = 1; i < count; i++) {
            if (lines[i] ~ /^  - url:/) print "  - url: " url
            else if (lines[i] ~ /^    branches:/) print "    branches: HEAD"
            else print lines[i]
          }
        } else {
          printf "%s", block
        }
      }
      block = ""
    }
    /^content:/ {
      print "content:"
      print "  sources:"
      in_content = 1
      next
    }
    in_content && /^[^ #]/ { flush(); in_content = 0 }
    in_content && /^  - url:/ { flush(); block_name = repository($0); block = $0 "\n"; next }
    in_content && /^    / { if (block != "") block = block $0 "\n"; next }
    in_content { next }
    { print }
  ' "$input" | sed '/^  start_page:/d' >"$output"

  # Antora must not stop for warnings and errors. The script reads the log and decides.
  sed -i 's/^    failure_level: .*/    failure_level: fatal/' "$output"
  grep -q '^    failure_level: fatal' "$output" || die "Could not change failure_level in the playbook."

  if [ -n "$INCLUDE_DEV" ]; then
    sed -i '/exclude-dev-version\.js/d' "$output"
  fi

  grep -q 'url: '"$CONTAINER_SOURCE" "$output" || die "Could not change content.sources in the playbook."
  echo "Playbook: $output"
  echo "Sources of the build:"
  grep -E '^  - url:' <(sed -n '/^content:/,/^[a-z]/p' "$output") | sed 's/^  - url: /  - /'
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
  echo

  local status
  set +e
  # Same options as the Netlify production build, without the Algolia and PDF extensions.
  docker_run antora \
    --fetch \
    --html-url-extension-style=indexify \
    --redirect-facility=netlify \
    --to-dir "$CONTAINER_WORK/public" \
    build-playbook.yml 2>&1 | tee "$LOG_FILE" | format_log
  status="${PIPESTATUS[0]}"
  set -e
  echo
  if [ "$status" != "0" ]; then
    echo "RESULT: Antora stopped with a fatal error (exit code $status). See $LOG_FILE"
    return "$status"
  fi
  local ours others
  ours="$(count_problems ours)"
  others="$(count_problems others)"
  echo "AsciidoctorJ docs: $ours warnings and errors"
  echo "Other sources: $others warnings and errors (ignored)"
  if [ "$ours" = "0" ]; then
    echo "RESULT: build passed. Output: $OUTPUT_DIR"
    return 0
  fi
  echo "RESULT: build failed, the AsciidoctorJ docs have problems. See $LOG_FILE"
  return 1
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
