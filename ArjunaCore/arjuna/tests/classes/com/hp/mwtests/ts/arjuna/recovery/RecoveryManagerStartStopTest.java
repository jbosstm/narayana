/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors 
 * as indicated by the @author tags. 
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
 * (C) 2009,
 * @author JBoss Inc.
 */

package com.hp.mwtests.ts.arjuna.recovery;

import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.arjuna.common.recoveryPropertyManager;

import java.net.Socket;
import java.net.InetAddress;
import java.net.SocketException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.LinkedList;

import org.junit.Test;
import static org.junit.Assert.*;
import org.jboss.byteman.rule.helper.Helper;
import org.jboss.byteman.rule.Rule;

/**
 * test to ensure that the recovery manager cleans up all its threads when terminated
 */
public class RecoveryManagerStartStopTest
{
    @Test
    public void testStartStop() throws Exception
    {
        recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryPort(4712);

        // check how many threads there are running

        ThreadGroup thg = Thread.currentThread().getThreadGroup();
        int activeCount = thg.activeCount();

        dumpThreadGroup(thg, "Before recovery manager create");

        RecoveryManager.delayRecoveryManagerThread();
        RecoveryManager manager = RecoveryManager.manager(RecoveryManager.INDIRECT_MANAGEMENT);

        dumpThreadGroup(thg, "Before recovery manager initialize");

        manager.initialize();

        dumpThreadGroup(thg, "Before recovery manager start periodic recovery thread");

        manager.startRecoveryManagerThread();

        dumpThreadGroup(thg, "Before recovery manager client create");

        // Thread.sleep(1000);

        // we need to open several connections to the recovery manager listener service and then
        // ensure they get closed down

        addRecoveryClient();
        addRecoveryClient();

        dumpThreadGroup(thg, "Before recovery manager terminate");

        manager.terminate();

        // ensure the client threads get killed

        ensureRecoveryClientsTerminated();

        dumpThreadGroup(thg, "After recovery manager terminate");

        int newActiveCount = thg.activeCount();

        assertEquals(activeCount, newActiveCount);
    }

    private void ensureRecoveryClientsTerminated()
    {
        // check that any threads added to talk to the recovery listener get their sockets closed

        for (RecoveryManagerStartStopTestThread client : clients) {
            try {
                client.join();
            } catch (InterruptedException e) {
                // do nothing
            }
            assertFalse(client.failed());
        }
    }

    private void addRecoveryClient()
    {
        // open a connection to the recovery listener service in a new thread and ensure that the
        // thread is terminated by having its socket closed.

        RecoveryManagerStartStopTestThread client = new RecoveryManagerStartStopTestThread();
        clients.add(client);
        client.start();
        client.ensureStarted();
    }

    private void dumpThreadGroup(ThreadGroup thg, String header)
    {
        int activeCount = thg.activeCount();
        Thread[] threads = new Thread[activeCount];
        int reported = thg.enumerate(threads);

        System.out.println(header);
        System.out.println("Thread count == " + activeCount);
        for (int i = 0; i < reported; i++) {
            System.out.println("Thread[" + i + "] == " + threads[i].getName());
        }

        System.out.flush();
    }

    private List<RecoveryManagerStartStopTestThread> clients = new ArrayList<RecoveryManagerStartStopTestThread>();

    private static class RecoveryManagerStartStopTestThread extends Thread
    {
        private boolean failed = true;
        private boolean started = false;
        private boolean stopped = false;

        public RecoveryManagerStartStopTestThread()
        {
            super("Recovery Listener Client");
        }

        public boolean failed()
        {
            return failed;
        }

        public void run()
        {
            BufferedReader fromServer = null;
            Socket connectorSocket = null;
            // get a socket connected to the listener
            // don't write anything just sit on a read until the socket is closed
            try {
                String host;
                int port;

                host = InetAddress.getLocalHost().getHostName();
                
                port = recoveryPropertyManager.getRecoveryEnvironmentBean().getRecoveryPort();

                System.out.println("client atempting to connect to host " + host + " port " + port);
                System.out.flush();

                try
                {
                    connectorSocket = new Socket(host, port);
                }
                catch (final Exception ex)
                {
                    // in case local host name bind fails (e.g., on Mac OS)
                    
                    host = "127.0.0.1";
                    
                    connectorSocket = new Socket(host, port);
                }

                System.out.println("connected!!!");
                System.out.flush();

                fromServer = new BufferedReader(new InputStreamReader(connectorSocket.getInputStream()));
            } catch (Exception e) {

                System.out.println("Failed to set up listener input stream!!!");
                e.printStackTrace();
                System.out.flush();

                return;
            } finally {
                notifyStarted();
            }

            try {
                String result = fromServer.readLine();
                if (result == null || result.equals("")) {
                    System.out.println("Recovery Listener Client got empty string from readline() as expected");
                    System.out.flush();
                    failed = false;
                }
            } catch (SocketException e) {
                if (!connectorSocket.isClosed()) {
                    try {
                        connectorSocket.close();
                    } catch (IOException e1) {
                        // ignore
                    }
                }
                System.out.println("Recovery Listener Client got socket exception as expected");
                e.printStackTrace();
                System.out.flush();
                failed = false;
            } catch (IOException e) {
                if (!connectorSocket.isClosed()) {
                    System.out.println("Recovery Listener Client got non socket IO exception without socket being closed");
                    try {
                        connectorSocket.close();
                    } catch (IOException e1) {
                        // ignore
                    }
                } else {
                    System.out.println("Recovery Listener Client got IO exception under readline() as expected");
                    failed = false;
                }
                e.printStackTrace();
                System.out.flush();
            } catch (Exception e) {
                System.out.println("Recovery Listener Client got non IO exception");
                e.printStackTrace();
                System.out.flush();
            }
        }

