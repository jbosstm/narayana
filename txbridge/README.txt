
This is the WS-AT to JTA (XA) transaction bridge module.

Usage:
------

see docs/TransactionBridgingGuide

A word on version compatibility:
--------------------------------

This version, like the JBossTS it is bundled with, targets JBossAS 6  It probably will not work on earlier releases.
Ideally the txbridge should be used in conjunction with the JBossTS release it is included in, although it may
work with JBossTS 4.13.1 or later. This version won't work on any earlier release of JBossTS.

Building:
---------

ant
ensure XTS is deployed into the chosen server (see JBossAS docs/examples/transactions)
set jbossas.home and jbossas.server in build.xml
ant deploy

for the demo app, assuming the above steps are complete first:

cd demo
set jbossas.home and jbossas.server in build.xml
ant dist
ant deploy-service
ant deploy-client

See docs/TransactionBridgingGuide for further details on the demo app.

Testing:
--------

See docs/TransactionBridgingGuide - test framework section in the design notes appendix.