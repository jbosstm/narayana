package com.hp.mwtests.ts.arjuna.recovery;

import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.internal.arjuna.recovery.PeriodicRecovery;

import java.net.Socket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * test to ensure that the recovery manager cleans up all its threads when terminated
 */
public class RecoveryManagerStartStopTest
{
    @Test
    public void testStartStop() throws Exception
    {
        // check how many threads there are running

        ThreadGroup thg = Thread.currentThread().getThreadGroup();
        int activeCount = thg.activeCount();

        dumpThreadGroup(thg, "Before recovery manager create");

        RecoveryManager.delayRecoveryManagerThread();
        RecoveryManager manager = RecoveryManager.manager(RecoveryManager.INDIRECT_MANAGEMENT);

        dumpThreadGroup(thg, "Before recovery manager initialize");

        manager.initialize();

        // give threads a chance to start

        Thread.sleep(2000);

        dumpThreadGroup(thg, "Before recovery manager start periodic recovery thread");

        manager.startRecoveryManagerThread();

        dumpThreadGroup(thg, "Before recovery manager client create");

        // we need to open several connections to the recovery manager listener service and then
        // ensure they get closed down

        addRecoveryClient();
        addRecoveryClient();

        Thread.sleep(5000);

        dumpThreadGroup(thg, "Before recovery manager terminate");

        manager.terminate();

        // ensure the client threads get killed

        ensureRecoveryClientsTerminated();

        // ensure there are no extra threads running

        Thread.sleep(2000);

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
                ServerSocket socket = PeriodicRecovery.getServerSocket();
                InetAddress address;
                String host;
                int port;

                address = socket.getInetAddress();

                host = InetAddress.getLocalHost().getHostName();

                port = PeriodicRecovery.getServerSocket().getLocalPort();

                System.out.println("client atempting to connect to host " + host + " port " + port);
                System.out.flush();

                connectorSocket = new Socket(host, port);

                System.out.println("connected!!!");
                System.out.flush();

                fromServer = new BufferedReader(new InputStreamReader(connectorSocket.getInputStream()));
            } catch (Exception e) {
                System.out.println("Failed to set up listener input stream!!!");
                e.printStackTrace();
                System.out.flush();

                return;
            }

            try {
                String result = fromServer.readLine();
                if (result == null || result.equals("")) {
                    System.out.flush();
                    System.out.println("Recovery Listener Client got empty string from readline() as expected");
                    failed = false;
                }
            } catch (IOException e) {
                if (!connectorSocket.isClosed()) {
                    System.out.println("Recovery Listener Client got IO exception without socket being closed");
                    System.out.flush();
                    e.printStackTrace();
                    try {
                        connectorSocket.close();
                    } catch (IOException e1) {
                        // ignore
                    }
                } else {
                    System.out.flush();
                    System.out.println("Recovery Listener Client got IO exception under readline() as expected");
                    failed = false;
                }
            } catch (Exception e) {
                System.out.println("Recovery Listener Client got non IO exception");
                e.printStackTrace();
                System.out.flush();
            }
        }
    }
}
