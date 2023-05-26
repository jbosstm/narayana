#
# SPDX short identifier: Apache-2.0
#

CONTINUE_SETUP=true

if test "x$JAVA_HOME" = "x"
then

	echo Please ensure the JAVA_HOME environment variable is set
	CONTINUE_SETUP=false

else

	if test "x$NARAYANA_HOME" = "x"
	then

		echo Please ensure the NARAYANA_HOME environment variable is set
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
	NARAYANA_HOME=`echo $NARAYANA_HOME | sed -e 's;\\\;/;g'`
    ;;
esac

# Setup EXT classpath

echo Setting up environment

PRODUCT_CLASSPATH="$NARAYANA_HOME/lib/jts/narayana-jts.jar"
PRODUCT_CLASSPATH="$PRODUCT_CLASSPATH$CPS$NARAYANA_HOME/etc/"


for i in $NARAYANA_HOME/lib/ext/*.jar
do
EXT_CLASSPATH="$EXT_CLASSPATH$CPS$i"
done

CLASSPATH=".$CPS$PRODUCT_CLASSPATH$CPS$EXT_CLASSPATH"

export CLASSPATH=$CLASSPATH:$NARAYANA_HOME/lib/jts/narayana-jts-idlj.jar

fi
