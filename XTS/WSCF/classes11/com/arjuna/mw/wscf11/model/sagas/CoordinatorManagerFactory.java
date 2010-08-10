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
 * $Id: CoordinatorManagerFactory.java,v 1.4 2005/05/19 12:13:23 nmcl Exp $
 */

package com.arjuna.mw.wscf11.model.sagas;

import com.arjuna.mw.wscf.logging.wscfLogger;

import com.arjuna.mw.wscf.model.sagas.hls.SagasHLS;
import com.arjuna.mw.wscf.model.sagas.api.CoordinatorManager;

import com.arjuna.mw.wsas.exceptions.SystemException;

import com.arjuna.mwlabs.wscf.utils.ProtocolLocator;

import com.arjuna.mw.wscf.common.CoordinatorXSD;

import com.arjuna.mw.wscf.utils.*;

import com.arjuna.mw.wscf.exceptions.ProtocolNotRegisteredException;
import com.arjuna.mw.wscf.protocols.ProtocolManager;
import com.arjuna.mw.wscf.protocols.ProtocolRegistry;

import java.util.HashMap;

import com.arjuna.mwlabs.wscf11.model.sagas.arjunacore.SagasHLSImple;

/**
 * The factory to return the specific CoordinatorManager implementation.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: CoordinatorManagerFactory.java,v 1.4 2005/05/19 12:13:23 nmcl Exp $
 * @since 1.0.
 */

public class CoordinatorManagerFactory
{


    /**
     * @exception com.arjuna.mw.wscf.exceptions.ProtocolNotRegisteredException Thrown if the default
     * protocol is not available.
     *
     * @return the CoordinatorManager implementation to use. The default
     * coordination protocol is used (two-phase commit) with its
     * associated implementation.
     *
     */

    public static CoordinatorManager coordinatorManager () throws ProtocolNotRegisteredException, SystemException
    {
        return coordinatorManager("Sagas11HLS");
    }

    /**
     * Obtain a reference to a coordinator that implements the specified
     * protocol.
     *
     * @param protocol The XML definition of the type of
     * coordination protocol required.
     *
     * @exception com.arjuna.mw.wscf.exceptions.ProtocolNotRegisteredException Thrown if the requested
     * protocol is not available.
     *
     * @return the CoordinatorManager implementation to use.
     */

    /*
     * Have the type specified in XML. More data may be specified, which
     * can be passed to the implementation in the same way ObjectName was.
     */

    public static CoordinatorManager coordinatorManager (String protocol) throws ProtocolNotRegisteredException, SystemException
    {
        try
        {
            SagasHLS coordHLS;

            synchronized (_implementations)
            {
                coordHLS = (SagasHLS) _implementations.get(protocol);

                if (coordHLS == null)
                {
                    coordHLS = (SagasHLS) _protocolManager.getProtocolImplementation(protocol);
                    
                    _implementations.put(protocol, coordHLS);
                }
            }

            return coordHLS.coordinatorManager();

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