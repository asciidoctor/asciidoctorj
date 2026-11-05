#!/bin/sh
# Lists the failing tests from the JUnit XML reports with the first line of the failure message.
for f in */build/test-results/test/*.xml; do
  awk '/<testcase / {match($0, /name="[^"]*"/); name=substr($0, RSTART+6, RLENGTH-7); match($0, /classname="[^"]*"/); cls=substr($0, RSTART+11, RLENGTH-12)}
       /<(failure|error) / {m=""; if (match($0, /message="[^"]*/)) m=substr($0, RSTART+9, RLENGTH-9); print cls "." name " || " m}' "$f"
done | sed -E 's/^org\.asciidoctor\.//' | cut -c1-${1:-300}
