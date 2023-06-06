/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */



package com.arjuna.mw.wscf11.model.twophase;

import com.arjuna.mw.wscf.model.twophase.hls.TwoPhaseHLS;
import com.arjuna.mw.wscf.model.twophase.api.CoordinatorManager;

import com.arjuna.mw.wsas.exceptions.SystemException;

import com.arjuna.mw.wscf.exceptions.ProtocolNotRegisteredException;
import com.arjuna.mw.wscf.protocols.ProtocolManager;
import com.arjuna.mw.wscf.protocols.ProtocolRegistry;

import java.util.HashMap;

/**
 * The factory to return the specific CoordinatorManager implementation.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: CoordinatorManagerFactory.java,v 1.8 2005/05/19 12:13:25 nmcl Exp $
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
		return coordinatorManager("TwoPhase11HLS");
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
            TwoPhaseHLS coordHLS;
            synchronized (_implementations)
            {
                coordHLS = (TwoPhaseHLS) _implementations.get(protocol);

                if (coordHLS == null)
                {
                    coordHLS = (TwoPhaseHLS) _protocolManager.getProtocolImplementation(protocol);

                    _implementations.put(protocol, coordHLS);
                }

                return coordHLS.coordinatorManager();
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