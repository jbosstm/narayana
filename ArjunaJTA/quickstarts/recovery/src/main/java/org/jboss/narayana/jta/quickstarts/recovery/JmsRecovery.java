/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2011,
 * @author JBoss, by Red Hat.
 */
package org.jboss.narayana.jta.quickstarts.recovery;

import java.util.ArrayList;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.XAConnection;
import javax.jms.XASession;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.jms.HornetQJMSClient;
import org.hornetq.api.jms.JMSFactoryType;
import org.hornetq.core.config.Configuration;
import org.hornetq.core.config.impl.ConfigurationImpl;
import org.hornetq.core.remoting.impl.invm.InVMAcceptorFactory;
import org.hornetq.core.remoting.impl.invm.InVMConnectorFactory;
import org.hornetq.core.remoting.impl.netty.NettyAcceptorFactory;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.hornetq.core.server.JournalType;
import org.hornetq.jms.client.HornetQConnectionFactory;
import org.hornetq.jms.server.config.ConnectionFactoryConfiguration;
import org.hornetq.jms.server.config.JMSConfiguration;
import org.hornetq.jms.server.config.JMSQueueConfiguration;
import org.hornetq.jms.server.config.impl.ConnectionFactoryConfigurationImpl;
import org.hornetq.jms.server.config.impl.JMSConfigurationImpl;
import org.hornetq.jms.server.config.impl.JMSQueueConfigurationImpl;
import org.hornetq.jms.server.embedded.EmbeddedJMS;
import org.hornetq.jms.server.recovery.HornetQXAResourceRecovery;
import org.hornetq.utils.UUIDGenerator;
import org.jboss.narayana.jta.quickstarts.util.DummyXAResource;
import org.jboss.narayana.jta.quickstarts.util.DummyXid;
import org.jboss.narayana.jta.quickstarts.util.Util;

import com.arjuna.ats.jta.common.JTAEnvironmentBean;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

public class JmsRecovery extends RecoverySetup {
    private static EmbeddedJMS jmsServer;
    private static HornetQConnectionFactory xacf;
    private static Queue queue;
    private static boolean inVM = true;

    public static void main(String[] args) throws Exception {

        if (args.length == 1) {
            startServices();

            if (args[0].equals("-f")) {
                new JmsRecovery().testXAWithErrorPart1();
            } else if (args[0].equals("-r")) {
                startRecovery();
                new JmsRecovery().testXAWithErrorPart2();
            }

            stopServices();
        }

        System.err.println("to generate something to recover: java JmsRecovery -f");
        System.err.println("to recover from the failure: java JmsRecovery -r");
    }

    public static void startServices() throws Exception
    {
        startHornetq();
        startRecovery();
    }

    public static void stopServices() throws Exception
    {
        stopRecovery();
        stopHornetq();
    }

    private static void startHornetq() throws Exception {
        /*
         * Step 1. Decide whether to use inVM or remote communications:
         *  clients connect to servers by obtaining connections from a ConnectorFactory
         *  servers accept connections from clients by obtaining acceptors from an AcceptorFactory
         */
        String acceptorName = inVM ? InVMAcceptorFactory.class.getName() : NettyAcceptorFactory.class.getName();
        String connFacName = inVM ? InVMConnectorFactory.class.getName() :NettyConnectorFactory.class.getName();

        String queueName = "/queue/queue1";

        startHornetqServer(acceptorName, connFacName, queueName);
        initialiseHornetqClient(connFacName, queueName);
    }

    private static void stopHornetq() throws Exception {
        xacf.close();
        jmsServer.stop();
    }

    public static void startRecovery() {
        String resourceRecoveryClass = HornetQXAResourceRecovery.class.getName();
        String inVMResourceRecoveryOpts = org.hornetq.core.remoting.impl.invm.InVMConnectorFactory.class.getName();
        String remoteResourceRecoveryOpts = org.hornetq.core.remoting.impl.netty.NettyConnectorFactory.class.getName();

        /*
         * Tell JBossTS how to recover Hornetq resources. To do it via jbossts-properties.xml use
         *  <entry key="JTAEnvironmentBean.xaResourceRecoveryClassNames">
         */
        List<String> recoveryClassNames = new ArrayList<String>();

        if (inVM)
            recoveryClassNames.add(resourceRecoveryClass + ";" + inVMResourceRecoveryOpts);
        else
            recoveryClassNames.add(resourceRecoveryClass + ";" + remoteResourceRecoveryOpts);

        BeanPopulator.getDefaultInstance(JTAEnvironmentBean.class).setXaResourceRecoveryClassNames(recoveryClassNames);

        RecoverySetup.startRecovery();
    }

