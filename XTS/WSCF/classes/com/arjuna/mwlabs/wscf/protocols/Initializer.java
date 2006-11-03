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
 * $Id: Initializer.java,v 1.7 2005/05/19 12:13:39 nmcl Exp $
 */

package com.arjuna.mwlabs.wscf.protocols;

import com.arjuna.mw.wscf.logging.wscfLogger;

import com.arjuna.mw.wscf.protocols.ProtocolManager;

import com.arjuna.mw.wsas.exceptions.*;

import com.arjuna.mwlabs.wscf.utils.*;

/**
 * Register all of the default coordination protocols with the system.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: Initializer.java,v 1.7 2005/05/19 12:13:39 nmcl Exp $
 * @since 1.0.
 */

public class Initializer
{

    public Initializer (ProtocolManager manager) throws SystemException
    {
	_manager = null;
	
	try
	{
	    com.arjuna.mw.wsas.utils.Configuration.initialise("/wscf.xml");
	}
	catch (java.io.FileNotFoundException ex)
	{
	}
	catch (Exception ex)
	{
	}
    }
    
    /**
     * @message com.arjuna.mwlabs.wscf.protocols.Initializer_1 [com.arjuna.mwlabs.wscf.protocols.Initializer_1] - Failed to create: 
     */

    private final void addProtocol (String impl) throws SystemException
    {
	try
	{
	    // add in ArjunaCore support

	    ProtocolLocator pl = new ProtocolLocator(impl);
	    org.w3c.dom.Document doc = pl.getProtocol();
	    
	    if (doc == null)
	    {
		throw new SystemException(wscfLogger.log_mesg.getString("com.arjuna.mwlabs.wscf.protocols.Initializer_1")+impl);
	    }
	    else
	    {
		_manager.addProtocol(doc, impl);
	    }
	}
	catch (Exception ex)
	{
	    ex.printStackTrace();
	    
	    throw new SystemException(ex.toString());
	}
    }

    private ProtocolManager _manager;
    
}

