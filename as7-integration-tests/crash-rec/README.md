CMT: Using transactions managed by the container
================================================

What is it?
-----------

This example uses JTS to propagate a transaction from one server to another.

System requirements
-------------------

All you need to build this project is Java 6.0 (Java SDK 1.6) or better, Maven 3.0 or better.

The application this project produces is designed to be run on a JBoss AS 7 or EAP 6. 
The following instructions target JBoss AS 7, but they also apply to JBoss EAP 6.


Testing the application
-------------------------

Executing the following command will build and test the application using Arquillian:
mvn clean test -Parq-jbossas-managed -Djboss.dist=<PATH_TO_JBOSS_HOME>
NOTE: If you get the following you have not set -Djboss.dist=<PATH_TO_JBOSS_HOME>:

     [echo] Building AS instance "iiop-client" from ${jboss.dist} to /home/tom/projects/jbossas/quickstart/jts/target
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------


persistence-enabled true
