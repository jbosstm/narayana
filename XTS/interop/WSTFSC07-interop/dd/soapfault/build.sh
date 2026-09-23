#
#
#
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

# $GF_HOME/bin/wsimport -verbose -keep -wsdllocation wsdl/soapfault.wsdl -d tmp/classes -s tmp/src -target 2.1 -p org.jboss.jbossts.xts.soapfault wsdl/soapfault.wsdl
$JBOSS_HOME/bin/wsconsume.sh -v -k -w wsdl/soapfault.wsdl -o tmp/classes -s tmp/src wsdl/soapfault.wsdl
