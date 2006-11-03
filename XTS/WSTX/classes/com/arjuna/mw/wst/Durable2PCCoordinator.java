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
 * Copyright (C) 2004,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Durable2PCCoordinator.java,v 1.1.2.1 2004/08/06 10:25:30 nmcl Exp $
 */

package com.arjuna.mw.wst;

import com.arjuna.mw.wst.exceptions.SystemException;
import com.arjuna.mw.wst.exceptions.UnknownParticipantException;
import com.arjuna.mw.wst.exceptions.UnknownTransactionException;
import com.arjuna.mw.wst.exceptions.WrongStateException;

/**
 * Allows a durable 2PC participant to rollback or send readonly
 * before the coordinator initiates the protocol.
 *
 * One instance of this per transaction.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: Durable2PCCoordinator.java,v 1.1.2.1 2004/08/06 10:25:30 nmcl Exp $
 * @since XTS 1.0.
 */

public interface Durable2PCCoordinator
{

    /**
     * The participant has rolled back.
     */

    public void rollback (String id) throws WrongStateException, UnknownTransactionException, UnknownParticipantException, SystemException;

    /**
     * The participant is read-only.
     */

    public void readonly (String id) throws WrongStateException, UnknownTransactionException, UnknownParticipantException, SystemException;

}
