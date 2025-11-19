#
# SPDX short identifier: Apache-2.0
#

#
#######################################################################################
#
# README.txt for JBossTS QA Testsuite.
# $Id$
# @author jonathan.halliday@redhat.com, created 2007-02-08

This is the JBossTS QA Testsuite.

JBossTS provides distributed transaction management. The tests in the QA suite exercise
it using several processes (JVMs) for e.g. clients, servers and recovery processes. This
differs from normal unit tests that run in a single JVM. In addition the QA tests can
be long running, either because they do a lot of iterations for stress testing or because
they include realtime waits for e.g. recovery process timeouts and such. Thus running the
QA tests is a distinct step from running unit tests, which are an integral part of the build.

From July 2009, JBossTS no longer uses the DTF framework for running QA tests. Instead the
tests have been converted to junit4 and use a small scaffold to spawn the java processes
as required. This allows better use of existing tool support e.g. junit ant integration.

requires: jdk (1.9+), ant (1.9.6+), database drivers (for jdbc tests only, see build.xml)

To build the qa tests:
  cd qa
  ant

The test framework runs in an environment defined by the run-tests.xml script, but the test tasks i.e.
clients, servers etc, run in an environment dictated by the ./TaskImpl.properties file. 

To create that file, copy TaskImpl.properties.template and name it TaskImpl.properties. Proceed to edit 
the new file as required for your system e.g. location of desired jvm and such.

The tests are executed by the run-tests.xml ant script. There are various test groups, see
the script for an up to date list. For short(ish) smoke testing try

  ant -f run-test.xml express

This will test the product as built by the initial product build.

To debug the test framework, see debug jvm arg in the run-test.xml junit-tests task.
To debug spawned processes, edit TaskImpl.properties to set debug command line args. You can also
enable a robust debugging mode by setting a system property tasks.remote.debug. This is set in the junit
process and will set debugging ports up incrementing from port 5000 for the first spawned process.

For JDBC tests, ensure the required drivers are present (`cd qa && mvn install`). Then, starting from `config/jdbc_profiles/_template/JDBCProfiles`, create a suitable `JDBCProfiles` file in:
* `config/jdbc_profiles/<name_of_testnode_host>/` to use this configuration for a specific testnode host
* `config/jdbc_profiles/default/` to apply a default configuration to all testnode hosts
By convention each test node has two accounts on each database server, with names of '<testnode_hostname>1'
and '<testnode_hostname>2'. This allows for testing of transactions with two resources. These accounts
need table creation privs plus the usual CRUD. The actual tables will be created automatically when the
tests run.
Please note that if the parameter `profile` is passed as an `ant` parameter (e.g. `-Dprofile=postgres`)
then QA will only run against the specified database profile.

# Running a single test
There is a way to run specific QA tests. In the run-tests.xml there is a target "onetest" - it runs a 
single test based on various system properties.

  cd qa/
  ant
  ant -f run-tests.xml onetest -Dtest.name=rawresources01_1 -Dtest.methods=RawResources01_1_Test001
