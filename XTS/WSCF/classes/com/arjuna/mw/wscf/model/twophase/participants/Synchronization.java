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
 * Copyright (C) 2002,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Synchronization.java,v 1.2 2005/05/19 12:13:26 nmcl Exp $
 */

package com.arjuna.mw.wscf.model.twophase.participants;

import com.arjuna.mw.wsas.exceptions.SystemException;

/**
 * This is the interface that all synchronization participants must define.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: Synchronization.java,v 1.2 2005/05/19 12:13:26 nmcl Exp $
 * @since 1.0.
 */

public interface Synchronization
{

    /**
     * The transaction that the instance is enrolled with is about to
     * commit.
     * 
     * @exception SystemException Thrown if any error occurs. This will cause
     * the transaction to roll back.
     */

    public void beforeCompletion () throws SystemException;

    /**
     * The transaction that the instance is enrolled with has completed and
     * the state in which is completed is passed as a parameter.
     *
     * @param CompletionStatus cs The state in which the transaction completed.
     *
     * @exception SystemException Thrown if any error occurs. This has no
     * affect on the outcome of the transaction.
     */

    public void afterCompletion (int status) throws SystemException;
    
}

