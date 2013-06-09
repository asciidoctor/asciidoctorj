#!/bin/sh

PROGNAME=`basename "$0"`

warn ( ) {
    echo "${PROGNAME}: $*"
}

die ( ) {
    warn "$*"
    exit 1
}

if [ -z "$JAVA_HOME" ] ; then

	die "JAVA_HOME is not be set, please set JAVA_HOME."

fi

# Determine the Java command to use to start the JVM.
if [ -z "$JAVACMD" ] ; then
    if [ -n "$JAVA_HOME" ] ; then
        if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
            # IBM's JDK on AIX uses strange locations for the executables
            JAVACMD="$JAVA_HOME/jre/sh/java"
        else
            JAVACMD="$JAVA_HOME/bin/java"
        fi
    else
        JAVACMD="java"
    fi
fi
if [ ! -x "$JAVACMD" ] ; then
    die "JAVA_HOME is not defined correctly, can not execute: $JAVACMD"
fi

exec "$JAVACMD" $JAVA_OPTS \
            -jar *.jar \
            "$@"
