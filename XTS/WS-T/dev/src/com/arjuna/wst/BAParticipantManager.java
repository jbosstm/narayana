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
 * Copyright (C) 2003,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: BAParticipantManager.java,v 1.5 2004/09/09 08:48:33 kconner Exp $
 */

package com.arjuna.wst;

/**
 * When a Business Activity participant registers with a BA transaction it
 * gets a reference to the transaction coordinator that lets it callback
 * into the transaction and drive the state-transition.
 *
 * One instance of this per transaction.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: BAParticipantManager.java,v 1.5 2004/09/09 08:48:33 kconner Exp $
 * @since XTS 1.0.
 */

public interface BAParticipantManager
{

    /**
     * The participant has exited the business activity. The participant
     * uses this to inform the coordinator that is has left the activity.
     * It will not be informed when (and how) the business activity terminates.
     */

    public void exit () throws WrongStateException, UnknownTransactionException, SystemException;

    /**
     * The participant has completed it works, but wishes to continue in
     * the business activity, so that it will eventually be told when (and how)
     * the activity terminates. The participant may later be asked to
     * compensate for the work it has done.
     */

    public void completed () throws WrongStateException, UnknownTransactionException, SystemException;

    /**
     * Fault.
     */

    public void fault () throws SystemException;
    
    /**
     * An unknown error has occurred that the participant wants to communicate
     * to the coordinator.
     */

    public void unknown () throws SystemException;

    /**
     * An error has occurred during the execution of the protocol that the
     * participant wants to communicate to the coordinator.
     */

    public void error () throws SystemException;

}
