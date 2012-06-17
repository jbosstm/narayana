#! /bin/sh

function fatal {
	echo "$1"
	exit -1
}

#export NARAYANA_TESTS=1
[ $NARAYANA_TESTS ] || NARAYANA_TESTS=1
echo "NARAYANA_TESTS=$NARAYANA_TESTS"
echo "NARAYANA_ARGS=$NARAYANA_ARGS"
[ $NARAYANA_TESTS = 1 ] && NARAYANA_ARGS="xx" || NARAYANA_ARGS="-DskipTests"
echo "NARAYANA_ARGS=$NARAYANA_ARGS"

exit 0

echo "? = $?"
[ $? = 0 ] || fatal "wrong code"

isIdlj=0
for arg in "$@"; do
  [ `echo "$arg" |grep "idlj"` ] && isIdlj=1
done

echo "isIdlj=$isIdlj"

SKIP_NARAYANA_TESTS=1
[ "x$SKIP_NARAYANA_TESTS" = "x" ] && echo yes || echo no

SKIP_INTEROP=1
[ $SKIP_INTEROP ] || SKIP_INTEROP=0
if [ $SKIP_INTEROP = 1 ]; then
  echo skipping
else
  echo not skipping
fi

#BUILD NARAYANA WITH FINDBUGS
function build_narayana {
  [ $NARAYANA_TESTS = 1 ] || NARAYANA_ARGS="-DskipTests" && NARAYANA_ARGS=
  echo "NARAYANA_ARGS=$NARAYANA_ARGS"
  [ $? = 0 ] || fatal "narayana build failed"
}

NARAYANA_TESTS=0
[ $NARAYANA_TESTS ] || NARAYANA_TESTS=1

NARAYANA_BUILD=0
[ $NARAYANA_BUILD ] || NARAYANA_BUILD=1
[ $NARAYANA_BUILD = 1 ] && build_narayana


