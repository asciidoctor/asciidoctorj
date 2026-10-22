#!/usr/bin/env bash
# Compares the HTML5 output of Asciidoctor Ruby and of the Java port on a corpus.
# Usage: tools/compare/compare.sh <name> <root-dir> [out-base]
# Env: ATTRS (extra attributes, e.g. "toc sectnums"), EXT (file extension, default adoc), ASCIIDOCTOR_RB (Ruby source checkout, default ~/Workspace/opensource/asciidoctor), JAVA_HOME (JDK 25)
set -euo pipefail
here=$(cd "$(dirname "$0")" && pwd)
project=$(cd "$here/../.." && pwd)
name=$1; root=$(cd "$2" && pwd); base=${3:-${TMPDIR:-/tmp}/asciidoctor-compare}
rb=${ASCIIDOCTOR_RB:-$HOME/Workspace/opensource/asciidoctor}
java=${JAVA_HOME:-$HOME/.sdkman/candidates/java/25.0.4-tem}/bin/java
out=$base/$name
rm -rf "$out"; mkdir -p "$out"
(cd "$root" && find . -name "*.${EXT:-adoc}" -not -path '*/node_modules/*' -not -path '*/.git/*' | sed 's|^\./||' | sort) > "$out/files.txt"
echo "$name: $(wc -l < "$out/files.txt") files"
ruby -I "$rb/lib" "$here/convert_ruby.rb" "$out/files.txt" "$root" "$out/ruby" &
"$java" -cp "$project/asciidoctorj-native/build/classes/java/main:$project/asciidoctorj-native/build/resources/main:$project/asciidoctorj-api/build/classes/java/main" "$here/ConvertJava.java" "$out/files.txt" "$root" "$out/java"
wait
: > "$out/html.diff"; : > "$out/log.diff"; ndiff=0; nlog=0
while read -r rel; do
  if ! diff -u --label "ruby/$rel" --label "java/$rel" "$out/ruby/$rel.html" "$out/java/$rel.html" >> "$out/html.diff"; then
    ndiff=$((ndiff+1)); echo "$rel" >> "$out/html-differs.txt"
  fi
  if ! diff -u --label "ruby/$rel.log" --label "java/$rel.log" "$out/ruby/$rel.log" "$out/java/$rel.log" >> "$out/log.diff"; then
    nlog=$((nlog+1))
  fi
done < "$out/files.txt"
echo "$name: $ndiff HTML differences, $nlog log differences -> $out"
