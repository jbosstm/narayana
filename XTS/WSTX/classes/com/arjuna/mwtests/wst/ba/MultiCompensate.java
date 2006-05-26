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
 * $Id: MultiCompensate.java,v 1.3.8.1 2005/11/22 10:36:18 kconner Exp $
 */

package com.arjuna.mwtests.wst.ba;

import com.arjuna.mw.wst.BusinessActivityManager;
import com.arjuna.mw.wst.UserBusinessActivity;
import com.arjuna.mwtests.wst.common.DemoBusinessParticipant;
import com.arjuna.mwtests.wst.common.FailureBusinessParticipant;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: MultiCompensate.java,v 1.3.8.1 2005/11/22 10:36:18 kconner Exp $
 * @since 1.0.
 */

public class MultiCompensate
{

    public static void main (String[] args)
    {
	boolean passed = false;
	
	try
	{
	    UserBusinessActivity uba = UserBusinessActivity.getUserBusinessActivity();
	    BusinessActivityManager bam = BusinessActivityManager.getBusinessActivityManager();
	    com.arjuna.wst.BAParticipantManager bpm1 = null;
	    com.arjuna.wst.BAParticipantManager bpm2 = null;
	    DemoBusinessParticipant p = new DemoBusinessParticipant(DemoBusinessParticipant.COMPENSATE, "1234");
	    FailureBusinessParticipant fp = new FailureBusinessParticipant(FailureBusinessParticipant.FAIL_IN_COMPENSATE, "5678");
	    
	    uba.begin();
	    
	    bpm1 = bam.enlistForBusinessAgreementWithParticipantCompletion(p, "1234");
	    bpm2 = bam.enlistForBusinessAgreementWithParticipantCompletion(fp, "5678");

	    bpm1.completed();
	    bpm2.completed();
	    
	    uba.cancel();

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
