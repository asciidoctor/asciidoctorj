#!/usr/bin/env bash
# Smoke test for the native image of the AsciidoctorJ CLI.
# Usage: tools/native-smoke-test/run.sh <native-executable> [<jar>]
# The .exe extension of the executable may be omitted on Windows.
# If the jar is specified, the output of the native executable is compared with the output of the JVM.
set -euo pipefail
cli=$1
if [ ! -f "$cli" ] && [ -f "$cli.exe" ]; then cli=$cli.exe; fi
jar=${2:-}
dir=$(cd "$(dirname "$0")" && pwd)
work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT
failures=0

pass() { echo "ok - $1"; }
fail() { echo "not ok - $1"; failures=$((failures + 1)); }
check() { # check <description> <file> <expected text>...
  local desc=$1 file=$2; shift 2
  for expected in "$@"; do
    if ! grep -qF -- "$expected" "$file"; then
      fail "$desc (missing: $expected)"
      return
    fi
  done
  pass "$desc"
}

"$cli" --version > "$work/version.txt"
check 'prints version' "$work/version.txt" 'AsciidoctorJ ' 'Runtime Environment'
if grep -q 'AsciidoctorJ unknown' "$work/version.txt"; then fail 'reports AsciidoctorJ version (embedded resource)'; else pass 'reports AsciidoctorJ version (embedded resource)'; fi

"$cli" --help > "$work/help.txt"
check 'prints usage' "$work/help.txt" 'Usage: asciidoctor' '--backend'

"$cli" --help syntax > "$work/syntax.txt"
check 'prints syntax reference (embedded resource)' "$work/syntax.txt" '= AsciiDoc Syntax'

"$cli" --help manpage > "$work/manpage-help.txt"
check 'prints man page (embedded resource)' "$work/manpage-help.txt" '.TH "ASCIIDOCTOR"'

printf '= Hello\n\nA *bold* statement.\n' | "$cli" -o - - > "$work/stdin.html"
check 'converts stdin to stdout' "$work/stdin.html" '<h1>Hello</h1>' '<strong>bold</strong>' 'Asciidoctor default stylesheet'

printf 'A *bold* statement.\n' | "$cli" -s -o - - > "$work/embedded.html"
check 'converts embedded document' "$work/embedded.html" '<div class="paragraph">'
if grep -q '<html' "$work/embedded.html"; then fail 'embedded document has no header and footer'; else pass 'embedded document has no header and footer'; fi

cp "$dir/sample.adoc" "$dir/partial.adoc" "$dir/manpage.adoc" "$work/"
"$cli" -a reproducible "$work/sample.adoc"
check 'converts file next to source' "$work/sample.html" \
  '<div id="toc" class="toc">' '<h2 id="_lists_and_tables">1. Lists and Tables</h2>' '<ol class="loweralpha" type="a">' \
  '<td class="tableblock halign-left valign-top"><div class="content"><div class="ulist">' 'This content was included.' \
  '<code class="language-java hljs" data-lang="java">' '<i class="conum" data-value="1"></i>' '<kbd>Ctrl</kbd>' 'Ünïcödé, 日本語, 🚀' \
  'href="#_footnotedef_1"' 'MathJax.js' 'highlight.min.js' 'font-awesome'

"$cli" -b docbook5 -a reproducible -o "$work/sample.xml" "$work/sample.adoc"
check 'converts to DocBook' "$work/sample.xml" '<article xmlns="http://docbook.org/ns/docbook"' '<title>Smoke Test Document</title>'

"$cli" -b manpage -o "$work/smoketest.1" "$work/manpage.adoc"
check 'converts to man page' "$work/smoketest.1" '.TH "SMOKETEST" "1"' '\fBsmoketest\fP'

mkdir "$work/out"
"$cli" -D "$work/out" -a linkcss -a copycss "$work/sample.adoc"
if [ -f "$work/out/sample.html" ] && [ -f "$work/out/asciidoctor.css" ]; then pass 'writes to destination dir and copies stylesheet'; else fail 'writes to destination dir and copies stylesheet'; fi

if "$cli" "$work/does-not-exist.adoc" 2> "$work/missing.txt"; then fail 'fails on missing input file'; else check 'fails on missing input file' "$work/missing.txt" 'input file'; fi

printf '= Title\n\n=== Out of sequence\n' > "$work/warn.adoc"
if "$cli" --failure-level WARN -o - "$work/warn.adoc" > /dev/null 2> "$work/warn.txt"; then fail 'exits with non-zero status when failure level is reached'; else check 'exits with non-zero status when failure level is reached' "$work/warn.txt" 'section title out of sequence'; fi

if [ -n "$jar" ]; then
  for backend in html5 docbook5; do
    "$cli" -b $backend -a reproducible -o "$work/native.$backend" "$work/sample.adoc"
    java -jar "$jar" -b $backend -a reproducible -o "$work/jvm.$backend" "$work/sample.adoc"
    if diff -u "$work/jvm.$backend" "$work/native.$backend"; then pass "native output matches JVM output ($backend)"; else fail "native output matches JVM output ($backend)"; fi
  done
fi

if [ $failures -gt 0 ]; then
  echo "$failures check(s) failed"
  exit 1
fi
echo 'all checks passed'
