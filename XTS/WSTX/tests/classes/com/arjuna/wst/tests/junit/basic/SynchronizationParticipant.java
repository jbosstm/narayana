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
 * Copyright (C) 2002,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: SynchronizationParticipant.java,v 1.7.8.1 2005/11/22 10:36:11 kconner Exp $
 */

package com.arjuna.wst.tests.junit.basic;

import com.arjuna.mw.wst.TransactionManager;
import com.arjuna.mw.wst.UserTransaction;
import com.arjuna.wst.tests.common.DemoDurableParticipant;
import com.arjuna.wst.tests.common.DemoVolatileParticipant;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: SynchronizationParticipant.java,v 1.7.8.1 2005/11/22 10:36:11 kconner Exp $
 * @since 1.0.
 */

public class SynchronizationParticipant
{

    @Test
    public void testSynchronizationParticipant()
            throws Exception
    {
	    UserTransaction ut = UserTransaction.getUserTransaction();
	    TransactionManager tm = TransactionManager.getTransactionManager();
	    DemoDurableParticipant p = new DemoDurableParticipant();
	    DemoVolatileParticipant pz = new DemoVolatileParticipant();
	    
	    ut.begin();
    try {
	    tm.enlistForDurableTwoPhase(p, p.identifier());
	    tm.enlistForVolatileTwoPhase(pz, pz.identifier());
    }  catch (Exception eouter) {
        try {
            ut.rollback();
        } catch(Exception einner) {
        }
        throw eouter;
    }
	    ut.commit();
    }
}
