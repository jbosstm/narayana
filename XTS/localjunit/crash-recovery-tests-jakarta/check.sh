#!/bin/bash

SCRIPT=$1

if [ "$SCRIPT" == "" ]; then
	echo "usage: $0 <script location>"
	exit 0
fi


JARS=$(cd ../../..; find -name \*.jar)

for i in $JARS; do
	CP="$CP:../../../$i";
done

bmcheck.sh -cp $CP $SCRIPT
