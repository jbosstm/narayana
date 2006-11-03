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
 * $Id: Cancel.java,v 1.3.8.1 2005/11/22 10:36:18 kconner Exp $
 */

package com.arjuna.mwtests.wst.ba;

import com.arjuna.mw.wst.BusinessActivityManager;
import com.arjuna.mw.wst.UserBusinessActivity;
import com.arjuna.mwtests.wst.common.DemoBusinessParticipant;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: Cancel.java,v 1.3.8.1 2005/11/22 10:36:18 kconner Exp $
 * @since 1.0.
 */

public class Cancel
{

    public static void main (String[] args)
    {
	boolean passed = false;
	
	try
	{
	    UserBusinessActivity uba = UserBusinessActivity.getUserBusinessActivity();
	    BusinessActivityManager bam = BusinessActivityManager.getBusinessActivityManager();
	    String participantId = "1234";
	    DemoBusinessParticipant p = new DemoBusinessParticipant(DemoBusinessParticipant.CANCEL, participantId);
	    
	    uba.begin();
	    
	    bam.enlistForBusinessAgreementWithParticipantCompletion(p, participantId);

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
