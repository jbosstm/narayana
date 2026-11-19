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
 * $Id: NestedActivity.java,v 1.1 2002/11/25 10:51:46 nmcl Exp $
 */

package com.arjuna.wsas.tests.arq.basic;

import javax.inject.Named;

import com.arjuna.mw.wsas.UserActivity;
import com.arjuna.mw.wsas.UserActivityFactory;
import com.arjuna.mw.wsas.exceptions.NoActivityException;

import com.arjuna.wsas.tests.WSASTestUtils;
import static org.junit.Assert.*;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: NestedActivity.java,v 1.1 2002/11/25 10:51:46 nmcl Exp $
 * @since 1.0.
 */

@Named
public class NestedActivity
{

    public void testNestedActivity()
            throws Exception
    {
	    UserActivity ua = UserActivityFactory.userActivity();
	
        try
        {
	    ua.start("dummy");
	    
	    System.out.println("Started: "+ua.activityName());
	    
	    ua.start("dummy");

	    String nested = ua.activityName();
	    
	    System.out.println("Started: "+nested);

	    System.out.println("\nEnding: "+nested);
	    
	    ua.end();

	    String parent = ua.activityName();
	    
	    System.out.println("\nCurrent: "+parent);
	    
	    System.out.println("\nEnding: "+parent);
	    
	    ua.end();

        try {
            if (ua.activityName() != null) {
                fail("activity name should be null but is " + ua.activityName());
            }
        } catch (NoActivityException ex) {
            // ok if we get here
        }

        System.out.println("\nEnded.");

	}
    catch (Exception ex)
    {
        WSASTestUtils.cleanup(ua);
        throw ex;
    }
    }
}
