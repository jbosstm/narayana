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

This is the JBossTS QA Testsuite.

JBossTS provides distributed transaction management. The tests in the QA suite exercise
it using several processes (JVMs) for e.g. clients, servers and recovery processes. This
differs from normal unit tests that run in a single JVM. In addition the QA tests can
be long running, either because they do a lot of iterations for stress testing or because
they include realtime waits for e.g. recovery process timeouts and such. Thus running the
QA tests is a distinct step from running unit tests, which are an integral part of the build.

From July 2009, JBossTS no longer uses the DTF framework for running QA tests. Instead the
tests have been converted to junit4 and use a small scaffold to spawn the java processes
as required. This allows better use of existing tool support e.g. junit ant integration, hudson.

requires: jdk (1.6+), ant (1.7+), database drivers (for jdbc tests only, see build.xml)

build the product:
  cd jbossts_checkout_from_svn; ant jts
This will build the qa tests, although you can also do that manually with
  cd qa; ant

The tests are executed by the run-tests.xml ant script. There are various test groups, see
the script for an up to date list. For short(ish) smoke testing try

  ant -f run-test.xml express

This will test the product as built and placed in the ../install directory by the initial product build.
The test framework runs in an environment defined by the run-tests.xml script, but the test tasks i.e.
clients, servers etc, run in an environment dictated by the ./TaskImpl.properties file. Edit that file
as required for your system e.g. location of desired jvm and such.

To debug the test framework, see debug jvm arg in the run-test.xml junit-tests task.
To debug spawned processes, edit TaskImpl.properties to set debug command line args.

For jdbc tests, ensure the required drivers are present (see build.xml get.drivers target)
and create a suitable config/jdbc_profiles/<name_of_testnode_host>/ file by copying the existing
config/jdbc_profiles/_template/ directory or rely on the one in config/jdbc_profiles/default
By convention each test node has two accounts on each database server, with names of '<testnode_hostname>1'
and '<testnode_hostname>2'. This allows for testing of transactions with two resources. These accounts
need table creation privs plus the usual CRUD. The actual tables will be created automatically when the
tests run.

