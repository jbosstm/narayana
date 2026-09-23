#!/bin/bash
# generate jaxws code from wsdl

GLASSFISHDIR=/home/adinn/glassfish/v2-b58g/glassfish/
WSIMPORT=$GLASSFISHDIR/bin/wsimport

if [ -d tmp/src ] ; then
  rm -rf tmp/src/*
fi
if [ -d tmp/classes ] ; then
  rm -rf tmp/classes/*
fi
if [ ! -d tmp/src ]; then
  mkdir -p tmp/src
fi
if [ ! -d tmp/classes ] ; then
  mkdir -p tmp/classes
fi

if [ $# -eq 0 ]; then
    files=`ls *.wsdl` 
else
    files="$*"
fi
#for wsdlfile in RestaurantServiceAT.wsdl TaxiServiceAT.wsdl \
#    TheatreServiceAT.wsdl RestaurantServiceBA.wsdl \
#    TaxiServiceBA.wsdl TheatreServiceBA.wsdl
for wsdlfile in $files
do
  $WSIMPORT -s tmp/src -d tmp/classes -target 2.0 -keep -wsdllocation /WEB-INF/wsdl/$wsdlfile $wsdlfile
done
