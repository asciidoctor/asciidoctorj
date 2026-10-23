#!/bin/sh
# Compiles the project and lists the compilation errors with the corresponding source line.
"$(dirname "$0")/../gradlew" -q --offline testClasses 2>&1 | grep -E "^/.*\.java:[0-9]+: error:" | sort -u | while read -r l; do
  f=$(printf '%s' "$l" | sed -E 's/^([^:]+):([0-9]+):.*/\1 \2/'); set -- $f
  printf '%s:%s: %s || %s\n' "$(basename $1)" "$2" "$(printf '%s' "$l" | sed -E 's/^[^:]+:[0-9]+: error: //')" \
    "$(sed -n ${2}p $1 | sed 's/^ *//' | cut -c1-${WIDTH:-160})"
done
