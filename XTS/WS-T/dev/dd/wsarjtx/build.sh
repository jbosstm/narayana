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

# $JBOSS_HOME/bin/wsconsume.sh -v -k -w /wsdl/wsarjtx.wsdl -o tmp/classes -s tmp/src -k wsdl/wsarjtx.wsdl

# $JBOSS_HOME/bin/wsconsume.sh -v -k -w /home/adinn/tmp/wsdl/wsarjtx.wsdl -o tmp/classes -s tmp/src -k wsdl/wsarjtx.wsdl

# generate termination participant and coordinator code

# $GF_HOME/bin/wsimport -verbose -keep -wsdllocation wsdl/wsarjtx-termination-participant-binding.wsdl -d tmp/classes -s tmp/src -target 2.1 wsdl/wsarjtx-termination-participant-binding.wsdl

# $GF_HOME/bin/wsimport -verbose -keep -wsdllocation wsdl/wsarjtx-termination-coordinator-binding.wsdl -d tmp/classes -s tmp/src -target 2.1 wsdl/wsarjtx-termination-coordinator-binding.wsdl

$JBOSS_HOME/bin/wsconsume.sh -v -k -w wsdl/wsarjtx-termination-coordinator-binding.wsdl -o tmp/classes -s tmp/src -k wsdl/wsarjtx-termination-coordinator-binding.wsdl

$JBOSS_HOME/bin/wsconsume.sh -v -k -w wsdl/wsarjtx-termination-participant-binding.wsdl -o tmp/classes -s tmp/src -k wsdl/wsarjtx-termination-participant-binding.wsdl

$JBOSS_HOME/bin/wsconsume.sh -v -k -w wsdl/wsarjtx-termination-coordinator-rpc-binding.wsdl -o tmp/classes -s tmp/src -k wsdl/wsarjtx-termination-coordinator-rpc-binding.wsdl

# we need to patch the resource lookup used to provide a jar base URL
# for the wsdl location supplied above wsimport generates
# e.g. 'qualified.path.ClassName.class.getResource(".")' to locate a
# URL for the jar dir containing ClassName.class. But the EJB class
# loader returns null for this (even though it returns true for
# hasEntry!). If we patch the generated code to specify
# 'qualified.path.ClassName.class.getResource("")' it works ok.

sed -i -e 's/TerminationCoordinatorService.class.getResource(".")/TerminationCoordinatorService.class.getResource("")/' tmp/src/com/arjuna/schemas/ws/_2005/_10/wsarjtx/TerminationCoordinatorService.java
sed -i -e 's/TerminationParticipantService.class.getResource(".")/TerminationParticipantService.class.getResource("")/' tmp/src/com/arjuna/schemas/ws/_2005/_10/wsarjtx/TerminationParticipantService.java
sed -i -e 's/TerminationCoordinatorRPCService.class.getResource(".")/TerminationCoordinatorRPCService.class.getResource("")/' tmp/src/com/arjuna/schemas/ws/_2005/_10/wsarjtx/TerminationCoordinatorRPCService.java
