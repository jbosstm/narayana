/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors 
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors. 
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
 * Copyright (C) 2004,
 * 
 * Arjuna Technologies Ltd, Newcastle upon Tyne, Tyne and Wear, UK.
 * 
 * $Id: XAResourceErrorHandler.java 2342 2006-03-30 13:06:17Z  $
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
     * OTS. But it's not a big deal.
     */
    
    /**
     * Call beforeCompletion on the registered instance. Exceptions will cause the transaction
     * to rollback.
     * 
     * @param xid the transaction instance.
     * @return success (or not).
     */
    
    public boolean beforeCompletion (Xid xid) throws javax.transaction.SystemException;
    
    /**
     * Call afterCompletion on the registered instance. Exceptions will be logged but have no
     * affect on the transaction (which has already completed.)
     * 
     * @param xid the transaction instance
     * @param status the transaction status (values from JTA Status).
     * @return success (or not).
     * @throws javax.transaction.SystemException
     */
    
    public boolean afterCompletion (Xid xid, int status) throws javax.transaction.SystemException;
}
