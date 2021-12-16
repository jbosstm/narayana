#!/bin/sh
### ====================================================================== ###
##                                                                          ##
##  This is the main entry point for the build system.                      ##
##                                                                          ##
##  Users should execute this file rather than 'mvn' to ensure              ##
##  the correct version is being used with the correct configuration.       ##
##                                                                          ##
### ====================================================================== ###

# $Id: build.sh 105735 2010-06-04 19:45:13Z pgier $

# Create the bpa if you can
BPA=
uname | grep Linux >> /dev/null
if [ "$?" -ne "1" ]; then
	uname -a | grep x86_64 >> /dev/null
	if [ "$?" -ne "1" ]; then
		BPA="-Dbpa=centos54x64"
	else
		BPA="-Dbpa=centos55x32"
	fi
  # This is required for the upgrade of g++ https://issues.jboss.org/browse/JBTM-1787
  if [ -f /etc/fedora-release ]
  then
	uname -a | grep x86_64 >> /dev/null
	if [ "$?" -ne "1" ]; then
	    BPA="-Dbpa=fc18x64"
	fi
  fi
fi

ORIG_WORKING_DIR=`pwd`
PROGNAME=`basename $0`
DIRNAME=`dirname $0`
GREP="grep"
ROOT="/"

# Ignore user's MAVEN_HOME if it is set (M2_HOME is unsupported since Apache Maven 3.5.0)
unset M2_HOME
unset MAVEN_HOME

JAVA_VERSION=$(java -version 2>&1 | grep "\(java\|openjdk\) version" | cut -d\  -f3 | tr -d '"' | tr -d '[:space:]'| awk -F . '{if ($1==1) print $2; else print $1}')

if [ $JAVA_VERSION -eq "9" ]; then
  MAVEN_OPTS="$MAVEN_OPTS --add-modules java.corba"
  MAVEN_OPTS="$MAVEN_OPTS --add-modules java.xml.bind"
  MAVEN_OPTS="$MAVEN_OPTS --add-modules java.xml.ws"
  export MAVEN_OPTS
fi

if [ -z "$MAVEN_OPTS" ]
then
	if [ $JAVA_VERSION -ge "9" ]; then
		MAVEN_OPTS="$MAVEN_OPTS -Xms1303m -Xmx1303m"
	else
    MAVEN_OPTS="$MAVEN_OPTS -Xms1303m -Xmx1303m -XX:MaxMetaspaceSize=512m"
	fi
	export MAVEN_OPTS
fi

#  Default arguments
MVN_OPTIONS="-B -s \"${DIRNAME}/.mvn/wrapper/settings.xml\" $BPA"

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

    echo "$MVN $MVN_OPTIONS $MVN_GOAL $ADDIT_PARAMS"

    # workaround in case 'mvn -f' is not supported
    if [ "$PRESERVE_WORKING_DIR" = "true" ]; then cd "$ORIG_WORKING_DIR"; fi

    #  Execute in debug mode, or simply execute.
    if [ "x$MVN_DEBUG" != "x" ]; then
        eval /bin/sh -x $MVN $MVN_OPTIONS $MVN_GOAL $ADDIT_PARAMS
    else
        eval exec $MVN $MVN_OPTIONS $MVN_GOAL $ADDIT_PARAMS
    fi
}

##
##  Bootstrap
##
main "$@"
