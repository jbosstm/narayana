
This is the prototype transaction bridge module.

Usage:
------

see docs/TransactionBridgingGuide

A word on quality:
------------------

It's still a work in progress and should be regarded as beta code at best,
regardless of the designation of the JBossTS release in whose scope it is released.
API stability between releases is not guaranteed. Don't use it in production.

A word on version compatibility:
--------------------------------

This version, like the JBossTS it is bundled with, targets JBossAS 6  It probably will not work on earlier releases.
Ideally the txbridge should be used in conjunction with the JBossTS release it is included in, although it may
work with JBossTS 4.6.1 or later. This version won't work on any earlier release of JBossTS. Try the original
prototype from http://anonsvn.jboss.org/repos/labs/labs/jbosstm/workspace/jhalliday/ if you need it to work
on earlier versions.

Building:
---------

set jbossas.home and jbossas.server in build.xml
ensure XTS is deployed into the chosen server (see JBossAS docs/examples/transactions)
ant dist
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

See tests/README.txt