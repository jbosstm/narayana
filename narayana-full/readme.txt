          WELCOME TO NARAYANA
          -------------------
This release of Narayana is designed for use standalone.
The JTA is also used by JBossAS 7 releases, but manual upgrading of the component inside JBossAS is not recommended.
 JTS and XTS are not currently available in AS7.
Integration with JBossAS 6 or earlier is no longer supported.

This version of Narayana contains a JTA/JTS/XTS and RTS. You should never include both lib/jts/*.jar and lib/jta/*.jar as they will conflict with each other.

          WHATS INCLUDED
          --------------
This release contains:
  bin/
    rest-tx-web.war - This can be deployed into a server running Narayana JTA to expose the Rest-TX API
    ws-*.war - These wars can be used to deploy XTS standalone
    jbossts-jopr-plugin - This can be deployed into a server running Narayana JTA and RHQ to administer the server - more details below
    start-*.[bat|sh] - These scripts can be used to launch standalone transaction managers and recovery managers
  
  docs/
    api - The various project components javadocs
    guides - PDF versions of the projects documentation
    idl - The IDL files for integration with the JTS
    jts/
      trailmap - A trailmap for the project
    xts/
      demo - A trailmap for the project
      
  etc/
    Some example configuration files are available here, remove the .jts or .jta suffix to enable the operation mode you require
    
  jacorb/
    A jacorb deployment for use with JTS
    
  lib/
    ext/ - All the dependencies required to run Narayana JTA/JTS/XTS
    txbridge/jbosstxbridge.jar - After removing the .txbridge suffix, deploy this into a server running JTA and XTS to enable transactional bridging
    xts/jbossxts*.jar - More details are available in the XTS guide.
    jta/narayana-jta*.jar - See below for usage information
    jts/narayana-jts*.jar - See below for usage information
    rts/rest-tx-api.jar
    
  quickstarts/
    Some examples to get you up and running quickly
  
  services/
    This directory contains the files required to install Narayana as an operating system service
    
  /
    jta-setup-env-*.[bat|sh] - This will configure an environment for use with Narayana JTA
    jts-setup-env-*.[bat|sh] - This will configure an environment for use with Narayana JTA
    copyright.txt - A file to provide the copyright
    readme.txt - This file

    

          ENABLING JTA
          ------------
To enable JTA you MUST:
1. Include lib/ext/*.jar and lib/jta/narayana-jta.jar
2. If you need to use this version of JTA in an application server, you should also include lib/jta/narayana-jta-integration.jar, however, note the comment above regarding manual upgrading of the component inside JBossAS is not recommended
3. Execute jta-setup-env.[bat|sh] to put JTA in the classpath


          ENABLING JTS
          ------------
To enable JTS you MUST:
1. Include lib/ext/*.jar and lib/jts/narayana-jts.jar
2. If you need to use this version of JTS in an application server, you should also include lib/jts/narayana-jts-integration.jar, however, note the comment above regarding manual upgrading of the component inside JBossAS is not recommended
3. Execute jta-setup-env.[bat|sh] to put JTA in the classpath


          ENABLING XTS
          ------------ 
Please read the XTS guide for details on this, you will need to include the following files:
1. lib/ext/*.jar
2. lib/xts/jbossxts.jar
3. lib/xts/jbossxts-api.jar


          ENABLING TXBRIDGE
          ----------------- 
Please read the txbridge guide for details on this, you will need to include the following file:
1. lib/txbridge/jbosstxbridge.jar



          ENABLING REST-TX API
          -------------------- 
Please read the txbridge guide for details on this, you will need to include the following file:
1. lib/rts/rest-tx-api.jar


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
