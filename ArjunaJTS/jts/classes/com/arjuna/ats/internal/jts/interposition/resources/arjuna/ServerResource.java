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
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: ServerResource.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts.interposition.resources.arjuna;

import com.arjuna.ats.jts.logging.*;

import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.coordinator.Reapable;
import com.arjuna.ats.internal.arjuna.template.*;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.interposition.ServerFactory;
import com.arjuna.ats.internal.jts.orbspecific.interposition.ServerControl;
import com.arjuna.ats.internal.jts.orbspecific.interposition.resources.arjuna.*;

import com.arjuna.common.util.logging.*;

import org.omg.CosTransactions.*;
import java.io.PrintWriter;

import org.omg.CORBA.SystemException;

/**
 * The base class from which interposed resources derive.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ServerResource.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class ServerResource implements ListElement
{

    /*
     * Assume only one thread can delete an object!
     */

public void finalize ()
    {
	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.DESTRUCTORS, VisibilityLevel.VIS_PUBLIC,
					       com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ServerResource.finalize ( "+_theUid+" )");
	}

	tidyup();
    }

    /*
     * This is called to allow lazy interposition of resources.
     * Since some implementations of interposition may not need
     * this, its default is to do nothing.
     */

public boolean interposeResource ()
    {
	return true;
    }

public final boolean valid ()
    {
	return _valid;
    }

public final synchronized boolean destroyed ()
    {
	return _destroyed;
    }

public final ServerControl control ()
    {
	return _theControl;
    }

    /*
     * Add transaction to this transaction's children, and set the new
     * transaction's notion of its parent to us.
     */

public final boolean addChild (ServerNestedAction c)
    {
	if (_children != null)
	{
	    if (_children.add(c))
	    {
		c.setParentHandle(this);
	    
		return true;
	    }
	}
    
	return false;
    }

public final boolean removeChild (ServerNestedAction c)
    {
	if (_children != null)
	{
	    c.setParentHandle(null);
	    
	    return _children.remove(c);
	}
	else
	    return false;
    }

public final ServerNestedAction getChild (Uid id)
    {
	if (_children != null)
	    return (ServerNestedAction) _children.lookFor(id);
	else
	    return null;
    }

public final HashList getChildren ()
    {
	return _children;
    }

    /**
     * @message com.arjuna.ats.internal.jts.interposition.resources.arjuna.notchild {0} - not my child!
     * @message com.arjuna.ats.internal.jts.interposition.resources.arjuna.nochild {0} - error, no child found!
     */

public final boolean abortChild (ServerNestedAction toAbort)
    {
	if ((_children != null) && (toAbort != null))
	{
	    ServerNestedAction child = (ServerNestedAction) _children.remove(toAbort.get_uid());
	
	    if (child != null)
	    {
		org.omg.CosTransactions.Status nestedStatus = child.otsStatus();

		if ((nestedStatus != org.omg.CosTransactions.Status.StatusRolledBack) &&
		    (nestedStatus != org.omg.CosTransactions.Status.StatusCommitted) &&
		    (nestedStatus != org.omg.CosTransactions.Status.StatusNoTransaction))
		{
		    child.rollback_subtransaction();
		}

		try
		{
		    ORBManager.getPOA().shutdownObject(child.theResource());
		}
		catch (Exception e)
		{
		}

		return true;
	    }
	    else
	    {
		if (jtsLogger.loggerI18N.isWarnEnabled())
		{
		    jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.interposition.resources.arjuna.notchild",
					      new Object[] {"ServerResource.abortChild"});
		}
	    }
	}
	else
	{
	    if (jtsLogger.loggerI18N.isWarnEnabled())
	    {
		jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.interposition.resources.arjuna.nochild",
					  new Object[] {"ServerResource.abortChild"});
	    }
	}

	return false;
    }

public synchronized final void setParentHandle (ServerResource p)
    {
	_parent = p;
    }

public synchronized final ServerResource getParentHandle ()
    {
	return _parent;
    }

public Uid get_uid ()
    {
	return _theUid;
    }

public final org.omg.CosTransactions.Status otsStatus ()
    {
	try
	{
	    if (_theControl != null)
		return _theControl.getImplHandle().get_status();
	    else
		return org.omg.CosTransactions.Status.StatusNoTransaction;
	}
	catch (Exception e)
	{
	    return org.omg.CosTransactions.Status.StatusUnknown;
	}
    }

public final String getChildren (int depth)
    {
	String children = "";
	
	if (_children != null)
	{
	    HashListIterator iter = new HashListIterator(_children);
	    ServerNestedAction childPtr = (ServerNestedAction) iter.iterate();

	    while (childPtr != null)
	    {
		children += "\n";
	    
		for (int i = 0; i < depth; i++)
		    children += " ";

		children += childPtr.get_uid();

		children += childPtr.getChildren(depth+1);
		childPtr = (ServerNestedAction) iter.iterate();
	    }
	}

	return children;
    }

protected ServerResource ()
    {
	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PROTECTED,
					       com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ServerResource::ServerResource ()");
	}

	_theControl = null;
	_children = null;
	_parent = null;
	_valid = true;
	_destroyed = false;
    }

protected ServerResource (ServerControl control)
    {
	_theControl = control;
	_children = new HashList(11);
	_theUid = control.get_uid();
	_parent = null;
	_valid = true;
	_destroyed = false;

	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PROTECTED,
					       com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ServerResource::ServerResource ( "+_theUid+" )");
	}
    }

protected void tidyup ()
    {
	if (_children != null)
	{
	    ServerNestedAction ptr = (ServerNestedAction) _children.orderedPop();

	    while (ptr != null)
	    {
		ptr.setParentHandle(null);
		
		try
		{
		    ORBManager.getPOA().shutdownObject(ptr.theResource());
		}
		catch (Exception e)
		{
		}

		ptr = (ServerNestedAction) _children.orderedPop();
	    }

	    _children = null;
	}

	if (_theControl != null)
	{
	    /*
	     * If it's a wrapper, then the control
	     * will not have been driven to commit or
	     * rollback or forget, and hence will not have destroyed
	     * itself. So, do so now.
	     */

	    if (_theControl.isWrapper())
	    {
		try
		{
		    _theControl.destroy();  // will delete itself
		}
		catch (Exception e)
		{
		}
	    }

	    _theControl = null;
	}
    }

protected ServerControl  _theControl;
protected HashList       _children;
protected Uid            _theUid;
protected ServerResource _parent;
protected boolean        _valid;
protected boolean        _destroyed;
    
}

