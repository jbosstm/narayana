This directory contains ant build scripts for running TS tests with an AS under
the DTF framework.

To add new tests you may either add new targets to an existing script or write
a new script. For an example of how to run a target under DTF please refer to
the test selection jbossts-qa-as-crashrecovery01-testdefs.xml located in
the testdefs directory.

These tests normally run as part of the DTF test suite. To run them in
standalone mode you will need define a security policy and override the ant
property product.dir (which controls where the tests will search for product
resources) as follows:
   export ANT_OPTS="-Djava.security.policy=../src/org/jboss/jbossts/qa/astests/resources/java.policy
-Dproduct.dir=../../build/"

If the target product directory does not contain a directory called 'as'
containing a JBOSS installation then the environment variable JBOSS_HOME will
be used.
