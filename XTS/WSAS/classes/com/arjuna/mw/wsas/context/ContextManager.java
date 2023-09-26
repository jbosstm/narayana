/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wsas.context;

import com.arjuna.mw.wsas.UserActivity;
import com.arjuna.mw.wsas.UserActivityFactory;
import com.arjuna.mw.wsas.activity.ActivityHandle;
import com.arjuna.mw.wsas.exceptions.NoActivityException;
import com.arjuna.mw.wsas.logging.wsasLogger;

import com.arjuna.mw.wsas.ActivityManager;
import com.arjuna.mw.wsas.ActivityManagerFactory;

import com.arjuna.mw.wsas.activity.HLS;
import com.arjuna.mwlabs.wsas.ActivityManagerImple;
import com.arjuna.mwlabs.wsas.activity.ActivityImple;

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
     * message com.arjuna.mw.wsas.context.ContextManager_3 [com.arjuna.mw.wsas.context.ContextManager_3] - getHighLevelServices threw:
     * message com.arjuna.mw.wsas.context.ContextManager_4 [com.arjuna.mw.wsas.context.ContextManager_4] - assembling context and received:
     */

    public final Context context (String serviceType)
    {
        Context ctx = null;
        HLS service = null;

        try
        {
            // ensure we are in an activity associated with the correct service type

            String currentServiceType = UserActivityFactory.userActivity().serviceType();
            if (currentServiceType.equals(serviceType)) {
                service = _manager.getHighLevelService(serviceType);
            }
        }
        catch (NoActivityException nae)
        {
            // ignore
        }
        catch (Exception ex)
        {
        wsasLogger.i18NLogger.warn_context_ContextManager_1(ex);
        }

        if (service != null)
        {
            try
            {
                ctx = service.context();
            }
            catch (Exception ex)
            {
                wsasLogger.i18NLogger.warn_context_ContextManager_2(ex);

                ctx = null;
            }
        }

        return ctx;
    }

    private ActivityManager _manager;

}