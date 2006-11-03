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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2003,
 *
 * Arjuna Technologies Ltd,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: WorkerService.java 2342 2006-03-30 13:06:17Z  $
 */
 
package com.arjuna.ats.internal.arjuna.recovery;

import com.arjuna.ats.arjuna.logging.FacilityCode ;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.recovery.Service;

import java.io.*;

/**
 * @message com.arjuna.ats.internal.arjuna.recovery.WorkerService_1 [com.arjuna.ats.internal.arjuna.recovery.WorkerService_1] - Other Exception:
 * @message com.arjuna.ats.internal.arjuna.recovery.WorkerService_2 [com.arjuna.ats.internal.arjuna.recovery.WorkerService_2] - IOException
 * @message com.arjuna.ats.internal.arjuna.recovery.WorkerService_3 [com.arjuna.ats.internal.arjuna.recovery.WorkerService_3] RecoveryManager scan scheduled to begin.
 * @message com.arjuna.ats.internal.arjuna.recovery.WorkerService_4 [com.arjuna.ats.internal.arjuna.recovery.WorkerService_4] RecoveryManager scan completed.
 */ 

public class WorkerService implements Service
{
    public WorkerService (PeriodicRecovery pr)
    {
	_periodicRecovery = pr;
    }
    
    public synchronized void doWork (InputStream is, OutputStream os) throws IOException
    {
	BufferedReader in  = new BufferedReader(new InputStreamReader(is));
	PrintWriter out = new PrintWriter(new OutputStreamWriter(os));

	try
	{
	    String request = in.readLine();

        if (request.equals("PING"))
        {
            out.println("PONG");
        }
        else
	    if (request.equals("SCAN") || (request.equals("ASYNC_SCAN")))
	    {
		_periodicRecovery.interrupt();

		tsLogger.arjLogger.info("com.arjuna.ats.internal.arjuna.recovery.WorkerService_3");

		if (request.equals("SCAN"))
		{
		    try
		    {
			wait();
		    }
		    catch (Exception ex)
		    {
			tsLogger.arjLogger.info("com.arjuna.ats.internal.arjuna.recovery.WorkerService_4");
		    }
		}

		out.println("DONE");
	    }
	    else
		out.println("ERROR");

	    out.flush();
	}
	catch (IOException ex)
	{
	    if (tsLogger.arjLoggerI18N.isWarnEnabled())
		tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.recovery.WorkerService_2");
	}
	catch ( Exception ex )
	{
	    if (tsLogger.arjLoggerI18N.isWarnEnabled())
	    {
		tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.recovery.WorkerService_1", new Object[]{ex});
	    }
	}
    }

    public synchronized void signalDone ()
    {
	try
	{
	    notifyAll();
	}
	catch (Exception ex)
	{
	}
    }
    
    private PeriodicRecovery _periodicRecovery = null;

}

