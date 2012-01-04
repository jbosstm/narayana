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
/*
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: OTSThread.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.jts.thread;

import org.omg.CosTransactions.Control;

import com.arjuna.ats.arjuna.exceptions.FatalError;
import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;
import com.arjuna.ats.jts.logging.jtsLogger;

/**
 * Create an OTS transactional thread. When the thread is created it is
 * automatically registered with the current transaction, and removed
 * when the transaction terminates.
 *
 * @author Mark Little (mark_little@hp.com)
 * @version $Id: OTSThread.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.2.4.
 */

public class OTSThread extends Thread
{

    /*
     * Can't use finalize since it may be called by
     * some other thread than this one.
     */

    public void terminate ()
    {
	try
	{
	    CurrentImple current = OTSImpleManager.current();

	    if (current != null)
	    {
		Control c = current.suspend();

		c = null;
	    }
	}
	catch (Exception e)
	{
	}
    }

    public void run ()
    {
	if (_currentControl != null)
	{
	    try
	    {
		CurrentImple current = OTSImpleManager.current();

		if (current != null)
		{
		    current.resume(_currentControl);
		    current = null;
		}
	    }
	    catch (Exception e)
	    {
            jtsLogger.i18NLogger.warn_thread_resumefailed( "OTSThread.run", e);

		throw new FatalError("OTSThread.run - "+jtsLogger.i18NLogger.get_thread_resumefailederror(), e);
	    }

	    _currentControl = null;
	}
    }

    protected OTSThread ()
    {
	try
	{
	    CurrentImple current = OTSImpleManager.current();

	    if (current != null)
	    {
		_currentControl = current.get_control();
		current = null;
	    }
	}
	catch (Exception e)
	{
	    _currentControl = null;
	}
    }

    private Control _currentControl;

}
