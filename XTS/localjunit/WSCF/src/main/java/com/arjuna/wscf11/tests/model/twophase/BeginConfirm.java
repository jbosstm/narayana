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
 * $Id: BeginConfirm.java,v 1.2 2003/01/17 15:39:35 nmcl Exp $
 */

package com.arjuna.wscf11.tests.model.twophase;

import javax.inject.Named;

import com.arjuna.mw.wscf.model.twophase.api.UserCoordinator;

import com.arjuna.mw.wscf11.model.twophase.UserCoordinatorFactory;
import com.arjuna.wscf11.tests.WSCF11TestUtils;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: BeginConfirm.java,v 1.2 2003/01/17 15:39:35 nmcl Exp $
 * @since 1.0.
 */

@Named
public class BeginConfirm
{
    public void testBeginConfirm()
            throws Exception
    {
        System.out.println("Running test : " + this.getClass().getName());

        UserCoordinator ua = UserCoordinatorFactory.userCoordinator();

	try
	{
	    ua.begin("TwoPhase11HLS");

	    System.out.println("Started: "+ua.identifier()+"\n");

	    ua.confirm();
	}
	catch (Exception ex)
	{
	    WSCF11TestUtils.cleanup(ua);
        throw ex;
    }
    }
}