    private static void startHornetqServer(String acceptorName, String connFacName, String queueName) throws Exception
    {
        // Step 2. Create the server configuration
        Configuration configuration = new ConfigurationImpl();

        configuration.setPersistenceEnabled(true);
        configuration.setSecurityEnabled(false);
        configuration.setJournalType(JournalType.NIO);
        configuration.setJournalDirectory(Util.hornetqStoreDir);
        configuration.setBindingsDirectory(Util.hornetqStoreDir + "/bindings");
        configuration.setLargeMessagesDirectory(Util.hornetqStoreDir + "/largemessages");

        configuration.getAcceptorConfigurations().add(new TransportConfiguration(acceptorName));
        configuration.getConnectorConfigurations().put("connector", new TransportConfiguration(connFacName));

        // Step 3. Create the JMS configuration
        JMSConfiguration jmsConfig = new JMSConfigurationImpl();

        // Step 4. Configure the JMS ConnectionFactory
        ArrayList<String> connectorNames = new ArrayList<String>();
        connectorNames.add("connector");
        ConnectionFactoryConfiguration cfConfig = new ConnectionFactoryConfigurationImpl("cf", true,  connectorNames, "/cf");
        jmsConfig.getConnectionFactoryConfigurations().add(cfConfig);

        // Step 5. Configure the JMS Queue
        JMSQueueConfiguration queueConfig = new JMSQueueConfigurationImpl("queue1", null, true, queueName);
        jmsConfig.getQueueConfigurations().add(queueConfig);

        // Step 6. Start the JMS Server using the HornetQ core server and the JMS configuration
        jmsServer = new EmbeddedJMS();
        jmsServer.setConfiguration(configuration);
        jmsServer.setJmsConfiguration(jmsConfig);
        jmsServer.start();

        System.out.println("Embedded JMS Server is running");
    }

    // Step 6 Initialise client side objects: connection factory and JMS queue
    private static void initialiseHornetqClient(String connFacName, String queueName) {
        xacf = HornetQJMSClient.createConnectionFactoryWithoutHA(JMSFactoryType.XA_CF, new TransportConfiguration(connFacName));
        queue = (Queue)jmsServer.lookup(queueName);
    }

    private void endTx(XAResource xaRes, Xid xid, boolean commit) throws XAException {
        xaRes.end(xid, XAResource.TMSUCCESS);
        xaRes.prepare(xid);
        if (commit)
            xaRes.commit(xid, false);
        else
            xaRes.rollback(xid);
    }

    private Xid startTx(XAResource xaRes) throws XAException {
        Xid xid = new DummyXid("xa-example1".getBytes(), 1, UUIDGenerator.getInstance()
                    .generateStringUUID()
                    .getBytes());

        xaRes.start(xid, XAResource.TMNOFLAGS);

        return xid;
    }

    private void startJTATx(XAResource ... resources) throws XAException, SystemException, NotSupportedException, RollbackException {
        TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

        tm.begin();

        for (XAResource xaRes : resources)
            tm.getTransaction().enlistResource(xaRes);
    }

    private void endJTATx(boolean commit) throws SystemException, RollbackException, HeuristicRollbackException, HeuristicMixedException {
        TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

        // no need to delist resources since that is done automatically by a JTA compliant TM
        if (commit)
            tm.commit();
        else
            tm.rollback();
    }

    private void produceMessages(XAConnection connection, String ... msgs) throws Exception{
        // Create an XA session and a message producer
        //Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Session session = connection.createXASession();
        MessageProducer producer = session.createProducer(queue);

        for (String msg : msgs)
            producer.send(session.createTextMessage(msg));

        producer.close();
        session.close();
    }

