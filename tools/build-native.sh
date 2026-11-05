#!/usr/bin/env bash
# Builds the native executable of the CLI (asciidoctorj-cli/build/native/asciidoctorj) with GraalVM from the CLI jar built by
# `./gradlew :asciidoctorj-cli:assemble` (the core jar, which is referenced by the manifest of the CLI jar, is copied next to it).
# native-image is looked up in $GRAALVM_HOME/bin, then on the PATH. Extra arguments are passed to native-image.
# Usage: tools/build-native.sh [native-image options]
set -euo pipefail
cd "$(dirname "$0")/.."
jar=$(ls asciidoctorj-cli/build/libs/asciidoctorj-cli-*.jar | grep -v -e '-sources' -e '-javadoc' -e '-tests' | head -1)
native_image=native-image
case "$(uname -s)" in MINGW* | MSYS* | CYGWIN*) native_image=native-image.cmd ;; esac
if [ -n "${GRAALVM_HOME:-}" ]; then native_image="$GRAALVM_HOME/bin/$native_image"; fi
# -march=compatibility: run on any CPU of the target architecture
# -H:-ParseRuntimeOptions: don't let the VM consume the -D (destination dir) argument as a system property
mkdir -p asciidoctorj-cli/build/native
"$native_image" -jar "$jar" -o asciidoctorj-cli/build/native/asciidoctorj -march=compatibility -H:-ParseRuntimeOptions "$@"
