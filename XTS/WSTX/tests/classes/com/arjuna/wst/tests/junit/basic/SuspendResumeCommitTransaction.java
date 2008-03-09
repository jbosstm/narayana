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
 * $Id: SuspendResumeCommitTransaction.java,v 1.2.24.1 2005/11/22 10:36:11 kconner Exp $
 */

package com.arjuna.wst.tests.junit.basic;

import com.arjuna.mw.wst.TransactionManager;
import com.arjuna.mw.wst.TxContext;
import com.arjuna.mw.wst.UserTransaction;
import junit.framework.TestCase;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: SuspendResumeCommitTransaction.java,v 1.2.24.1 2005/11/22 10:36:11 kconner Exp $
 * @since 1.0.
 */

public class SuspendResumeCommitTransaction extends TestCase
{

    public static void testSuspendResumeCommitTransaction()
            throws Exception
    {
	    UserTransaction ut = UserTransaction.getUserTransaction();
	    TransactionManager tm = TransactionManager.getTransactionManager();
	
	    ut.begin();
	    
	    TxContext ctx = tm.suspend();

	    System.out.println("Suspended: "+ctx);
	    
	    tm.resume(ctx);

	    System.out.println("\nResumed");
	    
	    ut.commit();
    }
}
