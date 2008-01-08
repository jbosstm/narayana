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
#
# Arjuna Transaction Service (now JBoss Transaction Service)
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

	if test "x$JBOSSTS_HOME" = "x"
	then

		echo Please ensure the JBOSSTS_HOME environment variable is set
		CONTINUE_SETUP=false

	fi

fi

if test "$CONTINUE_SETUP" = "true"
then

# Full JacORB installation location
# Caution: JBossTS needs a specially patched version of JacORB.
# Use $JBOSSTS_HOME/jacorb here unless you have a good reason not to.
JACORB_HOME="PUT_JACORB_HOME_HERE"

# Find classpath separator

CPS=":"

case `uname -a` in
    CYGWIN_* | Windows* )
        CPS=";"
	JACORB_HOME=`echo $JACORB_HOME | sed -e 's;\\\;/;g'`
	JBOSSTS_HOME=`echo $JBOSSTS_HOME | sed -e 's;\\\;/;g'`
    ;;
esac


# Setup EXT classpath

echo Setting up environment

PRODUCT_CLASSPATH="$JBOSSTS_HOME/lib/@PRODUCT_NAME@.jar"
PRODUCT_CLASSPATH="$PRODUCT_CLASSPATH$CPS$JBOSSTS_HOME/lib/@PRODUCT_NAME@-jacorb.jar"
PRODUCT_CLASSPATH="$PRODUCT_CLASSPATH$CPS$JBOSSTS_HOME/bin/tsmx-tools.jar"
PRODUCT_CLASSPATH="$PRODUCT_CLASSPATH$CPS$JBOSSTS_HOME/etc/"

EXT_CLASSPATH="$JBOSSTS_HOME/lib/ext/jbossts-common.jar"
EXT_CLASSPATH="$EXT_CLASSPATH$CPS$JBOSSTS_HOME/lib/ext/commons-logging.jar"
EXT_CLASSPATH="$EXT_CLASSPATH$CPS$JBOSSTS_HOME/lib/ext/concurrent.jar"
EXT_CLASSPATH="$EXT_CLASSPATH$CPS$JBOSSTS_HOME/lib/ext/connector-api.jar"
EXT_CLASSPATH="$EXT_CLASSPATH$CPS$JBOSSTS_HOME/lib/ext/jdbc2_0-stdext.jar"
EXT_CLASSPATH="$EXT_CLASSPATH$CPS$JBOSSTS_HOME/lib/ext/jmxri.jar"
EXT_CLASSPATH="$EXT_CLASSPATH$CPS$JBOSSTS_HOME/lib/ext/jndi.jar"
EXT_CLASSPATH="$EXT_CLASSPATH$CPS$JBOSSTS_HOME/lib/ext/jta-1_1-classes.zip"
EXT_CLASSPATH="$EXT_CLASSPATH$CPS$JBOSSTS_HOME/lib/ext/log4j-1.2.8.jar"
EXT_CLASSPATH="$EXT_CLASSPATH$CPS$JBOSSTS_HOME/lib/ext/xercesImpl.jar"
EXT_CLASSPATH="$EXT_CLASSPATH$CPS$JBOSSTS_HOME/lib/ext/xmlParserAPIs.jar"

JACORB_CLASSPATH="$JACORB_HOME/lib/jacorb.jar"
JACORB_CLASSPATH="$JACORB_CLASSPATH$CPS$JACORB_HOME/lib/idl.jar"
JACORB_CLASSPATH="$JACORB_CLASSPATH$CPS$JACORB_HOME/lib/logkit-1.2.jar"
JACORB_CLASSPATH="$JACORB_CLASSPATH$CPS$JACORB_HOME/lib/avalon-framework-4.1.5.jar"
JACORB_CLASSPATH="$JACORB_CLASSPATH$CPS$JACORB_HOME/etc"

CLASSPATH=".$CPS$PRODUCT_CLASSPATH$CPS$EXT_CLASSPATH$CPS$JACORB_CLASSPATH"
export CLASSPATH

fi
