#! /bin/sh
#
# JBoss, Home of Professional Open Source
# Copyright 2006, Red Hat Middleware LLC, and individual contributors
# as indicated by the @author tags. 
# See the copyright.txt in the distribution for a full listing 
# of individual contributors.
# This copyrighted material is made available to anyone wishing to use,
# modify, copy, or redistribute it subject to the terms and conditions
# of the GNU Lesser General Public License, v. 2.1.
# This program is distributed in the hope that it will be useful, but WITHOUT A
# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
# PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
# You should have received a copy of the GNU Lesser General Public License,
# v.2.1 along with this distribution; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
# MA  02110-1301, USA.

# 
# (C) 2005-2006,
# @author JBoss Inc.
#

#
# Skeleton sh script suitable for starting and stopping 
# wrapped Java apps on *nix platforms. 
#
# Make sure that PIDFILE points to the correct location,
# if you have changed the default location set in the 
# wrapper configuration file.
#

#-----------------------------------------------------------------------------
# These settings can be modified to fit the needs of your application

# Path to java executable
JAVA_HOME=/usr/local/jdk1.6.0.16
export JAVA_HOME
if [ "a$JAVA_HOME" != a ]; then
    PATH=$PATH:$JAVA_HOME/bin
    export PATH
fi

# Application
APP_NAME="jbossts"
APP_LONG_NAME="JBoss Transaction Service - Transaction Service"

# Wrapper
WRAPPER_CMD="./wrapper"
WRAPPER_CONF="../../config/transactionservice.conf"

# Priority at which to run the wrapper.  See "man nice" for valid priorities.
#  nice is only used if a priority is specified.
PRIORITY=

# Location of the pid file.
PIDDIR="./"

# Do not modify anything beyond this point
#-----------------------------------------------------------------------------

# Get the fully qualified path to the script
case $0 in
    /*)
        SCRIPT="$0"
        ;;
    *)
        PWD=`pwd`
        SCRIPT="$PWD/$0"
        ;;
esac

# Change spaces to ":" so the tokens can be parsed.
SCRIPT=`echo $SCRIPT | sed -e 's; ;:;g'`
# Get the real path to this script, resolving any symbolic links
TOKENS=`echo $SCRIPT | sed -e 's;/; ;g'`
REALPATH=
for C in $TOKENS; do
    REALPATH="$REALPATH/$C"
    while [ -h "$REALPATH" ] ; do
        LS="`ls -ld "$REALPATH"`"
        LINK="`expr "$LS" : '.*-> \(.*\)$'`"
        if expr "$LINK" : '/.*' > /dev/null; then
            REALPATH="$LINK"
        else
            REALPATH="`dirname "$REALPATH"`""/$LINK"
        fi
    done
done
# Change ":" chars back to spaces.
REALPATH=`echo $REALPATH | sed -e 's;:; ;g'`

# Change the current directory to the location of the script
cd "`dirname "$REALPATH"`"

# Process ID
PIDFILE="$PIDDIR/$APP_NAME.pid"
pid=""

# Resolve the location of the 'ps' command
PSEXE="/usr/bin/ps"
if [ ! -x $PSEXE ]
then
    PSEXE="/bin/ps"
    if [ ! -x $PSEXE ]
    then
        echo "Unable to locate 'ps'."
        echo "Please report this with the location on your system."
        exit 1
    fi
fi

# Build the nice clause
if [ "X$PRIORITY" = "X" ]
then
    CMDNICE=""
else
    CMDNICE="nice -$PRIORITY"
fi

getpid() {
    if [ -f $PIDFILE ]
    then
        if [ -r $PIDFILE ]
        then
            pid=`cat $PIDFILE`
            if [ "X$pid" != "X" ]
            then
                # Verify that a process with this pid is still running.
                pid=`$PSEXE -p $pid | grep $pid | grep -v grep | awk '{print $1}' | tail -1`
                if [ "X$pid" = "X" ]
                then
                    # This is a stale pid file.
                    rm -f $PIDFILE
                    echo "Removed stale pid file: $PIDFILE"
                fi
            fi
        else
            echo "Cannot read $PIDFILE."
            exit 1
        fi
    fi
}

testpid() {
    pid=`$PSEXE -p $pid | grep $pid | grep -v grep | awk '{print $1}' | tail -1`
    if [ "X$pid" = "X" ]
    then
        # Process is gone so remove the pid file.
        rm -f $PIDFILE
    fi
}

console() {
    echo "Running $APP_LONG_NAME..."
    getpid
    if [ "X$pid" = "X" ]
    then
        exec $CMDNICE $WRAPPER_CMD $WRAPPER_CONF wrapper.pidfile=$PIDFILE wrapper.debug=true wrapper.java.additional.1="-DFOO=This is a test"
    else
        echo "$APP_LONG_NAME is already running."
        exit 1
    fi
}
 
start() {
    echo "Starting $APP_LONG_NAME..."
    getpid
    if [ "X$pid" = "X" ]
    then
        exec $CMDNICE $WRAPPER_CMD $WRAPPER_CONF wrapper.pidfile=$PIDFILE wrapper.daemonize=TRUE
    else
        echo "$APP_LONG_NAME is already running."
        exit 1
    fi
}
 
stopit() {
    echo "Stopping $APP_LONG_NAME..."
    getpid
    if [ "X$pid" = "X" ]
    then
        echo "$APP_LONG_NAME was not running."
    else
         # Running so try to stop it.
        kill $pid
        if [ $? -ne 0 ]
        then
            # An explanation for the failure should have been given
            echo "Unable to stop $APP_LONG_NAME."
            exit 1
        fi

        # We can not predict how long it will take for the wrapper to
        #  actually stop as it depends on settings in wrapper.conf.
        #  Loop until it does.
        savepid=$pid
        CNT=0
        TOTCNT=0
        while [ "X$pid" != "X" ]
        do
            # Loop for up to 5 minutes
            if [ "$TOTCNT" -lt "300" ]
            then
                if [ "$CNT" -lt "5" ]
                then
                    CNT=`expr $CNT + 1`
                else
                    echo "Waiting for $APP_LONG_NAME to exit..."
                    CNT=0
                fi
                TOTCNT=`expr $TOTCNT + 1`

                sleep 1

                testpid
            else
                pid=
            fi
        done

        pid=$savepid
        testpid
        if [ "X$pid" != "X" ]
        then
            echo "Timed out waiting for $APP_LONG_NAME to exit."
            echo "  Attempting a forced exit..."
            kill -9 $pid
        fi

        pid=$savepid
        testpid
        if [ "X$pid" != "X" ]
        then
            echo "Failed to stop $APP_LONG_NAME."
            exit 1
        else
            echo "Stopped $APP_LONG_NAME."
        fi
    fi
}

dump() {
    echo "Dumping $APP_LONG_NAME..."
    getpid
    if [ "X$pid" = "X" ]
    then
        echo "$APP_LONG_NAME was not running."

    else
        kill -3 $pid

        if [ $? -ne 0 ]
        then
            echo "Failed to dump $APP_LONG_NAME."
            exit 1
        else
            echo "Dumped $APP_LONG_NAME."
        fi
    fi
}

case "$1" in

    'console')
        console
        ;;

    'start')
        start
        ;;

    'stop')
        stopit
        ;;

    'restart')
        stopit
        start
        ;;

    'dump')
        dump
        ;;

    *)
        echo "Usage: $0 { console | start | stop | restart | dump }"
        exit 1
        ;;
esac

exit 0
