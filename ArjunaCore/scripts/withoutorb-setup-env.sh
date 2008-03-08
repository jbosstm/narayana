#
# JBoss, Home of Professional Open Source
# Copyright 2006, Red Hat Middleware LLC, and individual contributors
# as indicated by the @author tags.
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
# Arjuna Technologies Ltd.
# Copyright 2004
#

CONTINUE_SETUP=true

if test "x$JAVA_HOME" = "x"
then

	echo Please ensure the JAVA_HOME environment variable is set
	CONTINUE_SETUP=false

else

	if test "x$@HOME_DIRECTORY@" = "x"
	then

		echo Please ensure the @HOME_DIRECTORY@ environment variable is set
		CONTINUE_SETUP=false

	fi

fi

if test "$CONTINUE_SETUP" = "true"
then

# Find classpath separator

CPS=":"

case `uname -a` in
    CYGWIN_* | Windows* )
        CPS=";"
	@HOME_DIRECTORY@=`echo $@HOME_DIRECTORY@ | sed -e 's;\\\;/;g'`
    ;;
esac


# Setup EXT classpath

echo Setting up environment

PRODUCT_CLASSPATH="$@HOME_DIRECTORY@/lib/@PRODUCT_NAME@.jar"
PRODUCT_CLASSPATH="$PRODUCT_CLASSPATH$CPS$@HOME_DIRECTORY@/bin/tsmx-tools.jar"
PRODUCT_CLASSPATH="$PRODUCT_CLASSPATH$CPS$@HOME_DIRECTORY@/etc/"

EXT_CLASSPATH="$@HOME_DIRECTORY@/lib/ext/jbossts-common.jar"
EXT_CLASSPATH="$EXT_CLASSPATH$CPS$@HOME_DIRECTORY@/lib/ext/commons-logging-1.1.jar"
EXT_CLASSPATH="$EXT_CLASSPATH$CPS$@HOME_DIRECTORY@/lib/ext/concurrent.jar"
EXT_CLASSPATH="$EXT_CLASSPATH$CPS$@HOME_DIRECTORY@/lib/ext/connector-api.jar"
EXT_CLASSPATH="$EXT_CLASSPATH$CPS$@HOME_DIRECTORY@/lib/ext/jdbc2_0-stdext.jar"
EXT_CLASSPATH="$EXT_CLASSPATH$CPS$@HOME_DIRECTORY@/lib/ext/jmxri.jar"
EXT_CLASSPATH="$EXT_CLASSPATH$CPS$@HOME_DIRECTORY@/lib/ext/jndi.jar"
EXT_CLASSPATH="$EXT_CLASSPATH$CPS$@HOME_DIRECTORY@/lib/ext/jta-1_1-classes.zip"
EXT_CLASSPATH="$EXT_CLASSPATH$CPS$@HOME_DIRECTORY@/lib/ext/log4j-1.2.8.jar"
EXT_CLASSPATH="$EXT_CLASSPATH$CPS$@HOME_DIRECTORY@/lib/ext/xercesImpl.jar"
EXT_CLASSPATH="$EXT_CLASSPATH$CPS$@HOME_DIRECTORY@/lib/ext/xmlParserAPIs.jar"

CLASSPATH=".$CPS$PRODUCT_CLASSPATH$CPS$EXT_CLASSPATH"
export CLASSPATH

fi
