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

$GF_HOME/bin/wsimport -verbose -keep -wsdllocation wsdl/sc007.wsdl -d tmp/classes -s tmp/src -target 2.0 -p com.jboss.transaction.wstf.webservices.sc007.generated wsdl/sc007.wsdl
