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
 * $Id: ContextManager.java,v 1.3 2005/05/19 12:13:16 nmcl Exp $
 */

package com.arjuna.mw.wsas.context;

import com.arjuna.mw.wsas.logging.wsasLogger;

import com.arjuna.mw.wsas.ActivityManager;
import com.arjuna.mw.wsas.ActivityManagerFactory;

import com.arjuna.mw.wsas.activity.HLS;

/**
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ContextManager.java,v 1.3 2005/05/19 12:13:16 nmcl Exp $
 * @since 1.0.
 */

public class ContextManager
{

    public ContextManager ()
    {
	_manager = ActivityManagerFactory.activityManager();
    }

    /**
     * @message com.arjuna.mw.wsas.context.ContextManager_1 [com.arjuna.mw.wsas.context.ContextManager_1] - allHighLevelServices threw: 
     * @message com.arjuna.mw.wsas.context.ContextManager_2 [com.arjuna.mw.wsas.context.ContextManager_2] - assembling contexts and received: 
     */

    public final Context[] contexts ()
    {
	Context[] ctxs = null;
	HLS[] services = null;

	try
	{
	    services = _manager.allHighLevelServices();
	}
	catch (Exception ex)
	{
	    wsasLogger.arjLoggerI18N.warn("com.arjuna.mw.wsas.context.ContextManager_1",
					  new Object[]{ex});
	}
	
	if (services != null)
	{
	    /*
	     * Null entries are allowed and should be ignored.
	     */

	    ctxs = new Context[services.length];

	    try
	    {
		//		ActivityHierarchy ctx = _activity.currentActivity();

		/*
		 * Check for null or leave to hls? (leave to hls at
		 * the moment).
		 */

		for (int i = 0; i < services.length; i++)
		    ctxs[i] = services[i].context();
	    }
	    catch (Exception ex)
	    {
		wsasLogger.arjLoggerI18N.warn("com.arjuna.mw.wsas.context.ContextManager_2",
					      new Object[]{ex});
		
		ctxs = null;
	    }
	}
	
	return ctxs;
    }

    private ActivityManager _manager;
    
}
