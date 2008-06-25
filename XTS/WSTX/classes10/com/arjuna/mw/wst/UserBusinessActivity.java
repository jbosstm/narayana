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
 * $Id: UserBusinessActivity.java,v 1.4.6.1 2005/11/22 10:36:06 kconner Exp $
 */

package com.arjuna.mw.wst;

import com.arjuna.wst.SystemException;
import com.arjuna.wst.TransactionRolledBackException;
import com.arjuna.wst.UnknownTransactionException;
import com.arjuna.wst.WrongStateException;

/**
 * This is the interface that allows BAs to be started and terminated.
 * The messaging layer converts the messages into calls on this.
 *
 * Importantly, a UserBusinessActivity does not represent a specific
 * transaction, but rather is responsible for providing access to an implicit
 * per-thread  transaction context; it is similar to the UserTransaction in
 * the JTA specification. Therefore, all of the UserTransaction methods
 * implicitly act on the current thread of control.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: UserBusinessActivity.java,v 1.4.6.1 2005/11/22 10:36:06 kconner Exp $
 * @since XTS 1.0.
 */

public abstract class UserBusinessActivity
{
    /**
     * The user business activity.
     */
    private static UserBusinessActivity USER_BUSINESS_ACTIVITY ;
    
    /**
     * Get the user business activity.
     * @return The user business activity.
     */
    public static synchronized UserBusinessActivity getUserBusinessActivity()
    {
        return USER_BUSINESS_ACTIVITY ;
    }
    
    /**
     * Set the user business activity.
     * @param userBusinessActivity The user business activity.
     */
    public static synchronized void setUserBusinessActivity(final UserBusinessActivity userBusinessActivity)
    {
        USER_BUSINESS_ACTIVITY = userBusinessActivity ;
    }

    public static final int ATOMIC_OUTCOME = 0;
    public static final int MIXED_OUTCOME = 1;
    
    /**
     * Start a new business activity with atomic outcome.
     * If one is already associated with this thread
     * then the WrongStateException will be thrown. Upon success, this
     * operation associates the newly created transaction with the current
     * thread.
     */
    public abstract void begin()
        throws WrongStateException, SystemException;

    /**
     * Start a new BA with atomic outcome and the specified timeout as
     * its lifetime.
     * If one is already associated with this thread then the
     * WrongStateException will be thrown.
     */
    public abstract void begin(final int timeout)
        throws WrongStateException, SystemException;

    /**
     * The BA is normally terminated by the close method. This signals to
     * all registered participants that the BA has ended and no compensation
     * is required.
     */
    public abstract void close()
        throws TransactionRolledBackException, UnknownTransactionException, SystemException;

    /**
     * If the BA must undo its work then the cancel method is used. Any
     * participants that can compensate are forced to do so.
     */
    public abstract void cancel()
        throws UnknownTransactionException, SystemException;

    /**
     * If participants have registered for the BusinessAgreementWithComplete
     * protocol then they will be expecting the application to inform them
     * when all work intended for them has been sent (and responded to). The
     * complete method is used for this purpose.
     */
    public abstract void complete()
        throws UnknownTransactionException, SystemException;

    public abstract String transactionIdentifier();
}
