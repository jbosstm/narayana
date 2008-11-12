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
 * Copyright (C) 2003,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: BAParticipantManagerImple.java,v 1.5.6.1 2005/11/22 10:36:08 kconner Exp $
 */

package com.arjuna.mwlabs.wst.ba.remote;

import com.arjuna.wst.SystemException;
import com.arjuna.wst.UnknownTransactionException;
import com.arjuna.wst.WrongStateException;
import com.arjuna.wst.BAParticipantManager;
import com.arjuna.mwlabs.wscf.model.sagas.arjunacore.ACCoordinator;

import javax.xml.namespace.QName;

/**
 * This is the interface that the core exposes in order to allow different
 * types of participants to be enrolled. The messaging layer continues to
 * work in terms of the registrar, but internally we map to one of these
 * methods.
 *
 * This could also be the interface that high-level users see (e.g., at the
 * application Web Service).
 */

public class BARecoveryParticipantManagerImple implements BAParticipantManager
{

    public BARecoveryParticipantManagerImple(ACCoordinator coordinator, String participantId)
    {
        this.coordinator = coordinator;
        this.participantId = participantId;
    }

    public void exit () throws WrongStateException, UnknownTransactionException, SystemException
    {
        try
        {
            coordinator.delistParticipant(participantId);
        }
        catch (com.arjuna.mw.wscf.exceptions.InvalidParticipantException ex)
        {
            throw new SystemException("UnknownParticipantException");
        }
        catch (com.arjuna.mw.wsas.exceptions.WrongStateException ex)
        {
            throw new WrongStateException();
        }
        catch (com.arjuna.mw.wsas.exceptions.SystemException ex)
        {
            throw new SystemException(ex.toString());
        }
    }

    public void completed () throws WrongStateException, UnknownTransactionException, SystemException
    {
        try
        {
            coordinator.participantCompleted(participantId);
        }
        catch (com.arjuna.mw.wscf.exceptions.InvalidParticipantException ex)
        {
            throw new SystemException("UnknownParticipantException");
        }
        catch (com.arjuna.mw.wsas.exceptions.WrongStateException ex)
        {
            throw new WrongStateException();
        }
        catch (com.arjuna.mw.wsas.exceptions.SystemException ex)
        {
            throw new SystemException(ex.toString());
        }
    }

    public void fault () throws SystemException
    {
        try
        {
            coordinator.participantFaulted(participantId);
        }
        catch (com.arjuna.mw.wscf.exceptions.InvalidParticipantException ex)
        {
            throw new SystemException("UnknownParticipantException");
        }
        catch (com.arjuna.mw.wsas.exceptions.SystemException ex)
        {
            throw new SystemException(ex.toString());
        }
    }

    /**
     * An unknown error has occurred that the participant wants to communicate
     * to the coordinator.
     */

    public void unknown() throws SystemException
    {
        /*
         * this API is not needed here -- quite probably it is not needed in the non recovery case either
         */
    }

    /**
     * An error has occurred during the execution of the protocol that the
     * participant wants to communicate to the coordinator.
     */
    public void error () throws SystemException
    {
        /*
         * this API is not needed here -- quite probably it is not needed in the non recovery case either
         */
    }

    private ACCoordinator coordinator;
    private String participantId;

}