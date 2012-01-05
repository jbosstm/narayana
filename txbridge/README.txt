
This is the WS-AT to JTA (XA) transaction bridge module.

Usage:
------

see docs/TransactionBridgingGuide

A word on version compatibility:
--------------------------------

This version, like the JBossTS it is bundled with, targets JBossAS 7. It probably will not work on earlier releases.
Ideally the txbridge should be used in conjunction with the JBossTS release it is included in, although it may
work with JBossTS 4.13.1 or later. This version won't work on any earlier release of JBossTS.

Building:
---------

Build the bridge:
ant

Build and deploy the demo:

cd demo
set jbossas.home and jbossas.server in build.xml
ant dist
ant deploy-service
ant deploy-client

Start JBoss AS:
---------------

ensure JBoss AS is started with XTS enabled. This can be done by specifying the standalone-xts configuration when starting JBoss AS:

cd $JBOSS_HOME
./bin/standalone.sh --server-config=standalone-xts.xml


See docs/TransactionBridgingGuide for further details on the demo app.

Testing:
--------

See docs/TransactionBridgingGuide - test framework section in the design notes appendix.
