#!/bin/bash

# Test against released version
#mvn test

# Test against upstream version
TAG=master
#TAG=v1.5.2
if [ "$TAG" == "master" ]; then
  SRC_DIR=asciidoctor-master
else
  SRC_DIR=asciidoctor-${TAG#v}
fi
mvn clean
mkdir -p target && cd target
rm -rf $SRC_DIR*
wget --quiet -O $SRC_DIR.zip https://github.com/asciidoctor/asciidoctor/archive/$TAG.zip
unzip -q $SRC_DIR.zip
cp ../asciidoctor-gem-installer.pom $SRC_DIR/pom.xml
cd $SRC_DIR
ASCIIDOCTOR_VERSION=`grep 'VERSION' ./lib/asciidoctor/version.rb | sed "s/.*'\(.*\)'.*/\1/"`
sed -i "s;<version></version>;<version>$ASCIIDOCTOR_VERSION</version>;" pom.xml
sed -i "s;^ *s\.files \+.*$;s.files = Dir['*.gemspec', '*.adoc', '{bin,data,lib}/*', '{bin,data,lib}/**/*'];" asciidoctor.gemspec
mvn install -Dgemspec=asciidoctor.gemspec
cd ../..
mvn test -Dasciidoctor.version=$ASCIIDOCTOR_VERSION -DforkMode=always
exit $?
