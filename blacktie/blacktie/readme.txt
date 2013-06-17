JBoss, Home of Professional Open Source
Copyright 2010, Red Hat, Inc., and others contributors as indicated
by the @authors tag. All rights reserved.
See the copyright.txt in the distribution for a
full listing of individual contributors.
This copyrighted material is made available to anyone wishing to use,
modify, copy, or redistribute it subject to the terms and conditions
of the GNU Lesser General Public License, v. 2.1.
This program is distributed in the hope that it will be useful, but WITHOUT A
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License,
v.2.1 along with this distribution; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
MA  02110-1301, USA.

Installing Blacktie into JBoss AS

   1 Download 5.1.0.GA
  	- Extract into any location on your hard drive, this will be referred to as JBOSS_HOME
   2. Download HornetQ 2.1.2.Final and configure JBoss AS to use it:
    - Extract into any location on your harddrive we will call this HQ_HOME
    - cd HQ_HOME/config/jboss-as-5/
    - chmod 775 build.sh
    - Ensure JBOSS_HOME is set and run ./build.sh
    - Add the blacktie administration security configuration to 
          JBOSS_HOME/server/all-with-hornetq/deploy/hornetq.sar/hornetq-configuration.xml
      <security-setting match="jms.queue.BTR_BTDomainAdmin">
         <permission type="send" roles="blacktie,guest"/>
         <permission type="consume" roles="blacktie,guest"/>
      </security-setting>
      <security-setting match="jms.queue.BTR_BTStompAdmin">
         <permission type="send" roles="blacktie,guest"/>
         <permission type="consume" roles="blacktie,guest"/>
      </security-setting>
     - Configure HQ to not use connection buffering
        In:
           JBOSS_HOME/server/all-with-hornetq/deploy/hornetq.sar/hornetq-jms.xml
        Replace:
           <connection-factory name="InVMConnectionFactory">
        With:
           <connection-factory name="InVMConnectionFactory">
              <consumer-window-size>0</consumer-window-size>
     - Configure HornetQ to not timeout InVM connections
        In:
           JBOSS_HOME/server/all-with-hornetq/deploy/hornetq.sar/hornetq-jms.xml
        Replace:
           <connection-factory name="InVMConnectionFactory">
        With: 
           <connection-factory name="InVMConnectionFactory">
              <connection-ttl>-1</connection-ttl>
              <client-failure-check-period>86400000</client-failure-check-period>
        And in:
           JBOSS_HOME/server/all-with-hornetq/deploy/hornetq-ra.rar/META-INF/ra.xml
        Replace:
           <resourceadapter-class>org.hornetq.ra.HornetQResourceAdapter</resourceadapter-class>
        With:
           <resourceadapter-class>org.hornetq.ra.HornetQResourceAdapter</resourceadapter-class>
              <config-property>
              <description>The connection TTL</description>
              <config-property-name>ConnectionTTL</config-property-name>
              <config-property-type>java.lang.Long</config-property-type>
              <config-property-value>-1</config-property-value>
           </config-property>
           <config-property>
              <description>The client failure check period</description>
              <config-property-name>ClientFailureCheckPeriod</config-property-name>
              <config-property-type>java.lang.Long</config-property-type>
              <config-property-value>86400000</config-property-value>
           </config-property>
   3 Configure JBoss AS to use JTS:
   	- cd JBOSS_HOME/docs/quickstarts/transactions;
   	- Type: ant jts -Dtarget.server.dir=../../../server/all-with-hornetq (this installs the JTS version of JBoss Transactions);
   	- Edit JBOSS_HOME/server/all-with-hornetq/conf/jbossts-properties.xml to change the CONFIGURATION_FILE to NAME_SERVICE
      (this cause JBossTS to make its TransactionFactory available as a CORBA object);
   4. Edit $BLACKTIE_HOME/setenv.sh|bat:
      - make sure BLACKTIE_HOME is set to the BlackTie install location;
      - make sure JBOSSAS_IP_ADDR is set to the interface on which the application server is running (see step 6 below);
   5. Copy $BLACKTIE_HOME/blacktie-admin-services/blacktie-admin-services-[VERSION].ear to JBOSS_HOME/server/all-with-hornetq/deploy
   6. Copy $BLACKTIE_HOME/blacktie-admin-services/stompconnectservice-[VERSION].ear to JBOSS_HOME/server/all-with-hornetq/deploy
   7. Copy $BLACKTIE_HOME/blacktie-admin-services/blacktie-rhq-plugin-[VERSION].jar to JBOSS_HOME/server/all-with-hornetq/deploy/admin-console.war/plugins
   8. Start the all configuration of the application server:
      cd JBOSS_HOME; bin/run.sh -c all-with-hornetq -b $JBOSSAS_IP_ADDR

Running the Quickstarts

   1. cd $BLACKTIE_HOME
   2. Make sure the setenv.[sh|bat] script has been sourced
   3. To run the queue quickstart you need to deploy a special queue
      (see quickstarts/xatmi/queues/README for instruction on how to deploy the queue))
   4. To run all the quickstarts execute the quickstarts script run_all_quickstarts.[sh|bat]

quickstarts/xatmi/txfooapp
    Demonstrates updating multiple databases within a single transaction. To run the quickstart follow the README.

quickstarts/xatmi/fooapp
    Starts a simple echo server and runs two clients (written in Java and C)

quickstarts/admin/jmx
    Starts a simple server and then stops the server using the JMX interface to the BlackTie mbean component

quickstarts/admin/xatmi
    Starts a simple server and then stops the server by sending XATMI requests to the Administration service

quickstarts/xatmi/queues
	Tests "externally managed queues" by sending asynchronous service requests before the service is running.
	Then a service is started to process those requests.

quickstarts/security
    Shows how services may be secured using the <security> element in the BlackTie configuration file (btconfig.xml)
    Refer to the README file in the quickstarts/security directory for more information

quickstarts/mdb-xatmi-service
    Demonstrates how to write BlackTie services as MDB's: starts a service (implemented as an MDB) running in the 
    App Server. Then runs a client which uses the jatmibroker api to send requests to the service. The quickstart also shows 
    the service making EJB calls to demonstrate transaction propagation.

integration1
    Shows how a C client can invoke an EJB (via a "BlackTie service adaptor") within the scope of a transaction
    started at the client.

