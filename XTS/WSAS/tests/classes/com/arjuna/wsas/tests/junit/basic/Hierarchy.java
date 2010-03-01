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
 * $Id: Hierarchy.java,v 1.1 2002/11/25 10:51:46 nmcl Exp $
 */

package com.arjuna.wsas.tests.junit.basic;

import com.arjuna.mw.wsas.UserActivity;
import com.arjuna.mw.wsas.UserActivityFactory;
import com.arjuna.mw.wsas.exceptions.NoActivityException;
import com.arjuna.mw.wsas.activity.ActivityHierarchy;

import com.arjuna.wsas.tests.WSASTestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: Hierarchy.java,v 1.1 2002/11/25 10:51:46 nmcl Exp $
 * @since 1.0.
 */

public class Hierarchy
{

    @Test
    public void testHierarchy()
            throws Exception
    {
        UserActivity ua = UserActivityFactory.userActivity();

	try
	{
	    ua.start();
	    
	    System.out.println("Started: "+ua.activityName());
	    
	    ua.start();

	    System.out.println("Started: "+ua.activityName());

	    ActivityHierarchy ctx = ua.currentActivity();
	    
	    System.out.println("\nHierarchy: "+ctx);

	    if (ctx == null) {
            fail("current activity should not be null");
        } else {
            ua.end();

            System.out.println("\nCurrent: "+ua.activityName());
	    
            ua.end();

            try {
                if (ua.activityName() != null) {
                    fail("activity name should be null but is " + ua.activityName());
                }
            } catch (NoActivityException ex) {
                // ok if we get here
            }
        }
    } catch (Exception ex) {
        WSASTestUtils.cleanup(ua);
        throw ex;
    }
    }

}
