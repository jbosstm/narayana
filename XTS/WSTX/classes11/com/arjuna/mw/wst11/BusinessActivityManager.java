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
 * $Id: BusinessActivityManager.java,v 1.4.8.1 2005/11/22 10:36:05 kconner Exp $
 */

package com.arjuna.mw.wst11;

import com.arjuna.wsc.AlreadyRegisteredException;
import com.arjuna.wst.*;
import com.arjuna.wst11.BAParticipantManager;
import com.arjuna.mw.wst.TxContext;

/**
 * This is the interface that the core exposes in order to allow different
 * types of participants to be enrolled. The messaging layer continues to
 * work in terms of the registrar, but internally we map to one of these
 * methods.
 *
 * As with UserTransaction a TransactionManager does not represent a specific
 * transaction, but rather is responsible for providing access to an implicit
 * per-thread transaction context.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: BusinessActivityManager.java,v 1.4.8.1 2005/11/22 10:36:05 kconner Exp $
 * @since XTS 1.0.
 */

public abstract class BusinessActivityManager
{
    /**
     * The manager.
     */
    private static BusinessActivityManager MANAGER ;
    
    /**
     * Get the business activity manager.
     * @return The business activity manager.
     */
    public static synchronized BusinessActivityManager getBusinessActivityManager()
    {
        return MANAGER ;
    }
    
    /**
     * Set the business activity manager.
     * @param manager The business activity manager.
     */
    public static synchronized void setBusinessActivityManager(final BusinessActivityManager manager)
    {
        MANAGER = manager ;
    }

    /**
     * Enlist a participant for the BusinessAgreement protocol.
     *
     * @return the BAParticipantManager for this transaction.
     */
    public abstract BAParticipantManager enlistForBusinessAgreementWithParticipantCompletion(
        final BusinessAgreementWithParticipantCompletionParticipant bap, final String id)
        throws WrongStateException, UnknownTransactionException, AlreadyRegisteredException, SystemException;

    /**
     * Enlist a participant for the BusinessAgreementWithCoordinatorCompletion protocol.
     *
     * @return the BAParticipantManager for this transaction.
     */
    public abstract BAParticipantManager enlistForBusinessAgreementWithCoordinatorCompletion(
        final BusinessAgreementWithCoordinatorCompletionParticipant bawcp, final String id)
        throws WrongStateException, UnknownTransactionException, AlreadyRegisteredException, SystemException;

    /**
     * The resume method can be used to (re-)associate a thread with a 
     * transaction(s) via its TxContext. Prior to association, the thread is
     * disassociated with any transaction(s) with which it may be currently
     * associated. If the TxContext is null, then the thread is associated with
     * no transaction. The UnknownTransactionException exception is thrown if
     * the transaction that the TxContext refers to is invalid in the scope of
     * the invoking thread.
     */
    public abstract void resume(final TxContext txContext)
        throws UnknownTransactionException, SystemException;

    /**
     * A thread of control may require periods of non-transactionality so that
     * it may perform work that is not associated with a specific transaction.
     * In order to do this it is necessary to disassociate the thread from any
     * transactions. The suspend method accomplishes this, returning a
     * TxContext instance, which is a handle on the transaction. The thread is
     * then no longer associated with any transaction.
     */
    public abstract TxContext suspend()
        throws SystemException;
    
    /**
     * The currentTransaction method returns the TxContext for the current
     * transaction, or null if there is none. Unlike suspend, this method does
     * not disassociate the current thread from the transaction(s). This can
     * be used to enable multiple threads to execute within the scope of the
     * same transaction.
     */
    public abstract TxContext currentTransaction()
        throws SystemException;
    
}
