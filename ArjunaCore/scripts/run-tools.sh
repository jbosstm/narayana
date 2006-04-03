#!/bin/sh
#
# JBoss, Home of Professional Open Source
# Copyright 2006, JBoss Inc., and others contributors as indicated 
# by the @authors tag. All rights reserved. 
# See the copyright.txt in the distribution for a
# full listing of individual contributors. 
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

if test "x$@HOME_DIRECTORY@" = "x"
then

	echo The environment variable @HOME_DIRECTORY@ is not set
	exit 1

fi

# Find classpath separator

CPS=":"

case `uname -a` in
    CYGWIN_* | Windows* )
        CPS=";"
    ;;
esac

# Setup the environment for the Arjuna Transaction Service
. "$@HOME_DIRECTORY@/bin/setup-env.sh"

# Add ext libraries required for tools
CLASSPATH="$CLASSPATH$CPS$@HOME_DIRECTORY@/bin/tools/ext/jfreechart-0.9.15.jar"
CLASSPATH="$CLASSPATH$CPS$@HOME_DIRECTORY@/bin/tools/ext/jcommon-0.9.0.jar"

export CLASSPATH

"$JAVA_HOME/bin/java" "-Dcom.arjuna.mw.ArjunaToolsFramework.lib=$@HOME_DIRECTORY@/bin/tools" com.arjuna.ats.tools.toolsframework.ArjunaToolsFramework
