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

# $JBOSS_HOME/bin/wsconsume.sh -v -k -w /wsdl/wsat.wsdl -o tmp/classes -s tmp/src -k wsdl/wsat-binding.wsdl

# $JBOSS_HOME/bin/wsconsume.sh -v -k -w /wsdl/wsba.wsdl -o tmp/classes -s tmp/src -k wsdl/wsba-binding.wsdl

# $JBOSS_HOME/bin/wsconsume.sh -v -k -w /home/adinn/tmp/wsdl/wsat.wsdl -o tmp/classes -s tmp/src -k wsdl/wsat-binding.wsdl

# $JBOSS_HOME/bin/wsconsume.sh -v -k -w /home/adinn/tmp/wsdl/wsba.wsdl -o tmp/classes -s tmp/src -k wsdl/wsba-binding.wsdl

# first the at services

$GF_HOME/bin/wsimport -verbose -keep -wsdllocation wsdl/wsat-completion-coordinator-binding.wsdl -d tmp/classes -s tmp/src -target 2.0 wsdl/wsat-completion-coordinator-binding.wsdl

$GF_HOME/bin/wsimport -verbose -keep -wsdllocation wsdl/wsat-completion-initiator-binding.wsdl -d tmp/classes -s tmp/src -target 2.0 wsdl/wsat-completion-initiator-binding.wsdl

$GF_HOME/bin/wsimport -verbose -keep -wsdllocation wsdl/wsat-coordinator-binding.wsdl -d tmp/classes -s tmp/src -target 2.0 wsdl/wsat-coordinator-binding.wsdl

$GF_HOME/bin/wsimport -verbose -keep -wsdllocation wsdl/wsat-participant-binding.wsdl -d tmp/classes -s tmp/src -target 2.0 wsdl/wsat-participant-binding.wsdl

# now the ba services

$GF_HOME/bin/wsimport -verbose -keep -wsdllocation wsdl/wsba-coordinator-completion-coordinator-binding.wsdl -d tmp/classes -s tmp/src -target 2.0 wsdl/wsba-coordinator-completion-coordinator-binding.wsdl

$GF_HOME/bin/wsimport -verbose -keep -wsdllocation wsdl/wsba-coordinator-completion-participant-binding.wsdl -d tmp/classes -s tmp/src -target 2.0 wsdl/wsba-coordinator-completion-participant-binding.wsdl

$GF_HOME/bin/wsimport -verbose -keep -wsdllocation wsdl/wsba-participant-completion-coordinator-binding.wsdl -d tmp/classes -s tmp/src -target 2.0 wsdl/wsba-participant-completion-coordinator-binding.wsdl

$GF_HOME/bin/wsimport -verbose -keep -wsdllocation wsdl/wsba-participant-completion-participant-binding.wsdl -d tmp/classes -s tmp/src -target 2.0 wsdl/wsba-participant-completion-participant-binding.wsdl

# we need to patch the resource lookup used to provide a jar base URL
# for the wsdl location supplied above wsimport generates
# e.g. 'qualified.path.ClassName.class.getResource(".")' to locate a
# URL for the jar dir containing ClassName.class. But the EJB class
# loader returns null for this (even though it returns true for
# hasEntry!). If we patch the generated code to specify
# 'qualified.path.ClassName.class.getResource("")' it works ok.

sed -i -e 's/CoordinatorService.class.getResource(".")/CoordinatorService.class.getResource("")/' tmp/src/org/oasis_open/docs/ws_tx/wsat/_2006/_06/CoordinatorService.java
sed -i -e 's/ParticipantService.class.getResource(".")/ParticipantService.class.getResource("")/' tmp/src/org/oasis_open/docs/ws_tx/wsat/_2006/_06/ParticipantService.java
sed -i -e 's/CompletionInitiatorService.class.getResource(".")/CompletionInitiatorService.class.getResource("")/' tmp/src/org/oasis_open/docs/ws_tx/wsat/_2006/_06/CompletionInitiatorService.java
sed -i -e 's/CompletionCoordinatorService.class.getResource(".")/CompletionCoordinatorService.class.getResource("")/' tmp/src/org/oasis_open/docs/ws_tx/wsat/_2006/_06/CompletionCoordinatorService.java

sed -i -e 's/BusinessAgreementWithCoordinatorCompletionCoordinatorService.class.getResource(".")/BusinessAgreementWithCoordinatorCompletionCoordinatorService.class.getResource("")/' tmp/src/org/oasis_open/docs/ws_tx/wsba/_2006/_06/BusinessAgreementWithCoordinatorCompletionCoordinatorService.java
sed -i -e 's/BusinessAgreementWithCoordinatorCompletionParticipantService.class.getResource(".")/BusinessAgreementWithCoordinatorCompletionParticipantService.class.getResource("")/' tmp/src/org/oasis_open/docs/ws_tx/wsba/_2006/_06/BusinessAgreementWithCoordinatorCompletionParticipantService.java
sed -i -e 's/BusinessAgreementWithParticipantCompletionCoordinatorService.class.getResource(".")/BusinessAgreementWithParticipantCompletionCoordinatorService.class.getResource("")/' tmp/src/org/oasis_open/docs/ws_tx/wsba/_2006/_06/BusinessAgreementWithParticipantCompletionCoordinatorService.java
sed -i -e 's/BusinessAgreementWithParticipantCompletionParticipantService.class.getResource(".")/BusinessAgreementWithParticipantCompletionParticipantService.class.getResource("")/' tmp/src/org/oasis_open/docs/ws_tx/wsba/_2006/_06/BusinessAgreementWithParticipantCompletionParticipantService.java
