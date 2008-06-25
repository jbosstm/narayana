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
 * $Id: StartEnd.java,v 1.3 2003/03/14 14:26:34 nmcl Exp $
 */

package com.arjuna.wscf11.tests.model.twophase;

import com.arjuna.mwlabs.wscf.utils.ProtocolLocator;

import com.arjuna.mw.wscf.UserCoordinator;
import com.arjuna.mw.wscf11.UserCoordinatorFactory;

import com.arjuna.mw.wscf.model.twophase.common.*;
import com.arjuna.mw.wscf.model.twophase.outcomes.*;

import com.arjuna.mw.wsas.activity.*;

import com.arjuna.mw.wsas.exceptions.NoActivityException;
import com.arjuna.wscf.tests.WSCFTestUtils;
import junit.framework.TestCase;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: StartEnd.java,v 1.3 2003/03/14 14:26:34 nmcl Exp $
 * @since 1.0.
 */

public class StartEnd extends TestCase
{

    public  void testStartEnd()
            throws Exception
    {
        System.out.println("Running test : " + this.getClass().getName());

	String className = "com.arjuna.mwlabs.wscf.model.twophase.arjunacore.TwoPhase11HLSImple";
	org.w3c.dom.Document implementationDoc = null;

	//	System.setProperty("com.arjuna.mw.wscf.protocolImplementation", className);

        Class clazz = this.getClass().getClassLoader().loadClass(className);
	    ProtocolLocator pl = new ProtocolLocator(clazz);

	    implementationDoc = pl.getProtocol();

	    UserCoordinator ua = UserCoordinatorFactory.userCoordinator(implementationDoc);

    try
    {
	    ua.start();

	    System.out.println("Started: "+ua.activityName()+"\n");

	    Outcome res = ua.end();

	    if (res instanceof CoordinationOutcome)
	    {
		CoordinationOutcome co = (CoordinationOutcome) res;
        int result = co.result();

        if (result != TwoPhaseResult.CANCELLED)
		    fail("expected result \"CANCELLED\" (" + TwoPhaseResult.CANCELLED + ") but got " + result);
	    }
	}
	catch (NoActivityException ex)
	{
	    // why is it ok to get here?;
	}
    catch (Exception ex)
    {
        WSCFTestUtils.cleanup(ua);
        throw ex;
    }
    }
}