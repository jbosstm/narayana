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
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: UserTransactionFactory.java,v 1.6 2005/05/19 12:13:26 nmcl Exp $
 */

package com.arjuna.mw.wscf.model.xa;

import com.arjuna.mw.wscf.logging.wscfLogger;

import com.arjuna.mw.wscf.model.xa.hls.JTAHLS;

import com.arjuna.mw.wscf.common.*;
import com.arjuna.mw.wscf.protocols.*;
import com.arjuna.mw.wscf.utils.*;

import com.arjuna.mwlabs.wscf.model.jta.arjunajta.JTAHLSImple;

import com.arjuna.mwlabs.wscf.utils.*;

import javax.transaction.SystemException;

import com.arjuna.mw.wscf.exceptions.*;

import java.util.HashMap;

public class UserTransactionFactory
{

    /**
     * @exception ProtocolNotRegisteredException Thrown if the default
     * protocol is not available.
     *
     * @return the CoordinatorManager implementation to use. The default
     * coordination protocol is used (two-phase commit) with its
     * associated implementation.
     *
     * @message com.arjuna.mw.wscf.model.xa.UTF_1 [com.arjuna.mw.wscf.model.xa.UTF_1] - Failed to create 
     */

    public static javax.transaction.UserTransaction userTransaction () throws ProtocolNotRegisteredException, SystemException
    {
	try
	{
	    ProtocolLocator pl = new ProtocolLocator(JTAHLSImple.className());
	    org.w3c.dom.Document doc = pl.getProtocol();
	    
	    if (doc == null)
	    {
		wscfLogger.arjLoggerI18N.warn("com.arjuna.mw.wscf.model.xa.UTF_1",
					      new Object[]{JTAHLSImple.className()});
	    }
	    else
		return userTransaction(doc);
	}
	catch (Exception ex)
	{
	    ex.printStackTrace();

	    throw new SystemException(ex.toString());
	}

	return null;
    }

    /**
     * Obtain a reference to a coordinator that implements the specified
     * protocol.
     *
     * @param protocol The XML definition of the type of
     * coordination protocol required.
     *
     * @exception ProtocolNotRegisteredException Thrown if the requested
     * protocol is not available.
     *
     * @return the CoordinatorManager implementation to use.
     */
    
    /*
     * Have the type specified in XML. More data may be specified, which
     * can be passed to the implementation in the same way ObjectName was.
     */

    public static javax.transaction.UserTransaction userTransaction (org.w3c.dom.Document protocol) throws ProtocolNotRegisteredException, SystemException
    {
	try
	{
	    synchronized (_implementations)
	    {
		org.w3c.dom.Text child = DomUtil.getTextNode(protocol, CoordinatorXSD.coordinatorType);
		String protocolType = child.getNodeValue();
		JTAHLS coordHLS = (JTAHLS) _implementations.get(protocolType);
	
		if (coordHLS == null)
		{
		    Object implementation = _protocolManager.getProtocolImplementation(protocol);
		
		    if (implementation instanceof String)
		    {
			Class c = Class.forName((String) implementation);

			coordHLS = (JTAHLS) c.newInstance();
		    }
		    else
			coordHLS = (JTAHLS) implementation;

		    _implementations.put(protocolType, coordHLS);
		}
	    
		return coordHLS.userTransaction();
	    }
	}
	catch (ProtocolNotRegisteredException ex)
	{
	    throw ex;
	}
	catch (Exception ex)
	{
	    throw new SystemException(ex.toString());
	}
    }

    private static ProtocolManager _protocolManager = ProtocolRegistry.sharedManager();
    private static HashMap         _implementations = new HashMap();
    
}
