#
# SPDX short identifier: Apache-2.0
#

if [ ! -d tmp ] ; then
    mkdir tmp
fi

if [ ! -d tmp/classes ] ; then
    mkdir tmp/classes
fi

if [ ! -d tmp/src ] ; then
    mkdir tmp/src
fi

# delete old generated versions -- any changes should be made to a copy!

rm -rf tmp/classes/* tmp/src/*

# $JBOSS_HOME/bin/wsconsume.sh -v -k -p org.jboss.jbossts.xts.servicetests -w wsdl/xtsservicetests.wsdl -o tmp/classes -s tmp/src wsdl/xtsservicetests.wsdl

# $JBOSS_HOME/bin/wsconsume.sh -v -k -w wsdl/xtsservicetests.wsdl -o tmp/classes -s tmp/src wsdl/xtsservicetests.wsdl

# $GF_HOME/bin/wsimport -verbose -keep -wsdllocation wsdl/xtsservicetests.wsdl -d tmp/classes -s tmp/src -target 2.1 wsdl/xtsservicetests.wsdl

$GF_HOME/bin/wsimport -verbose -keep -target 2.0 -wsdllocation wsdl/xtsservicetests.wsdl -d tmp/classes -s tmp/src wsdl/xtsservicetests.wsdl
