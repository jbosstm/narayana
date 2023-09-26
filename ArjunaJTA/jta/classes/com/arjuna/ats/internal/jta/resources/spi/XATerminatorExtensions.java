/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jta.resources.spi;

import javax.transaction.xa.Xid;

/**
 * Extensions to the basic XATerminator spi interface. It is important that
 * these extensions aren't relied on to drive the normal JCA protocol in case
 * we're ever embedded in a foreign implementation.
 *
 * It would be nice to have these non-XA specific extensions managed by a different
 * class than the one that deals with the standard XATerminator interface methods,
 * c.f. JTS/OTS for 2PC and Synchronizations. However, it's a lot easier to just bundle
 * these together in the same implementation because of the way JCA works. Of course that can
 * be changed later if necessary and the user(s) won't notice anyway.
 *
 * @author marklittle
 */

public interface XATerminatorExtensions
{
    /*
     * Synchronizations aren't part of XA, so that's why it would be nice to have these
     * handled by a separate instance, as well as being architecturally symmetrical with
     * OTS. But it's not a big deal. Plus, this isn't really symmetrical either, since we
     * don't need afterCompletion.
     *
     * TODO check whether it makes sense to resurrect afterCompletion here for, say,
     * compensations.
     */

    /**
     * Call beforeCompletion on the registered instance. Exceptions will cause the transaction
     * to be set rollback only.
     *
     * Note: this will run beforeCompletion even on setRollbackOnly transactions.
     * Users may wish to avoid calling this method in such cases, or prior to calling rollback.
     *
     * @param xid the transaction instance.
     * @return success (or not).
     */

    public boolean beforeCompletion (Xid xid) throws jakarta.transaction.SystemException;
}