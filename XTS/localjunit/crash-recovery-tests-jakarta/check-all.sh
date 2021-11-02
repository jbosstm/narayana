#!/bin/bash

DIR=$1

if [ "$DIR" == "" ]; then
	echo "usage: $0 <script location>"
	exit 0
fi

SCRIPTS=$(find $DIR -name \*.txt)

JARS=$(cd ../../..; find . -name \*.jar)

for i in $JARS; do
	CP="$CP:../../../$i";
done

CP="$CP:../../sar/tests/target/classes"

for s in $SCRIPTS; do
	echo $s
	bmcheck.sh -cp $CP $s
done
