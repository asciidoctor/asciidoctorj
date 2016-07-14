#!/bin/bash

# This script runs the AsciidoctorJ tests against the specified tag (or master) of the Asciidoctor Ruby gem.

GRADLE_CMD=./gradlew
if [ ! -z $TRAVIS_JOB_NUMBER ] && [ "$TRAVIS_PULL_REQUEST" != "false" ] && [ "${TRAVIS_JOB_NUMBER##*.}" != '1' ]; then
  exit 0
fi
# to build against a tag, set TAG to a git tag name (e.g., v1.5.2)
TAG=master
if [ "$TAG" == "master" ]; then
  SRC_DIR=asciidoctor-master
else
  SRC_DIR=asciidoctor-${TAG#v}
fi
rm -rf build/maven && mkdir -p build/maven && cd build/maven
wget --quiet -O $SRC_DIR.zip https://github.com/asciidoctor/asciidoctor/archive/$TAG.zip
unzip -q $SRC_DIR.zip
cp ../../asciidoctor-gem-installer.pom $SRC_DIR/pom.xml
cd $SRC_DIR
ASCIIDOCTOR_VERSION=`grep 'VERSION' ./lib/asciidoctor/version.rb | sed "s/.*'\(.*\)'.*/\1/"`
# we don't use sed -i here for compatibility with OSX
sed "s;<version></version>;<version>$ASCIIDOCTOR_VERSION</version>;" pom.xml > pom.xml.sedtmp && \
  mv -f pom.xml.sedtmp pom.xml
mvn install -Dgemspec=asciidoctor.gemspec
cd ../..
#rm -rf maven
cd ..
$GRADLE_CMD -S -PasciidoctorGemVersion=$ASCIIDOCTOR_VERSION -PuseMavenLocal=true :asciidoctorj:clean :asciidoctorj:check
exit $?
