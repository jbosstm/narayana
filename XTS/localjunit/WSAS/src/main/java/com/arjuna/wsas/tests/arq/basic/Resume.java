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
 * $Id: Resume.java,v 1.1 2002/11/25 10:51:47 nmcl Exp $
 */

package com.arjuna.wsas.tests.arq.basic;

import javax.inject.Named;

import com.arjuna.mw.wsas.UserActivity;
import com.arjuna.mw.wsas.UserActivityFactory;

import com.arjuna.mw.wsas.activity.ActivityHierarchy;

import com.arjuna.mw.wsas.common.GlobalId;

import com.arjuna.wsas.tests.WSASTestUtils;
import static org.junit.Assert.*;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: Resume.java,v 1.1 2002/11/25 10:51:47 nmcl Exp $
 * @since 1.0.
 */

@Named
public class Resume
{

    public void testResume()
            throws Exception
    {
        UserActivity ua = UserActivityFactory.userActivity();

	try
	{
	    GlobalId ac1 = null;
	    GlobalId ac2 = null;
	    
	    ua.start("dummy");
	    
	    ac1 = ua.activityId();
	    
	    System.out.println("Started: "+ac1);
	    
	    ua.start("dummy");
	    
	    ac2 = ua.activityId();

	    System.out.println("\nStarted: "+ac2);
	    
	    ActivityHierarchy ctx = ua.suspend();
	    
	    System.out.println("\nSuspended: "+ctx);
	    
        if (ua.currentActivity() != null) {
            fail("Current activity shoudl be null " + ua.currentActivity());
        }

        ua.resume(ctx);
		
        if (!ac2.equals(ua.activityId()))
        {
            fail("Current activity id " + ua.activityId() + " should equal " + ac2);
        }

        ua.end();

        if (!ac1.equals(ua.activityId())) {
            fail("Current activity id " + ua.activityId() + " should equal " + ac1);
        }
    } finally {
        WSASTestUtils.cleanup(ua);
    }
    }

}
