/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
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
 * Copyright (C) 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: DefaultIORRecovery.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.orbportability.recovery;

import com.arjuna.orbportability.Services;
import com.arjuna.orbportability.ORB;

import java.io.FileNotFoundException;
import java.io.IOException;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.BAD_OPERATION;
import org.omg.CORBA.SystemException;

/**
 * The default recovery mechanism does the following:
 *
 * (i) if we are a POA based ORB then do nothing since the reference
 *     should have been created as persistent.
 * (ii) call Services with the binding protocols in the following
 *      order: NAME_SERVICE, FILE, NAMED_CONNECT, BIND_CONNECT,
 *             CONFIGURATION_FILE, RESOLVE_INITIAL_REFERENCES
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: DefaultIORRecovery.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 2.0.
 */

public class DefaultIORRecovery implements IORRecovery
{

public org.omg.CORBA.Object recover (ORB orb, String name, org.omg.CORBA.Object obj, Object[] params) throws SystemException
    {
	if ((obj == null) || (name == null))
	    throw new BAD_PARAM();

	int mechanism = Services.NAME_SERVICE;
	boolean finished = false;
	org.omg.CORBA.Object nObj = null;
        Services services = new Services(orb);

	while (!finished)
	{
	    try
	    {
		nObj = services.getService(name, params, mechanism);

		if (nObj == null)
		    throw new BAD_OPERATION();
		else
		    finished = true;
	    }
	    catch (Exception e)
	    {
		switch (mechanism)
		{
		case Services.CONFIGURATION_FILE:
		    mechanism = Services.RESOLVE_INITIAL_REFERENCES;
		    break;
		case Services.RESOLVE_INITIAL_REFERENCES:
		    finished = true;
		    break;
		case Services.NAME_SERVICE:
		    mechanism = Services.FILE;
		    break;
		case Services.FILE:
		    mechanism = Services.NAMED_CONNECT;
		    break;
		case Services.BIND_CONNECT:
		    mechanism = Services.CONFIGURATION_FILE;
		    break;
		default:
		    finished = true;
		    break;
		}
	    }
	}
	    
	return nObj;
    }

}
