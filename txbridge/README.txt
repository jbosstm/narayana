
This is the WS-AT to JTA (XA) transaction bridge module.

Usage:
------

see docs/TransactionBridgingGuide

A word on version compatibility:
--------------------------------

This version, like the JBossTS it is bundled with, targets JBossAS 7. It probably will not work on earlier releases.
The txbridge should be used in conjunction with the JBossTS release it is included in.

Building:
---------

Build the bridge:
mvn install

Demo
----

A demo of how to use the bridge is available as part of the Narayana quickstarts project. 

See docs/TransactionBridgingGuide for further details on the demo app.

Testing:
--------

See docs/TransactionBridgingGuide - test framework section in the design notes appendix.
