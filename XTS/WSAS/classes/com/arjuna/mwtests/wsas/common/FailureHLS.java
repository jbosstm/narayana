/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
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
 * Copyright (C) 2002,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: FailureHLS.java,v 1.2 2005/05/19 12:13:19 nmcl Exp $
 */

package com.arjuna.mwtests.wsas.common;

import com.arjuna.mw.wsas.context.Context;

import com.arjuna.mw.wsas.UserActivityFactory;

import com.arjuna.mw.wsas.common.GlobalId;

import com.arjuna.mw.wsas.activity.Outcome;
import com.arjuna.mw.wsas.activity.HLS;

import com.arjuna.mw.wsas.completionstatus.CompletionStatus;

import com.arjuna.mw.wsas.exceptions.*;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: FailureHLS.java,v 1.2 2005/05/19 12:13:19 nmcl Exp $
 * @since 1.0.
 */

public class FailureHLS implements HLS
{

    public static final int BEGUN_FAIL = 0;
    public static final int COMPLETE_FAIL = 1;
    public static final int SUSPENDED_FAIL = 2;
    public static final int RESUMED_FAIL = 3;
    public static final int COMPLETED_FAIL = 4;
    public static final int CONTEXT_FAIL = 5;
    public static final int NO_FAIL = 10;
    
    public FailureHLS ()
    {
	this(FailureHLS.NO_FAIL);
    }

    public FailureHLS (int failPoint)
    {
	_failPoint = failPoint;
    }
    
    /**
     * An activity has begun and is active on the current thread.
     */

    public void begun () throws SystemException
    {
	if (_failPoint == FailureHLS.BEGUN_FAIL)
	    throw new SystemException();
	
	try
	{
	    GlobalId activityId = UserActivityFactory.userActivity().activityId();

	    System.out.println("FailureHLS.begun "+activityId);
	}
	catch (Exception ex)
	{
	    ex.printStackTrace();
	}
    }

    /**
     * The current activity is completing with the specified completion status.
     *
     * @param CompletionStatus cs The completion status to use.
     *
     * @return The result of terminating the relationship of this HLS and
     * the current activity.
     */

    public Outcome complete (CompletionStatus cs) throws SystemException
    {
	if (_failPoint == FailureHLS.COMPLETE_FAIL)
	    throw new SystemException();

	try
	{
	    System.out.println("FailureHLS.complete ( "+cs+" ) "+UserActivityFactory.userActivity().activityId());
	}
	catch (Exception ex)
	{
	    ex.printStackTrace();
	}

	return null;
    }	

    /**
     * The activity has been suspended. How does the HLS know which activity
     * has been suspended? It must remember what its notion of current is.
     */

    public void suspended () throws SystemException
    {
	if (_failPoint == FailureHLS.SUSPENDED_FAIL)
	    throw new SystemException();

	System.out.println("FailureHLS.suspended");
    }	

    /**
     * The activity has been resumed on the current thread.
     */

    public void resumed () throws SystemException
    {
	if (_failPoint == FailureHLS.RESUMED_FAIL)
	    throw new SystemException();

	System.out.println("FailureHLS.resumed");
    }	

    /**
     * The activity has completed and is no longer active on the current
     * thread.
     */

    public void completed () throws SystemException
    {
	if (_failPoint == FailureHLS.COMPLETED_FAIL)
	    throw new SystemException();

	try
	{
	    System.out.println("FailureHLS.completed "+UserActivityFactory.userActivity().activityName());
	}
	catch (NoActivityException ex)
	{
	    ex.printStackTrace();
	}
    }		

    /**
     * The HLS name.
     */

    public String identity () throws SystemException
    {
	return "FailureHLS";
    }

    /**
     * The activity service maintains a priority ordered list of HLS
     * implementations. If an HLS wishes to be ordered based on priority
     * then it can return a non-negative value: the higher the value,
     * the higher the priority and hence the earlier in the list of HLSes
     * it will appear (and be used in).
     *
     * @return a positive value for the priority for this HLS, or zero/negative
     * if the order is not important.
     */

    public int priority () throws SystemException
    {
	return 0;
    }

    /**
     * Return the context augmentation for this HLS, if any on the current
     * activity.
     *
     * @param ActivityHierarchy current The handle on the current activity
     * hierarchy. The HLS may use this when determining what information to
     * place in its context data.
     *
     * @return a context object or null if no augmentation is necessary.
     */

    public Context context () throws SystemException
    {
	if (_failPoint == FailureHLS.CONTEXT_FAIL)
	    throw new SystemException();

	return new DemoSOAPContextImple(identity());
    }

    private int _failPoint;
    
}