        public synchronized void notifyStarted()
        {
            started = true;
            notify();
        }

        public synchronized void ensureStarted() {
            while (!started) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }
    }

    /**
     * helper class for use in byteman rules to ensure that the listener class
     * actually joins the connections it closes -- it does not need to as far as the
     * TS code is concerned but we cannot check that the test has run correctly without
     * adding this extra join
     */

    public static class JoinHelper extends Helper
    {
        public JoinHelper(Rule rule)
        {
            super(rule);
        }

        public boolean createJoin(Object key, int max)
        {
            if (max <= 0) {
                return false;
            }

            synchronized(joinerMap) {
                if (joinerMap.get(key) != null) {
                    return false;
                }
                joinerMap.put(key, new Joiner(max));
            }

            return true;
        }

        public boolean isJoin(Object key, int max)
        {
            synchronized(joinerMap) {
                Joiner joiner = joinerMap.get(key);

                if (joiner == null || joiner.getMax() != max) {
                    return false;
                }
            }

            return true;
        }

        public boolean joinEnlist(Object key)
        {
            Joiner joiner;
            synchronized (joinerMap)
            {
                joiner = joinerMap.get(key);
            }

            if (joiner == null) {
                return false;
            }

            Thread current = Thread.currentThread();

            switch (joiner.addChild(current)) {
                case DUPLICATE:
                case EXCESS:
                {
                    // failed to add  child
                    return false;
                }
                case ADDED:
                case FILLED:
                {
                    // added child but parent was not waiting so leave joiner in the map for parent to find
                    return true;
                }
                case DONE:
                default:
                {
                    // added child and parent was waiting so remove joiner from map now
                    synchronized (joinerMap) {
                        joinerMap.remove(joiner);
                    }
                    return true;
                }
            }
        }

        public boolean joinWait(Object key, int count)
        {
            Joiner joiner;
            synchronized (joinerMap)
            {
                joiner = joinerMap.get(key);
            }

            if (joiner == null || joiner.getMax() != count) {
                return false;
            }

            Thread current = Thread.currentThread();

            if (joiner.joinChildren(current)) {
                // successfully joined all child threads so remove joiner form map
                synchronized (joinerMap) {
                    joinerMap.remove(joiner);
                }
                return true;
            } else {
                // hmm, another thread must have done the join so leave it do the remove
                return true;
            }
        }

    }

    private static HashMap<Object, Joiner> joinerMap = new HashMap<Object, Joiner>();

    /**
     * status values returned from child add method
     */
    private enum Status {
        /**
         * a DUPLICATE status is returned when a child fails to add itself to the join list because it is already present
         */
        DUPLICATE,
        /**
         * an EXCESS status is returned when a child fails to add itself to a join list because it already contains the
         * expected number of children
         */
        EXCESS,
        /**
         * an ADDED status is returned when a child successfully adds itself to the join list but without reaching
         * the expected number of children
         */
        ADDED,
        /**
         * a FILLED status is returned when a child successfully adds itself to the join list reaching the expected
         * number of children but there is no parent thread waiting for the children
         */
        FILLED,
        /**
         * a DONE  status is returned when a child successfully adds itself to the join list reaching the expected
         * number of children and there is a parent thread waiting for the children
         */
        DONE
    }

    private static class Joiner
    {

        private List<Thread> children;
        private int max;
        private Thread parent;

        public Joiner(int max)
        {
            this.max = max;
            this.children = new LinkedList<Thread>();
            this.parent =  null;
        }

        public int getMax()
        {
            return max;
        }

        public synchronized Status addChild(Thread thread)
        {
            if (children.contains(thread)) {
                return Status.DUPLICATE;
            }

            int size = children.size();

            if (size == max) {
                return Status.EXCESS;
            }

            children.add(thread);
            size++;

            if (size == max) {
                if (parent ==  null) {
                    return Status.FILLED;
                } else {
                    notifyAll();
                    return Status.DONE;
                }
            }
            return Status.ADDED;
        }

        public boolean joinChildren(Thread thread)
        {
            synchronized (this) {
                if (parent != null) {
                    return false;
                }
                parent = thread;
                while (children.size() < max) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        // do nothing
                    }
                }
            }
            // since we are the parent and the waiting is over we don't need to stay synchronized
            for (int i = 0; i < max;) {
                Thread child = children.get(i);
                try {
                    child.join();
                } catch (InterruptedException e) {
                    // try again
                    break;
                }
                i++;
            }
            return true;
        }
    }
}
