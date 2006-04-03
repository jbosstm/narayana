/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
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
 * $Id: OTSManager.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.jts;

import com.arjuna.common.util.propertyservice.PropertyManager;

import com.arjuna.ats.jts.common.Environment;
import com.arjuna.ats.jts.common.jtsPropertyManager;
import com.arjuna.ats.jts.logging.*;

import com.arjuna.orbportability.*;

import com.arjuna.common.util.logging.*;

import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.coordinator.TransactionReaper;

import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;
import com.arjuna.ats.internal.jts.orbspecific.ControlImple;
import com.arjuna.ats.internal.jts.orbspecific.TransactionFactoryImple;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.utils.Helper;

import org.omg.CosTransactions.*;

import com.arjuna.ArjunaOTS.*;

import java.io.PrintStream;

import org.omg.CORBA.SystemException;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.UNKNOWN;
import org.omg.CORBA.UserException;
import org.omg.CORBA.ORBPackage.InvalidName;
import java.io.IOException;

/**
 * This class is essentially here for convenience purposes, and until
 * all ORBs provide a means to set their initial references for Current
 * and the TransactionFactory.
 *
 * @author Mark Little (mark_little@hp.com)
 * @version $Id: OTSManager.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class OTSManager
{

    /**
     * @return the Current object.
     */

    public static org.omg.CosTransactions.Current get_current () throws org.omg.CORBA.SystemException
    {
	return com.arjuna.ats.internal.jts.OTSImpleManager.get_current();
    }

    /**
     * @return the TransactionFactory object implementation. This has the
     * advantage of not needing to register the object withm the ORB, which
     * can affect performance.
     */
    
    public static TransactionFactoryImple factory () throws org.omg.CORBA.SystemException
    {
	return com.arjuna.ats.internal.jts.OTSImpleManager.factory();
    }

    /**
     * @return the TransactionFactory object.
     */

    public static TransactionFactory get_factory () throws org.omg.CORBA.SystemException
    {
	return com.arjuna.ats.internal.jts.OTSImpleManager.get_factory();
    }

    /**
     * Used to destroy a transaction control. Normally garbage collection
     * will take care of this, but in certain circumstances (e.g., a context
     * is propagated implicitly but we do not use interposition and we
     * have to manufacture a local control object) it is not possible for the
     * OTS to know when controls can be removed. This is a problem with the
     * specification and CORBA in general.
     */

    public static void destroyControl (ControlImple control) throws ActiveTransaction, ActiveThreads, BadControl, Destroyed, SystemException
    {
	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
						 com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "OTS::destroyControl ( "+control+" )");
	}
	
	if (control == null)
	    throw new BadControl();

	/*
	 * Just in case control is a top-level transaction, and has
	 * been registered with the reaper, we need to get it removed.
	 *
	 * Don't bother if the reaper has not been created.
	 */
    
	if (TransactionReaper.transactionReaper() != null)
	{
	    Coordinator coord = null;
	
	    try
	    {
		coord = control.get_coordinator();
	    }
	    catch (Exception e)
	    {
		coord = null;  // nothing else we can do!
	    }

	    if (coord != null)
	    {
		try
		{
		    if (coord.is_top_level_transaction())
		    {
			/*
			 * Transaction is local, but was registered as
			 * a Control. If this is a performance hit then
			 * add explicit add/removes for local instances.
			 */

			if (jtsLogger.logger.isDebugEnabled())
			{
			    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
						   com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "OTS::destroyControl - removing control from reaper.");
			}

			TransactionReaper.transactionReaper().remove(control.get_uid());
		    }
		}
		catch (Exception e)
		{
		}

		coord = null;
	    }
	}
	
	/*
	 * Watch out for conflicts with multiple threads deleting
	 * the same control!
	 */

	/*
	 * If local, then delete it here, rather than
	 * calling the destroy method.
	 *
	 * Possible problem if a local factory is being accessed
	 * remotely?
	 */

	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
						 com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "OTS::destroyControl - local transaction: "+control.get_uid());
	}

	control.destroy();
	    
	control = null;
    }

    /**
     * Destroy the transaction control.
     */
	
    public static void destroyControl (Control control) throws ActiveTransaction, ActiveThreads, BadControl, Destroyed, SystemException
    {
	if (control == null)
	    throw new BadControl();
	
	ControlImple lCont = Helper.localControl(control);

	if (lCont != null)
	{
	    destroyControl(lCont);
	}
	else
	{
	    /*
	     * Just in case control is a top-level transaction, and has
	     * been registered with the reaper, we need to get it removed.
	     *
	     * Don't bother if the reaper has not been created.
	     */
    
	    if (TransactionReaper.transactionReaper() != null)
	    {
		Coordinator coord = null;
	
		try
		{
		    coord = control.get_coordinator();
		}
		catch (Exception e)
		{
		    coord = null;  // nothing else we can do!
		}

		if (coord != null)
		{
		    try
		    {
			if (coord.is_top_level_transaction())
			    TransactionReaper.transactionReaper().remove(control);
		    }
		    catch (Exception e)
		    {
		    }

		    coord = null;
		}
	    }
    
	    /*
	     * Watch out for conflicts with multiple threads deleting
	     * the same control!
	     */

	    if (jtsLogger.logger.isDebugEnabled())
	    {
		jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
						     com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "OTS::destroyControl - remote control.");
	    }

	    /*
	     * Remote transaction, so memory management is different!
	     */
	
	    ActionControl action = null;
	    
	    try
	    {
		action = com.arjuna.ArjunaOTS.ActionControlHelper.narrow(control);

		if (action == null)
		    throw new BAD_PARAM();
	    }
	    catch (Exception e)
	    {
		action = null;
	    }

	    if (action != null)
	    {
		if (jtsLogger.logger.isDebugEnabled())
		{
		    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
							 com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "OTS::destroyControl - Arjuna control.");
		}

		/*
		 * Is an Arjuna control, so we can call destroy on it?
		 */

		action.destroy();
		
		action = null;
		control = null;
	    }
	    else
	    {
		/*
		 * Just call release on the control.
		 *
		 * We could throw a BadControl exception, but
		 * what would that do for the programmer?
		 */

		control = null;
	    }
	}
    }

    public static final void setLocalSlotId (int slotId)
    {
	_localSlotId = slotId;
    }
    
    public static final int getLocalSlotId ()
    {
	return _localSlotId;
    }

    public static final void setReceivedSlotId (int slotId)
    {
	_receivedSlotId = slotId;
    }
    
    public static final int getReceivedSlotId ()
    {
	return _receivedSlotId;
    }

    public static final void setORB (com.arjuna.orbportability.ORB theOrb)
    {
	com.arjuna.ats.internal.jts.ORBManager.setORB(theOrb);
    }
    
    public static final void setPOA (com.arjuna.orbportability.OA thePoa)
    {
	com.arjuna.ats.internal.jts.ORBManager.setPOA(thePoa);
    }
    
    public static final int serviceId = 0xDEADBEEF;

    private static int _localSlotId = -1;
    private static int _receivedSlotId = -1;

}
