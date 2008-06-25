/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package com.arjuna.ats.jts.services.transactionserver;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.jts.TransactionServer;

import org.tanukisoftware.wrapper.WrapperListener;
import org.tanukisoftware.wrapper.WrapperManager;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003, 2004
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: TransactionServerService.java 2342 2006-03-30 13:06:17Z  $
 */

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
            returnCode = new Integer(FAILED_TO_START_RETURN_CODE);
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
