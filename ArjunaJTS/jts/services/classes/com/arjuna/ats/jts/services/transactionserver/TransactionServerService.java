/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.jts.services.transactionserver;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.jts.TransactionServer;

import org.tanukisoftware.wrapper.WrapperListener;
import org.tanukisoftware.wrapper.WrapperManager;



public class TransactionServerService implements WrapperListener
{
    private final static int FAILED_TO_START_RETURN_CODE = 1;
    private final static int LICENCE_ERROR_RETURN_CODE = 2;

    /**
     * Called when the service is started.
     * @param args The arguments
     * @return The exit code to return if the task didn't start successfully, otherwise null.
     */
    public Integer start(final String[] args)
    {
        Integer returnCode = null;

        try
        {
            new Thread()
            {
                public void run()
                {
                    TransactionServer.main(args);
                }
            }.start();
        }
        catch (Throwable e)
        {
            e.printStackTrace(System.err);
            returnCode = Integer.valueOf(FAILED_TO_START_RETURN_CODE);
        }

        return returnCode;
    }

    /**
     * Called when the service is being asked to stop.
     * @param exitCode The suggested exit code
     * @return The exit code this service should return.
     */
    public int stop(int exitCode)
    {
        return exitCode;
    }

    /**
     * Passes events to the service
     * @param eventCode
     */
    public void controlEvent(int eventCode)
    {
        if ( ( eventCode == WrapperManager.WRAPPER_CTRL_LOGOFF_EVENT ) &&
             ( WrapperManager.isLaunchedAsService() ) )
        {
            // Ignore
        }
        else
        {
            WrapperManager.stop( 0 );
        }
    }

    public static void main(String[] args)
    {
        WrapperManager.start(new TransactionServerService(), args);
    }
}