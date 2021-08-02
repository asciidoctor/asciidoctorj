#!/bin/bash
# This script runs the AsciidoctorJ tests against the specified ref of the Asciidoctor Ruby gem.

GRADLE_CMD=./gradlew
# to build against a tag, set REF to a git tag name (e.g., refs/tags/v1.5.2)
REF=refs/heads/master
if [ ! -z "$1" ]; then
  REF=$1
  if [[ $REF != refs/* ]]; then
    REF=refs/heads/$REF
  fi
fi
BASE_REF=${REF##*/}
SRC_DIR=asciidoctor-${BASE_REF#v}
rm -rf build/maven && mkdir -p build/maven && cd build/maven
wget --quiet -O $SRC_DIR.zip https://github.com/asciidoctor/asciidoctor/archive/$REF.zip
unzip -q $SRC_DIR.zip
cp ../../asciidoctor-gem-installer.pom $SRC_DIR/pom.xml
cd $SRC_DIR
ASCIIDOCTOR_VERSION=`grep 'VERSION' ./lib/asciidoctor/version.rb | sed "s/.*'\(.*\)'.*/\1/" | sed "s/[.]dev$/.dev-SNAPSHOT/"`
# we don't use sed -i here for compatibility with OSX
sed "s;<version></version>;<version>$ASCIIDOCTOR_VERSION</version>;" pom.xml > pom.xml.sedtmp && \
  mv -f pom.xml.sedtmp pom.xml

#we override the jruby version here with one supported by java9, additionally java9 needs some add-opens arguments that need to be ignored on older jvms
mvn install --no-transfer-progress -Dgemspec=asciidoctor.gemspec -Djruby.version=9.2.17.0 -Djruby.jvmargs="-XX:+IgnoreUnrecognizedVMOptions --add-opens=java.base/java.security.cert=ALL-UNNAMED --add-opens=java.base/java.security=ALL-UNNAMED --add-opens=java.base/java.util.zip=ALL-UNNAMED"

cd ../..
#rm -rf maven
cd ..

$GRADLE_CMD -S -Pskip.signing -PasciidoctorGemVersion=$ASCIIDOCTOR_VERSION -PuseMavenLocal=true :asciidoctorj:clean :asciidoctorj:check
exit $?
