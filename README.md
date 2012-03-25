Narayana
========

NOTE: PATCHED EMMA JAR
----------------------
This build uses a patch of the EMMA Jar, the source code for the patch is available from:

	https://svn.jboss.org/repos/labs/labs/jbosstm/workspace/emma

The latest version of the patched emma is available from /ext folder.


Building Naryana
----------------
To build Narayana you should call:

	./build.[sh|bat] <maven_goals>

To use this wrapper to build an individual module (say arjuna) you would type:

	./build.[sh|bat] clean install -pl :arjuna

If you are using a different maven installation to the one provided in tools/maven you need to make sure you have the following options:

	-Dorson.jar.location=/full/path/to/checkout/location/ext/orson-0.5.0.jar -Demma.jar.location=/full/path/to/checkout/location/ext/emma.jar

If you want to be able to cd into a folder to call our shipped maven or a version of maven not shipped with our project (on linux):

	1. Create a file called $HOME/bin/txmvn (assuming $HOME/bin is in the path)
	2. Add the following contents to this file:
	#!/bin/bash
	M2_HOME=~/projects/narayana/trunk/tools/maven/
	PATH=$M2_HOME/bin:$PATH
	EXT_JARS="-Demma.jar.location=\${user.home}/projects/narayana/trunk/ext/emma.jar -Dorson.jar.location=\${user.home}/projects/narayana/trunk/ext/orson-0.5.0.jar"
	mvn $EXT_JARS $@
	3. Ensure that $HOME/bin/txmvn is executable
	4. type something like "txmvn clean install"

NOTE: This does not set the memory options etc that build.[sh|bat] does so you will need to update this script to reflect our changes in build.[sh|bat]

Build Profiles
--------------
To speed up the build, several profiles are provided to restrict what is built. The available options are:

1. all  This builds everything
2. docs  This builds the documentation only
3. core  This builds the common and arjunacore modules
4. jta  This builds common, arjunacore and JTA
5. jts  This builds common, arjunacore, JTA and JTS
6. xts  This builds common, arjunacore, JTA and XTS
7. stm  This builds common, arjuna, txoj and the STM module

So, for example, to build stm you can type:

	./build.[sh|bat] clean install -P stm

Note that by default, if a parent module has documentation, it will be built, to disable this you can deactive the docs profile as follows:

	./build.[sh|bat] clean install -P !docs

Build QA
--------

cd qa/
ant -Ddriver.url=file:///home/hudson/dbdrivers get.drivers dist
ant -f run-tests.xml ci-tests

Now The Gory Details.
---------------------
Each module contains a set of maven build scripts, which chiefly just inherits and selectively overrides the parent
 pom.xml  Understanding this approach requires some knowledge of maven's inheritance.

Top level maven builds always start from scratch. Individual module builds on the other hand are incremental,
such that you may rebuild a single module by traversing into its directory and running 'mvn', but only if you
have first built any pre-req modules e.g. via a parent build.

In addition to driving the build of individual modules, the build files in the bundles directories (ArjunaCore,
ArjunaJTA, ArjunaJTS) contain steps to assemble the release directory structure, including docs, scripts,
config files and other ancillaries. These call each other in some cases, as JTS is largely a superset of
JTA and JTA in turn a superset of Core.

3rd party dependency management is done via maven. Note that versions of most 3rd party components are resolved via the JBossAS component-matrix
pom.xml, even when building standalone releases. The version of JBossAS to use is determined by the top level pom.xml
You may need to set up maven to use the jboss.org repositories: http://community.jboss.org/wiki/MavenGettingStarted-Users

The build currently requires Java 6 and maven 3.0.3. Maven is provided in the tools/maven section, though later versions of these tools may work. Download locations are:
http://www.oracle.com/technetwork/java/javase/downloads/index.html
http://maven.apache.org/


A handful of unit tests build and run as part of the normal build. Most test coverage is in the form of integration
tests which reside in the qa/ directory. These are built but not run automatically. See qa/README.txt for usage.


Developing Narayana
-------------------
Please see the following JIRA for details on how to configure your IDE for developing with the Narayana code styles:
	
	https://issues.jboss.org/browse/JBTM-989
