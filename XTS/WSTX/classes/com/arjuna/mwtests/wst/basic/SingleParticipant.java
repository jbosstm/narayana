/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
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
 * $Id: SingleParticipant.java,v 1.7.8.1 2005/11/22 10:36:12 kconner Exp $
 */

package com.arjuna.mwtests.wst.basic;

import com.arjuna.mw.wst.TransactionManager;
import com.arjuna.mw.wst.UserTransaction;
import com.arjuna.mwtests.wst.common.DemoDurableParticipant;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: SingleParticipant.java,v 1.7.8.1 2005/11/22 10:36:12 kconner Exp $
 * @since 1.0.
 */

public class SingleParticipant
{

    public static void main (String[] args)
    {
	boolean passed = false;
	
	try
	{
	    UserTransaction ut = UserTransaction.getUserTransaction();
	    TransactionManager tm = TransactionManager.getTransactionManager();
	    DemoDurableParticipant p = new DemoDurableParticipant();
	    
	    ut.begin();
	    
	    tm.enlistForDurableTwoPhase(p, null);
	    
	    ut.commit();

	    passed = p.passed();
	}
	catch (Exception ex)
	{
	    ex.printStackTrace();
	}
	
	if (passed)
	    System.out.println("\nPassed.");
	else
	    System.out.println("\nFailed.");
    }

}
