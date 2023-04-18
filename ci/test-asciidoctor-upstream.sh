#!/bin/bash
# This script runs the AsciidoctorJ tests against the specified ref of the Asciidoctor Ruby gem.

set -euo pipefail

# to build against a tag, set REF to a git tag name (e.g., refs/tags/v1.5.2)
REF=${1:-refs/heads/main}
if [[ $REF != refs/* ]]; then
  REF=refs/heads/$REF
fi
readonly BASE_REF="${REF##*/}"
readonly SRC_DIR="asciidoctor-${BASE_REF#v}"
readonly WORKING_DIR="build/maven"

rm -rf $WORKING_DIR && mkdir -p $WORKING_DIR
pushd $WORKING_DIR

## Install gem
wget --quiet -O $SRC_DIR.zip https://github.com/asciidoctor/asciidoctor/archive/$REF.zip
unzip -q $SRC_DIR.zip
cp ../../ci/asciidoctor-gem-installer.pom $SRC_DIR/pom.xml
cd $SRC_DIR
# force SNAPSHOT trailing to keep always consistency, gem-maven-plugin adds it depending on version format, breaking the build
readonly ASCIIDOCTOR_VERSION="$(grep 'VERSION' ./lib/asciidoctor/version.rb | sed "s/.*'\(.*\)'.*/\1/" | sed "s/[.]dev$/.dev-SNAPSHOT/")-SNAPSHOT"
# we don't use sed -i here for compatibility with OSX
sed "s;<version></version>;<version>$ASCIIDOCTOR_VERSION</version>;" pom.xml > pom.xml.sedtmp && \
  mv -f pom.xml.sedtmp pom.xml

# we override the jruby version to latest
mvn install -B --no-transfer-progress \
  -Dgemspec="asciidoctor.gemspec" \
  -Djruby.version="9.2.21.0"
popd

## Test against installed gem
./gradlew --no-daemon -S -Pskip.signing -PasciidoctorGemVersion="$ASCIIDOCTOR_VERSION" -PuseMavenLocal=true check
exit $?
