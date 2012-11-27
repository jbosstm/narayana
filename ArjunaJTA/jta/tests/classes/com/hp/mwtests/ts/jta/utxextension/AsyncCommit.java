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
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: SimpleTest.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jta.utxextension;

import java.util.concurrent.Future;

import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;

import org.junit.Test;

import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionImple;
import com.arjuna.ats.internal.jta.transaction.arjunacore.UserTransactionImple;
import com.hp.mwtests.ts.jta.common.TestResource;

public class AsyncCommit
{
    @Test
    public void test() throws Exception
    {
    	UserTransactionImple ut = new UserTransactionImple();
    	ut.begin();
    	
    	TransactionImple current = TransactionImple.getTransaction();

        TestResource res1, res2;
        current.enlistResource( res1 = new TestResource() );
        current.enlistResource( res2 = new TestResource() );

        current.delistResource( res2, XAResource.TMSUCCESS );
        current.delistResource( res1, XAResource.TMSUCCESS );

        Future<Void> commitAsync = ut.commitAsync();
        
        commitAsync.get();
    }
}
