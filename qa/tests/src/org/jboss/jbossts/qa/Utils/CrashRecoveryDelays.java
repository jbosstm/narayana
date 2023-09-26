/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.Utils;

import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.common.arjPropertyManager;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Utility class to hold delay functions for crash recovery tests.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2009-09
 */
public class CrashRecoveryDelays
{
    public static void awaitRecoveryArjunaCore() throws InterruptedException {
        doRecovery();
    }

    public static void awaitRecoveryCR05() throws InterruptedException {
        awaitRecovery(2, 1);
    }

    public static void awaitRecoveryCR07(int num_clients) throws InterruptedException {
        awaitRecovery(1, num_clients);
    }

    public static void awaitRecoveryCR08(int num_clients) throws InterruptedException {
        // due to the way CachedRecoveredTransaction/StatusChecker handle TRANSIENT some of these tests need two passes
        awaitRecovery(2, num_clients);
    }

    public static void awaitRecoveryCR09() throws InterruptedException {
        awaitRecovery(1, 1);
    }

    public static void awaitRecoveryCR10() throws InterruptedException {
        awaitRecovery(1, 1);
    }

    public static void awaitRecoveryCR12() throws InterruptedException {
        awaitRecovery(2, 1);
    }

/*
    // old, slow method - sleep long enough to ensure periodic cr will have occurred.
    private static void awaitRecovery(int num_cycles, int num_clients) throws InterruptedException
    {
        // Note: this assumes the client is running with the same config as the rec mgr process.
        RecoveryEnvironmentBean recoveryEnvironmentBean = recoveryPropertyManager.getRecoveryEnvironmentBean();
        int recoveryCycleTime = recoveryEnvironmentBean.getPeriodicRecoveryPeriod()+recoveryEnvironmentBean.getRecoveryBackoffPeriod();
        // this timing may be a little tight on some older/busy systems
        // consider increasing the per-client fudge factor a bit...
        int delay = ((num_cycles*recoveryCycleTime)+(num_clients*10))*1000;
        System.out.println("Sleeping for " + delay + " ms.");
        Thread.sleep(delay);
    }
*/

    private static void awaitRecovery(int num_cycles, int num_clients) throws InterruptedException
    {
        num_cycles *= getDelayFactor(); // on some machines we may need to wait longer for recovery to complete

        for(int i = 0; i < num_cycles; i++) {
            if (i != 0)
                awaitReplayCompletion(5); // add a delay to accommodate slower test machines
            doRecovery();
        }
    }

    // prod the recovery manager via its socket. This avoid any sleep delay.
    private static void doRecovery() throws InterruptedException
    {
        int port = recoveryPropertyManager.getRecoveryEnvironmentBean().getRecoveryPort();
        String host = recoveryPropertyManager.getRecoveryEnvironmentBean().getRecoveryAddress();

        BufferedReader in = null;
        PrintStream out = null;
        Socket sckt = null;

        try
        {
            sckt = new Socket(host,port);

            in = new BufferedReader(new InputStreamReader(sckt.getInputStream()));
            out = new PrintStream(sckt.getOutputStream());

            // Output ping message
            out.println("SCAN");
            out.flush();

            // Receive pong message
            String inMessage = in.readLine();

            if(!inMessage.equals("DONE")) {
                System.err.println("Recovery failed with message: "+inMessage);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            try {
                if ( in != null )
                {
                    in.close();
                }

                if ( out != null )
                {
                    out.close();
                }

                if (sckt != null)
                    sckt.close();
            } catch(Exception e) {}
        }
   }

    /////////////////

    public static void awaitReplayCompletionCR01() throws InterruptedException {
        awaitReplayCompletion(5); // was 10
    }

    public static void awaitReplayCompletionCR04() throws InterruptedException {
        awaitReplayCompletion(5); // was 10
    }

    public static void awaitReplayCompletionCR02() throws InterruptedException {
        awaitRecovery(1, 1);
        //awaitReplayCompletion(5); // was 60
    }

    public static void awaitReplayCompletionCR02(int scaleFactor) throws InterruptedException {
        awaitReplayCompletion(5 * scaleFactor); // was 60
    }

    public static void awaitReplayCompletionCR05() throws InterruptedException {
        awaitRecovery(1, 1);
        //awaitReplayCompletion(10); // was 60
    }

    public static void awaitReplayCompletionCR05(int scaleFactor) throws InterruptedException {
        awaitReplayCompletion(10 * scaleFactor); // was 60
    }

    public static void awaitReplayCompletionCR06() throws InterruptedException {
        awaitReplayCompletion(10); // was 5
    }

    private static void awaitReplayCompletion(int seconds) throws InterruptedException
    {
        Thread.sleep(getDelayFactor() * seconds * 1000);
    }

    public static int getDelayFactor() {
        if (delayFactor < 0) {
            delayFactor = arjPropertyManager.getCoreEnvironmentBean().getTimeoutFactor();

            if (delayFactor <= 0)
                delayFactor = 1;

            System.out.printf("Using timeout delay factor of %d%n", delayFactor);
        }

        return delayFactor;
    }

    private static int delayFactor = -1;
}