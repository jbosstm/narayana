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
package com.hp.mwtests.ts.jta.lastresource;

import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import junit.framework.TestCase;

import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionManagerImple;

public class LastResourceDisallowTestCase extends TestCase
{
    public void testDisallowed()
        throws SystemException, NotSupportedException, RollbackException
    {
        final LastOnePhaseResource firstResource = new LastOnePhaseResource() ;
        final LastOnePhaseResource secondResource = new LastOnePhaseResource() ;
        
        final TransactionManager tm = new TransactionManagerImple() ;
        tm.begin() ;
        try
        {
            final Transaction tx = tm.getTransaction() ;
            assertTrue("First resource enlisted", tx.enlistResource(firstResource)) ;
            assertFalse("Second resource enlisted", tx.enlistResource(secondResource)) ;
        }
        finally
        {
            tm.rollback() ;
        }
    }
}
