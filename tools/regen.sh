#!/bin/sh
# Regenerates the given Java test classes from the Ruby test suite and applies the manual fixes.
# Usage: tools/regen.sh BlocksTest SectionsTest ...
set -e
dir=$(dirname "$0")
mkdir -p "$dir/../asciidoctorj-java/build"
for name in "$@"; do
  # the CLI tests are located in the asciidoctorj-java-cli module, the other tests in the asciidoctorj-java module
  case "$name" in
    InvokerTest | OptionsTest) module=asciidoctorj-java-cli ;;
    *) module=asciidoctorj-java ;;
  esac
  target="$dir/../$module/src/test/java/org/asciidoctor/core/$name.java"
  if grep -q "ported by hand" "$target" 2>/dev/null; then
    echo "skipping $name (ported by hand)" >&2
    continue
  fi
  file=$(echo "$name" | sed -E 's/Test$//; s/([a-z])([A-Z])/\1_\2/g' | tr 'A-Z' 'a-z')
  # NOTE write to a temporary file first so a translator failure doesn't clobber the existing test
  ruby "$dir/rb2java.rb" "$dir/../../asciidoctor/test/${file}_test.rb" "$name" > "$dir/../asciidoctorj-java/build/$name.java.tmp"
  mv "$dir/../asciidoctorj-java/build/$name.java.tmp" "$target"
done
"$dir/fix_tests.py" "$@"
