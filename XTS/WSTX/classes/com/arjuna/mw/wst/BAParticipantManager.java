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
 * Copyright (C) 2003,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: BAParticipantManager.java,v 1.4 2004/12/21 09:49:13 kconner Exp $
 */

package com.arjuna.mw.wst;

import com.arjuna.mw.wst.exceptions.*;

/**
 * When a Business Activity participant registers with a BA transaction it
 * gets a reference to the transaction coordinator that lets it callback
 * into the transaction and drive the state-transition.
 *
 * One instance of this per transaction.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: BAParticipantManager.java,v 1.4 2004/12/21 09:49:13 kconner Exp $
 * @since XTS 1.0.
 */

public interface BAParticipantManager
{

    /**
     * The participant has exited.
     */

    public void exited (String id) throws WrongStateException, UnknownTransactionException, UnknownParticipantException, SystemException;

    /**
     * The participant has completed.
     */

    public void completed (String id) throws WrongStateException, UnknownTransactionException, UnknownParticipantException, SystemException;

    /*
     * The participant has failed.
     */

    public void faulted (String id) throws WrongStateException, UnknownTransactionException, UnknownParticipantException, SystemException;

}
