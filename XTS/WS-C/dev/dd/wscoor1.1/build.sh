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

# $JBOSS_HOME/bin/wsconsume.sh -v -k -w /wsdl/wscoor.wsdl -o tmp/classes -s tmp/src -k wsdl/wscoor-binding.wsdl
# $JBOSS_HOME/bin/wsconsume.sh -v -k -w /home/adinn/tmp/wsdl/wscoor.wsdl -o tmp/classes -s tmp/src -k wsdl/wscoor-binding.wsdl

# $GF_HOME/bin/wsimport -verbose -keep -wsdllocation /home/adinn/tmp/wsdl/wscoor.wsdl -d tmp/classes -s tmp/src wsdl/wscoor-binding.wsdl

$GF_HOME/bin/wsimport -verbose -keep -wsdllocation wsdl/wscoor-activation-binding.wsdl -d tmp/classes -s tmp/src -target 2.1 wsdl/wscoor-activation-binding.wsdl

$GF_HOME/bin/wsimport -verbose -keep -wsdllocation wsdl/wscoor-registration-binding.wsdl -d tmp/classes -s tmp/src -target 2.1 wsdl/wscoor-registration-binding.wsdl

# $JBOSS_HOME/bin/wsconsume.sh -v -k -w wsdl/wscoor-activation-binding.wsdl -o tmp/classes -s tmp/src -t 2.0 wsdl/wscoor-activation-binding.wsdl

# $JBOSS_HOME/bin/wsconsume.sh -v -k -w wsdl/wscoor-registration-binding.wsdl -o tmp/classes -s tmp/src -t 2.0 wsdl/wscoor-registration-binding.wsdl

# we need to patch the resource lookup used to provide a jar base URL
# for the wsdl location supplied above wsimport generates
# e.g. 'qualified.path.ClassName.class.getResource(".")' to locate a
# URL for the jar dir containing ClassName.class. But the EJB class
# loader returns null for this (even though it returns true for
# hasEntry!). If we patch the generated code to specify
# 'qualified.path.ClassName.class.getResource("")' it works ok.

sed -i -e 's/ActivationService.class.getResource(".")/ActivationService.class.getResource("")/' tmp/src/org/oasis_open/docs/ws_tx/wscoor/_2006/_06/ActivationService.java
sed -i -e 's/RegistrationService.class.getResource(".")/RegistrationService.class.getResource("")/' tmp/src/org/oasis_open/docs/ws_tx/wscoor/_2006/_06/RegistrationService.java
