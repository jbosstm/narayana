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
 * Copyright (C) 2002,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: DemoHLS.java,v 1.2 2005/05/19 12:13:19 nmcl Exp $
 */

package com.arjuna.wsas.tests;

import com.arjuna.mw.wsas.context.Context;

import com.arjuna.mw.wsas.UserActivityFactory;

import com.arjuna.mw.wsas.common.GlobalId;

import com.arjuna.mw.wsas.activity.Outcome;
import com.arjuna.mw.wsas.activity.HLS;

import com.arjuna.mw.wsas.completionstatus.CompletionStatus;

import com.arjuna.mw.wsas.exceptions.*;

import java.util.*;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: DemoHLS.java,v 1.2 2005/05/19 12:13:19 nmcl Exp $
 * @since 1.0.
 */

public class DemoHLS implements HLS
{
    private Stack<GlobalId> _id;

    public DemoHLS()
    {
        _id = new Stack<GlobalId>();
    }

    /**
     * An activity has begun and is active on the current thread.
     */

    public void begun () throws SystemException
    {
	try
	{
	    GlobalId activityId = UserActivityFactory.userActivity().activityId();

        _id.push(activityId);

        System.out.println("DemoHLS.begun "+activityId);
	}
	catch (Exception ex)
	{
	    ex.printStackTrace();
	}
    }

    /**
     * The current activity is completing with the specified completion status.
     *
     * @return The result of terminating the relationship of this HLS and
     * the current activity.
     */

    public Outcome complete (CompletionStatus cs) throws SystemException
    {
	try
	{
	    System.out.println("DemoHLS.complete ( "+cs+" ) " + UserActivityFactory.userActivity().activityId());
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
    System.out.println("DemoHLS.suspended");
    }	

    /**
     * The activity has been resumed on the current thread.
     */

    public void resumed () throws SystemException
    {
    System.out.println("DemoHLS.resumed");
    }	

    /**
     * The activity has completed and is no longer active on the current
     * thread.
     */

    public void completed () throws SystemException
    {
        try {
        System.out.println("DemoHLS.completed "+ UserActivityFactory.userActivity().activityId());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (!_id.isEmpty()) {
            _id.pop();
        }
    }

    /**
     * The HLS name.
     */

    public String identity () throws SystemException
    {
	return "DemoHLS";
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
     * @return a context object or null if no augmentation is necessary.
     */

    public Context context () throws SystemException
    {
        if (_id.isEmpty()) {
            throw new SystemException("request for context when inactive");
        }
    try {
        System.out.println("DemoHLS.context "+ UserActivityFactory.userActivity().activityId());
    } catch (Exception ex) {
        ex.printStackTrace();
    }

        return new DemoSOAPContextImple(identity() + "_" + _id.size());
    }

}
