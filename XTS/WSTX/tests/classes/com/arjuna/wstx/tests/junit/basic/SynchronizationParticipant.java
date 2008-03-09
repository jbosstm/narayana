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
 * $Id: SynchronizationParticipant.java,v 1.1 2002/11/25 11:00:54 nmcl Exp $
 */

package com.arjuna.wstx.tests.junit.basic;

import com.arjuna.mw.wstx.*;
import com.arjuna.wstx.tests.DemoParticipant;
import com.arjuna.wstx.tests.DemoSynchronization;
import junit.framework.TestCase;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: SynchronizationParticipant.java,v 1.1 2002/11/25 11:00:54 nmcl Exp $
 * @since 1.0.
 */

public class SynchronizationParticipant extends TestCase
{

    public static void testSynchronizationParticipant()
            throws Exception
    {
        UserTransaction ut = UserTransactionFactory.userTransaction();

    try
	{
	    TransactionManager tm = TransactionManagerFactory.transactionManager();
	    DemoParticipant p = new DemoParticipant(ut.identifier());
	    DemoSynchronization s = new DemoSynchronization(ut.identifier());
	    
	    ut.begin();
	    
	    tm.enlist(p);
	    tm.addSynchronization(s);
	    
	    ut.commit();
    } catch (Exception eouter) {
        try {
            ut.rollback();
        } catch(Exception einner) {
        }
        throw eouter;
	}
    }
}
