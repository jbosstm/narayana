/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates,
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
 * (C) 2010,
 * @author JBoss, by Red Hat.
 */
package org.jboss.jbossts.txbridge.tests.common;

import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import org.junit.Before;

import java.io.*;
import java.net.Socket;

/**
 * Common methods for crash recovery test cases.
 */
public abstract class AbstractCrashRecoveryTests extends AbstractBasicTests {

    @Before
    @Override
    public void setUp() throws Exception {
        removeContents(new File(jboss_home, "server/default/data/tx-object-store/"));
        super.setUp();
    }

    @Override
    protected boolean restartServerForEachTest() {
        return true;
    }

    protected void rebootServer() throws Exception {

        instrumentor.removeLocalState();
        File rulesFile = new File("/tmp/bar3");
        rulesFile.delete();
        instrumentor.setRedirectedSubmissionsFile(rulesFile);
        bytemanArgument.setValue(bytemanArgument.getValue()+",script:"+rulesFile.getCanonicalPath());

//        instrumentedTestSynchronization = instrumentor.instrumentClass(TestSynchronization.class);
//        instrumentedTestXAResource = instrumentor.instrumentClass(TestXAResourceRecovered.class);
        instrumentationOnServerReboot();

        manager.getServer("default").setServerConnection(null);
        Thread.sleep(2000);
        manager.startServer("default");
    }

    protected abstract void instrumentationOnServerReboot() throws Exception;


    /////////////////

    // stolen from CrashRecoveryDelays - should probably just add that to the classpath?
    // prod the recovery manager via its socket. This avoid any sleep delay.
    protected static void doRecovery() throws InterruptedException
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

                sckt.close();
            } catch(Exception e) {}
        }
   }

    // stolen from EmptyObjectStore.java
    protected static void removeContents(File directory)
    {
        if ((directory != null) &&
                directory.isDirectory() &&
                (!directory.getName().equals("")) &&
                (!directory.getName().equals("/")) &&
                (!directory.getName().equals("\\")) &&
                (!directory.getName().equals(".")) &&
                (!directory.getName().equals("..")))
        {
            File[] contents = directory.listFiles();

            for (int index = 0; index < contents.length; index++)
            {
                if (contents[index].isDirectory())
                {
                    removeContents(contents[index]);

                    //System.err.println("Deleted: " + contents[index]);
                    contents[index].delete();
                }
                else
                {
                    System.err.println("Deleted: " + contents[index]);
                    contents[index].delete();
                }
            }
        }
    }

}
