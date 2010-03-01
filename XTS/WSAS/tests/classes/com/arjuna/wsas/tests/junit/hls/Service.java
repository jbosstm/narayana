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
 * $Id: Service.java,v 1.1 2002/11/25 10:51:47 nmcl Exp $
 */

package com.arjuna.wsas.tests.junit.hls;

import com.arjuna.mw.wsas.UserActivity;
import com.arjuna.mw.wsas.UserActivityFactory;
import com.arjuna.mw.wsas.ActivityManagerFactory;

import com.arjuna.wsas.tests.DemoHLS;
import com.arjuna.wsas.tests.WSASTestUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: Service.java,v 1.1 2002/11/25 10:51:47 nmcl Exp $
 * @since 1.0.
 */

public class Service
{

    @Test
    public void testService()
            throws Exception
    {
        UserActivity ua = UserActivityFactory.userActivity();
        DemoHLS demoHLS = new DemoHLS();
	try
	{
	    ActivityManagerFactory.activityManager().addHLS(demoHLS);
	    
	    ua.start();
	    
	    System.out.println("Started: "+ua.activityName());

	    ua.start();
	    
	    System.out.println("Started: "+ua.activityName());

	    ua.end();
	    
	    ua.end();
	}
    catch (Exception ex)
    {
        WSASTestUtils.cleanup(ua);
        throw ex;
    } finally {
        try {
            if (demoHLS != null) {
                ActivityManagerFactory.activityManager().removeHLS(demoHLS);
            }
        } catch (Exception ex) {
            // ignore this
        }
    }
    }

}
