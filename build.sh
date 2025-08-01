#!/bin/bash -e
### ====================================================================== ###
##                                                                          ##
##  This is the main entry point for the build system.                      ##
##                                                                          ##
##  Users should execute this file rather than 'mvn' to ensure              ##
##  the correct version is being used with the correct configuration.       ##
##                                                                          ##
### ====================================================================== ###
BASH_INTERPRETER=${BASH_INTERPRETER:-${SHELL}}

PROGNAME=`basename $0`
DIRNAME=`dirname $0`
GREP="grep"
ROOT="/"

# Ignore user's MAVEN_HOME if it is set
M2_HOME=""
MAVEN_HOME=""

#  Default arguments
#MVN_OPTIONS="-B -s \"${DIRNAME}/.mvn/wrapper/settings.xml\""
MVN_OPTIONS=""

#  Use the maximum available, or set MAX_FD != -1 to use that
MAX_FD="maximum"

#  OS specific support (must be 'true' or 'false').
cygwin=false;
darwin=false;
case "`uname`" in
    CYGWIN*)
        cygwin=true
        ;;

    Darwin*)
        darwin=true
        ;;
esac

#
#  Helper to complain.
#
die() {
    echo "${PROGNAME}: $*"
    exit 1
}

#
#  Helper to complain.
#
warn() {
    echo "${PROGNAME}: $*"
}

#
#  Helper to source a file if it exists.
#
source_if_exists() {
    for file in $*; do
        if [ -f "$file" ]; then
            . $file
        fi
    done
}

#
#  Main function.
#
main() {
    #  If there is a build config file, source it.
    source_if_exists "$DIRNAME/build.conf" "$HOME/.build.conf"

    #  Increase the maximum file descriptors if we can.
    if [ $cygwin = "false" ]; then
        MVN_OPTIONS="$MVN_OPTIONS -Dorson.jar.location=`pwd`/ext/"
        MAX_FD_LIMIT=`ulimit -H -n`
        if [ $? -eq 0 ]; then
            if [ "$MAX_FD" = "maximum" -o "$MAX_FD" = "max" ]; then
                #  Use system's max.
                MAX_FD="$MAX_FD_LIMIT"
            fi

            ulimit -n $MAX_FD
            if [ $? -ne 0 ]; then
                warn "Could not set maximum file descriptor limit: $MAX_FD"
            fi
        else
            warn "Could not query system maximum file descriptor limit: $MAX_FD_LIMIT"
        fi
    else
        MVN_OPTIONS="$MVN_OPTIONS -Dorson.jar.location=`cygpath -w $(pwd)/ext/`"
    fi

    #  Make sure we have one.
    MVN="${DIRNAME}/mvnw"
    if [ ! -x "$MVN" ]; then
        die "Maven binary is not executable: $MVN"
    fi

    #  Need to specify planet57/buildmagic protocol handler package.
    MVN_OPTS="-Djava.protocol.handler.pkgs=org.jboss.net.protocol"

    #  Setup some build properties
    MVN_OPTS="$MVN_OPTS -Dbuild.script=$0"

    #  Change to the directory where the script lives, so users are not forced
    #  to be in the same directory as build.xml.
    cd $DIRNAME

    MVN_GOAL="";
    ADDIT_PARAMS="";
    #  For each parameter, check for testsuite directives.
    for param in "$@" ; do
        case $param in
            -*)      ADDIT_PARAMS="$ADDIT_PARAMS '$param'";;
            clean)   MVN_GOAL="$MVN_GOAL$param ";;
            test)    MVN_GOAL="$MVN_GOAL$param ";;
            install) MVN_GOAL="$MVN_GOAL$param ";;
            deploy)  MVN_GOAL="$MVN_GOAL$param ";;
            site)    MVN_GOAL="$MVN_GOAL$param ";;
            *)       ADDIT_PARAMS="$ADDIT_PARAMS '$param'";;
        esac
    done
    #  Default goal if none specified.
    if [ -z "$MVN_GOAL" ]; then MVN_GOAL="install"; fi

    #  Export some stuff for maven.
    export MVN MVN_OPTS MVN_GOAL

    echo "${BASH_INTERPRETER} $MVN $MVN_OPTIONS $MVN_GOAL $ADDIT_PARAMS"

    #  Execute in debug mode, or simply execute.
    if [ "x$MVN_DEBUG" != "x" ]; then
        eval "${BASH_INTERPRETER}" -x $MVN $MVN_OPTIONS $MVN_GOAL $ADDIT_PARAMS
    else
        eval exec ${BASH_INTERPRETER} $MVN $MVN_OPTIONS $MVN_GOAL $ADDIT_PARAMS
    fi
}

##
##  Bootstrap
##
main "$@"
