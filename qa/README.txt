#######################################################################################
#
# JBoss, Home of Professional Open Source
# Copyright 2008, JBoss Inc., and others contributors as indicated
# by the @authors tag. All rights reserved.
# See the copyright.txt in the distribution for a
# full listing of individual contributors.
# This copyrighted material is made available to anyone wishing to use,
# modify, copy, or redistribute it subject to the terms and conditions
# of the GNU Lesser General Public License, v. 2.1.
# This program is distributed in the hope that it will be useful, but WITHOUT A
# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
# PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
# You should have received a copy of the GNU Lesser General Public License,
# v.2.1 along with this distribution; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
# MA  02110-1301, USA.
#
# (C) 2008,
# @author JBoss Inc.
#
#
#######################################################################################
#
# README.txt for JBossTS QA Testsuite.
# $Id$
# @author jonathan.halliday@redhat.com, created 2007-02-08

This is the JBossTS QA Testsuite, otherwise known as jbossts-qa and previously as ats-qa
It provides the glue between JBossTS and the DTF, including product specific tests and configuration.

As of April 2008, the DTF used to run these tests is open source. Thus the jbossts-qa test suite is
likewise being moved to open source.

For JBossTS 4.4 and later, the jbossts-qa test suite is in the JBossTS source repository directory qa/
i.e. it's now considered part of the JBossTS project, not an independent entity.
The DTF used to run it is at https://svn.jboss.org/repos/dtf/  (see http://labs.jboss.com/jbossdtf)

Earlier releases of JBossTS continue to use the closed source version of jbossts-qa, running under the
closed source version of DTF. For JBoss staff only, these can be found on the intranet at
  https://svn.corp.jboss.com/repos/qa/jbossts/trunk/
  https://svn.corp.jboss.com/repos/dtf/trunk/
  https://wiki.corp.jboss.com/bin/view/IT/SystemServiceQaDtfEnvironment

The live DTF environment used by JBoss to test JBossTS releases is not publicly accessible at present.
We hope to expose this test environment read-only to the community in due course, allowing for viewing
of our test results. Until then, users wishing to utilise DTF must install their own instance.

For JBoss staff wanting to access the internal servers, see the information at
  https://wiki.corp.jboss.com/bin/view/IT/SystemServiceQaDtfEnvironment


To use this package, you need:

  The JDK, apache ant, (plus tomcat and mysql if running the DTF locally)

  An installation of the Distributed Test Framework.
  If you don't already have access to one go fetch it from the URL abve:
  and follow the instructions in the DTF-Install-README.txt file.
  Note: build with Java 5 (export JAVA_HOME=... before running ant) if you want to run tests
  on a 5 JVM, or run the framework itself on 5. A Java 6 build won't run tests on Java 5.

  A build of the JBossTS, JTS edition to test. If you don't already have one then either:
  download a binary from http://labs.jboss.com/portal/jbosstm/downloads (get the 'full', not the 'jta' version)
  or
  svn co http://anonsvn.labs.jboss.com/labs/jbosstm/ and build with 'ant jts'
  Note: You need to build using Java 5 if you expect to test on that version of the JVM.

TODO: idl compiler

  JDBC Database drivers for any database you want to test against (there are issues with redistributing
  the drivers for commercial dbs, hence they are not provided bundled with this project)

TODO: check licencing on specific db drivers - can we check them into this project? It would make life easier...
should be possible for the open source dbs at least, plus maybe others

Now edit the build.xml file in this directory, setting the properties near the top to the locations of
the various pre-requisites mentioned above. If you want to package an AS as part of the test suite
then make sure that the JBOSS_HOME environment variable points to a valid JBoss distribution and
set the ant property 'as' to true. When runing the tests ensure that the version of TestingFramework.jar used
running the DTF is the same one that is checked into JBossTS.

Create the directory config/jdbc_profiles/<name_of_testnode_host>/ by copying the existing
config/jdbc_profiles/_template/ directory, one for each host on which a test node will run.
Edit the JDBCProfiles file in the new director(y|ies), setting the database connection information.
By convention each test node has two accounts on each database server, with names of '<testnode_hostname>1'
and '<testnode_hostname>2'. This allows for testing of transactions with two resources. These accounts
need table creation privs plus the usual CRUD. The actual tables will be created automatically when the
tests run.

