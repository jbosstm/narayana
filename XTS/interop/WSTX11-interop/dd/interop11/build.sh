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

#$GF_HOME/bin/wsimport -verbose -keep -wsdllocation wsdl/interop11-binding.wsdl -d tmp/classes -s tmp/src -target 2.1 -p com.jboss.transaction.txinterop.webservices.atinterop.generated wsdl/interopat-initiator-binding.wsdl

#$GF_HOME/bin/wsimport -verbose -keep -wsdllocation wsdl/interop11-binding.wsdl -d tmp/classes -s tmp/src -target 2.1 -p com.jboss.transaction.txinterop.webservices.atinterop.generated wsdl/interopat-participant-binding.wsdl

#$GF_HOME/bin/wsimport -verbose -keep -wsdllocation wsdl/interop11-binding.wsdl -d tmp/classes -s tmp/src -target 2.1 -p com.jboss.transaction.txinterop.webservices.bainterop.generated wsdl/interopba-initiator-binding.wsdl

#$GF_HOME/bin/wsimport -verbose -keep -wsdllocation wsdl/interop11-binding.wsdl -d tmp/classes -s tmp/src -target 2.1 -p com.jboss.transaction.txinterop.webservices.bainterop.generated wsdl/interopba-participant-binding.wsdl

$GF_HOME/bin/wsimport -verbose -keep -wsdllocation wsdl/interopat-initiator-binding.wsdl -d tmp/classes -s tmp/src -target 2.0 -p com.jboss.transaction.txinterop.webservices.atinterop.generated wsdl/interopat-initiator-binding.wsdl

$GF_HOME/bin/wsimport -verbose -keep -wsdllocation wsdl/interopat-participant-binding.wsdl -d tmp/classes -s tmp/src -target 2.0 -p com.jboss.transaction.txinterop.webservices.atinterop.generated wsdl/interopat-participant-binding.wsdl

$GF_HOME/bin/wsimport -verbose -keep -wsdllocation wsdl/interopba-initiator-binding.wsdl -d tmp/classes -s tmp/src -target 2.0 -p com.jboss.transaction.txinterop.webservices.bainterop.generated wsdl/interopba-initiator-binding.wsdl

$GF_HOME/bin/wsimport -verbose -keep -wsdllocation wsdl/interopba-participant-binding.wsdl -d tmp/classes -s tmp/src -target 2.0 -p com.jboss.transaction.txinterop.webservices.bainterop.generated wsdl/interopba-participant-binding.wsdl