    public void produceMessages(DummyXAResource.faultType fault, String ... messages) throws Exception {
        XAConnection connection = xacf.createXAConnection();

        try
        {
            connection.start();

            // Begin some Transaction work
            XASession xaSession = connection.createXASession();
            XAResource xaRes = xaSession.getXAResource();

            //Xid xid = startTx(xaRes);
            startJTATx(new DummyXAResource(fault), xaRes);


            produceMessages(connection, messages);

            endJTATx(true);
        }
        finally
        {
             connection.close();
        }
    }

    private int consumeMessages(MessageConsumer consumer, long millis, int cnt) throws JMSException {
        for (int i = 0; i < cnt; i++) {
            TextMessage tm = (TextMessage) consumer.receive(millis);
            if (tm == null)
                return i;

            System.out.println("Message received: " + tm.getText());
        }

        return cnt;
    }

    public int consumeMessages(int cnt, long millis) throws Exception {
        XAConnection connection = xacf.createXAConnection();
        int msgCnt = 0;

        try
        {
            connection.start();

            // create an XA JMS session and enlist the corresponding XA resource within a transaction
            XASession xaSession = connection.createXASession();
            XAResource xaRes = xaSession.getXAResource();

            startJTATx(xaRes, new DummyXAResource(DummyXAResource.faultType.NONE));

            // consume 2 messages withing a transaction
            MessageConsumer xaConsumer =  xaSession.createConsumer(queue);
            msgCnt = consumeMessages(xaConsumer, millis, cnt);

            // roll back the transaction - since we consumed the messages inside a transaction they should still be available
            endJTATx(true);

            xaConsumer.close();
            xaSession.close();
        }
        finally
        {
             connection.close();
        }

        return msgCnt;
    }

    public void drainQueue() throws Exception {
        while (consumeMessages(100, 500) == 100)
           System.out.println("drained 100 messages");
    }

    public void testIsJmsWorking() throws Exception {
        XAConnection connection = xacf.createXAConnection();

        drainQueue();

        try
        {
            connection.start();

            produceMessages(connection, "hello", "world");

            // create an XA JMS session and enlist the corresponding XA resource within a transaction
            XASession xaSession = connection.createXASession();
            XAResource xaRes = xaSession.getXAResource();

            startJTATx(xaRes, new DummyXAResource(DummyXAResource.faultType.NONE));

            // consume the 2 messages within a transaction
            MessageConsumer xaConsumer =  xaSession.createConsumer(queue);

            if (consumeMessages(xaConsumer, 1000, 2) != 2)
                throw new Exception("missing messages");

            // roll back the transaction - since we consumed the messages inside a transaction they should still be available
            endJTATx(false);

            // now have another go at consuming the 2 messages but this time commit
            startJTATx(xaRes, new DummyXAResource(DummyXAResource.faultType.NONE));
            if (consumeMessages(xaConsumer, 1000, 2) != 2)
                throw new Exception("missing messages");
            // commit the message consumption
            endJTATx(true);

            // since the work was committed we expect there to be no more messages available
            if (consumeMessages(xaConsumer, 1000, 1) != 0)
                throw new Exception("additional messages");

            xaConsumer.close();
            xaSession.close();
        }
        finally
        {
             connection.close();
        }
    }

    public void testXASendWithoutError() throws Exception {
        drainQueue();
        produceMessages(DummyXAResource.faultType.NONE, "hello", "world");
    }

    public void testXARecvWithoutError() throws Exception {
        int cnt = consumeMessages(2, 500);
        if (cnt != 2)
            throw new RuntimeException("Expected 2 messages but received " + cnt);
    }

    public void testXAWithErrorPart1() throws Exception {
        // drain the queue before producing messages
        drainQueue();

        produceMessages(DummyXAResource.faultType.HALT, "hello", "world");
        throw new RuntimeException("The commit request should have halted the VM");
    }

    public void testXAWithErrorPart2() throws Exception {
        runRecoveryScan();
        int cnt = consumeMessages(2, 500);
        if (cnt != 2)
            throw new RuntimeException("Expected 2 messages but received " + cnt);

        System.out.println("Message Count after running recovery: " + cnt);
    }
}