Set the JAVA_HOME to the older version of Java you may want to run tests on..

Run 'ant'. This target will build JTA/JTS version of the product if the JBossTS build was JTA/JTS.
The builds generates a .zip file with the QA distribution, which contains the tests plus the product itself
copied in from your installation above, plus the database drivers, plus some config files and a copy of
the JBoss AS if built with 'ant -Das=true'.

To deploy the built product QA distribution to the DTF:

  Edit the product configuration file config/JBossTS_JTS_JacORB_QA.xml to suit your DTF installation.
  Copy it to the testenv/services/products/ directory of the DTF installation.

  Edit the product installation file config/jbossts-jts-qa-install.xml to suit your DTF installation.
  Copy it to the appropriate directory on the DTF web server, typically webapps/dtf/productinstallers/

  (you can skip the two parts above if you just are redeploying a fresh build over an existing one)

  Copy the test package build/jbossts-jts-qa-distribution.zip to the appropriate directory
  on the DTF web server, typically webapps/dtf/productbuilds/

  Bind the product to the installer file using the DTF webapp Deployment menu
  (e.g. JBossTS_JTS_JacORB_QA -> http://.../dtf/productinstallers/jbossts-jts-qa-install.xml)

You can now deploy the product to test nodes, define tests (files can be uploaded from the
testdefs/ directory of this project) and run selections of them through the DTF web application.
However, doing this by hand on a new DTF installation is tedious, so here is a rather hacky optional
shortcut for those who have control over their own DTF installation and don't mind taking risks:

  Testdefs (what is involved in a test or group of tests e.g. client and server processes etc)
  and Test selections (which tests to run on which platforms) are stored in .xml files and also
  have entries in the DTF database. You can upload testdef files to the webapp, which creates the
  database entry for them automatically, including assigning them a sequence number. However, you
  can't do this with test selections files. Test selections are normally defined through the web
  interface, which creates both the .xml file and the database entry. These each contain reference to
  the sequence number of the corresponding testdefs. Since this number depends on the order in which
  the testdefs were installed, it may differ from one DTF instance to another. Thus to populate a
  DTF installation with pre-existing testdefs and testselections, you need to manipulate the database
  directly to ensure the correlation of sequence numbers. You'll probably be able to get away with this
  only if there are no existing testdefs or testselections in the database.

  The basic procedure is:

    Copy the files testdefs/*.xml to the DTF's webapps/dtf/producttests/ directory
    Copy the files testselections/*.xml to the DTF's webapps/dtf/producttests/ directory
    Run the config/load-jbossts-sql.sql file against the DTF database as the dtf user.
    Make appropriate ritual sacrifices, cross your fingers and restart the DTF.

  If you take this route, you then have the ability to use the config/run-tests.sh and config/run-qa.xml
  files to automate your test runs. The .xml file contains test selection file names, so it works
  only with the specific sequence numbers generated by the above steps, not any random DTF installation.
  You'll need to tweak the urls in these files to point to your DTF server.


Addendum for running JTA rather than JTS QA:

  Read the JTS deployment stuff above first so you know what is going on, this section is very terse.
  You need to build or obtain the JBossTS JTA rather than JTS ('ant jta')
  Edit build.xml, set buildtype to jta (TODO: determine automatically from .jars in ts.home?)
  Edit config/JBossTS_JTA_QA.xml and copy to the server
  Edit config/jbossts-jta-qa-install.xml and copy to the server TODO
  copy build/jbossts-jta-qa-distribution.zip to the server
  Bind JBossTS_JTA_QA -> http://.../dtf/productinstallers/jbossts-jta-qa-install.xml


Note: txoj tests require test classes from JBossTS that are not installed by default. try e.g.
 scp ArjunaCore/txoj/lib/tests/txoj_tests.jar /services/DTF/JBossTS-JTA-QA/lib/ext/
