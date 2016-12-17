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

TAG_PDF=master
if [ "$TAG_PDF" == "master" ]; then
  SRC_DIR_PDF=asciidoctor-pdf-master
else
  SRC_DIR_PDF=asciidoctor-pdf-${TAG_PDF#v}
fi
rm -rf build/maven-pdf && mkdir -p build/maven-pdf && cd build/maven-pdf
wget --quiet -O $SRC_DIR_PDF.zip https://github.com/asciidoctor/asciidoctor-pdf/archive/$TAG_PDF.zip
unzip -q $SRC_DIR_PDF.zip
cp ../../asciidoctor-pdf-gem-installer.pom $SRC_DIR_PDF/pom.xml
cd $SRC_DIR_PDF
ASCIIDOCTOR_PDF_VERSION=`grep 'VERSION' ./lib/asciidoctor-pdf/version.rb | sed "s/.*'\(.*\)'.*/\1/"`
sed "s;<version></version>;<version>$ASCIIDOCTOR_PDF_VERSION</version>;" pom.xml > pom.xml.sedtmp && mv -f pom.xml.sedtmp pom.xml
sed "s;^ *s\.files *.*$;s.files = Dir['*.gemspec', '*.adoc', '{bin,data,lib}/*', '{bin,data,lib}/**/*'];" asciidoctor-pdf.gemspec > asciidoctor-pdf.gemspec.sedtmp && mv -f asciidoctor-pdf.gemspec.sedtmp asciidoctor-pdf.gemspec
mvn install -Dgemspec=asciidoctor-pdf.gemspec
cd ../..
#rm -rf build/maven-pdf
cd ..

$GRADLE_CMD -S -Pskip.signing -PasciidoctorGemVersion=$ASCIIDOCTOR_VERSION -PasciidoctorPdfGemVersion=$ASCIIDOCTOR_PDF_VERSION -PuseMavenLocal=true :asciidoctorj:clean :asciidoctorj-pdf:clean :asciidoctorj:check :asciidoctorj-pdf:check
exit $?
