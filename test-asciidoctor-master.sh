#!/bin/bash

# Test against released version
#mvn test

# Test against unreleased version
wget -O asciidoctor-master.zip https://github.com/asciidoctor/asciidoctor/archive/master.zip
unzip asciidoctor-master.zip
cp install-asciidoctor-gem.pom asciidoctor-master/pom.xml
cd asciidoctor-master
ASCIIDOCTOR_VERSION=`grep 's\.version' asciidoctor.gemspec | sed "s/.*'\(.*\)'.*/\1/"`
sed -i "s;<version></version>;<version>$ASCIIDOCTOR_VERSION</version>;" pom.xml
mvn install
cd ..
rm -rf asciidoctor-master*
mvn test -Dasciidoctor.version=$ASCIIDOCTOR_VERSION
