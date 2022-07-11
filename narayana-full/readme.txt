          WELCOME TO NARAYANA
          -------------------
This release of Narayana is designed for use standalone.
The transaction manager is also used by WildFly 8+ releases, but manual upgrading of the component inside WildFly is not recommended.
Integration with JBossAS 6 or earlier is no longer supported.

This version of Narayana contains JTA/JTS transaction managers and an XTS and RTS. You should never include both lib/jts/*.jar and lib/jta/*.jar as they will conflict with each other.

          WHAT'S INCLUDED
          ---------------
This release contains:
  bin/
    restat-web.war - This can be deployed into a server running Narayana JTA
    to expose the Rest-TX API
    ws-*.war - These wars can be used to deploy XTS standalone
    jbossts-jopr-plugin - This can be deployed into a server running Narayana
    JTA and RHQ to administer the server - more details below
    start-*.[bat|sh] - These scripts can be used to launch standalone
    transaction managers and recovery managers
      
  etc/
    Some example configuration files are available here, copy to CWD and rename to jbossts-properties.xml to enable the operation mode you require
  
  idl/
    The IDL files for integration with the JTS
    
  lib/
    ext/ - All the dependencies required to run Narayana JTA/JTS/XTS
    txbridge/jbosstxbridge.jar
    xts/jbossxts*.jar
    jta/narayana-jta*.jar
    jts/narayana-jts*.jar
    rts/restat-api.jar
    
  services/
    This directory contains the files required to install Narayana as an operating system service
    
  /
    jta-setup-env.[bat|sh] - This will configure an environment for use with Narayana JTA
    jts-*-setup-env.[bat|sh] - This will configure an environment for use with Narayana JTA
    copyright.txt - A file to provide the copyright
    readme.txt - This file
    run_all_quickstarts.[bat|sh] - scripts to run all quickstarts. You will
    need to separately download and unzip the quickstarts before running these
    scripts

          WHAT'S NOT INCLUDED
          -------------------
This release download does not contain project documentation or quickstarts.

Online versions of the documentation are available from the project website at
http://narayana.io/documentation/.

Documentation source is now in docbook format and the associated files are
tagged at github: https://github.com/jbosstm/documentation/releases. Select
the archive corresponding to this release.

Previous releases contained a separate guide for the trailmap which has now
been moved to the "Narayana JTS Development Guide"

Examples to get you up and running quickly are now tagged at github:
https://github.com/jbosstm/quickstart/releases
Select the archive corresponding to this release and unzip it. The
run_all_quickstarts shell and batch scripts will run all the quickstarts.
Make sure you update the scripts to point to the directory where you unzipped
the quickstart archive.

          ENABLING JTA
          ------------
To enable JTA you MUST:
1. Execute jta-setup-env.[bat|sh] to put Narayana in the classpath
2. If you need to use this version of Narayana in an application server, you should also include lib/jts/narayana-jts-integration.jar, however, note the comment above regarding manual upgrading of the component inside JBossAS is not recommended
3. If you do not intend to launch a recovery manager in your application, execute: ./bin/start-recovery-recovery-manager.[bat|sh]


          LAUNCHING JTS
          -------------
To enable JTS you MUST:
1. Execute jts-${ORB}-setup-env.[bat|sh] to put Narayana in the classpath
2. Copy default-jts-${ORB}-jbossts-properties.xml into your CWD and rename it to jbossts-properties.xml
3. If you need to use this version of Narayana in an application server, you should also include lib/jts/narayana-jts-integration.jar, however, note the comment above regarding manual upgrading of the component inside JBossAS is not recommended
4. If you do not intend to launch a recovery manager in your application, execute: ./bin/start-recovery-recovery-manager.[bat|sh]
5. If you do not intend to launch an in process transaction manager, execute: ./bin/start-transaction-service.[bat|sh]


          ENABLING XTS
          ------------ 
XTS standalone is not yet documented. For details on running in this configuration you will need to include the following files:
1. lib/ext/*.jar
2. lib/xts/jbossxts.jar
3. lib/xts/jbossxts-api.jar
You can consult this community thread for more tips: http://community.jboss.org/message/554385#554385


          ENABLING TXBRIDGE
          ----------------- 
Please read the txbridge guide for details on this, you will need to include the following file:
1. lib/txbridge/jbosstxbridge.jar



          ENABLING REST-TX API
          -------------------- 
Please read the rts guide for details on this, you will need to include the following file:
1. lib/rts/restat-api.jar


          EMBEDDED TOOLS
          --------------
JMX Instrumentation
-------------------
With this release it is now possible to monitor the transaction Object
Store using JMX. Monitoring the Object Store is useful for trouble
shooting problems that occur when transactions are committed (it does
not expose transactions prior to commit). The JMX instrumentation
(of the ObjectStore) is a new feature and is not necessarily suitable
for monitoring production systems.

In any compliant JMX browser (such as jconsole) there should appear an
MBean with the name
    jboss.jta:type=ObjectStore
This MBean corresponds to the object store and provides a naming context
for other MBeans (for example in a JMX client such as jconsole the MBeans
will be displayed in the form of a tree control). The various MBeans
corresponding to this ObjectStore will have names prefixed by this
'top level' MBean.

Simply instantiate an object of type com.hp.mwtests.ts.arjuna.tools.ObjStoreBrowser
and initialise it with a valid set of types for handling ObjectStore records. These types
can be set using a setTypes() method via a properties file on the classpath.
Please refer to the unit tests in the src distribution for more details.

Tools Deployment
----------------
Transaction management is integrated into the admin console in the form of a JOPR plugin
which is located in the install bin directory (jbossts-jopr-plugin.jar). Install it by copying
to the admin console plugin directory ($JBOSS_HOME/common/deploy/admin-console.war/plugins).

There is also a transaction statistics graphing tool which can run standalone or inside a
jconsole tab (jconsole, a tool for managing JVMs, is distributed with the reference JDK).
Various transaction statistics are graphed in real time with each graph updated during each
poll interval (4 seconds unless the interval is overridden on the jconsole command line).

The tool depends on the JFree graphing library. Download and upack Orson from http://www.jfree.org/orson
and set the env variable ORSON_HOME to the directory where you plan to unpack the downloaded zip.
If you intend to use the tool with jconsole you will also need to put the JDK tools and jconsole jars on
the classpath:

export CLASSPATH="$JDK_HOME/lib/tools.jar:$JDK_HOME/lib/jconsole.jar:$ORSON_HOME/orson-0.5.0.jar:$ORSON_HOME/lib/jfreechart-1.0.6.jar:$ORSON_HOME/lib/jcommon-1.0.10.jar:$TS_INSTALL_DIR/lib/narayana-jts.jar>"

Standalone Usage:

java com.arjuna.ats.arjuna.tools.stats.TxPerfGraph

(note that standalone usage does not require the JDK tools and jconsole jars)

Usage with jconsole:

jconsole -J-Djava.class.path="$CLASSPATH" -pluginpath $TS_INSTALL_DIR/lib/narayana-jts.jar

The tool will automatically enable statistics gathering on startup. It is recommended that you disable statistics gathering prior to exit on the Settings tab in the GUI.
