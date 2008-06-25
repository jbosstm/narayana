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
 * $Id: Compensate.java,v 1.3.8.1 2005/11/22 10:36:17 kconner Exp $
 */

package com.arjuna.wst.tests.junit.ba;

import com.arjuna.mw.wst.BusinessActivityManager;
import com.arjuna.mw.wst.UserBusinessActivity;
import com.arjuna.wst.tests.DemoBusinessParticipant;
import junit.framework.TestCase;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: Compensate.java,v 1.3.8.1 2005/11/22 10:36:17 kconner Exp $
 * @since 1.0.
 */

public class Compensate extends TestCase
{
    public static void testCompensate()
            throws Exception
    {
	    UserBusinessActivity uba = UserBusinessActivity.getUserBusinessActivity();
        BusinessActivityManager bam = BusinessActivityManager.getBusinessActivityManager();
	    com.arjuna.wst.BAParticipantManager bpm = null;
	    String participantId = "1236";
	    DemoBusinessParticipant p = new DemoBusinessParticipant(DemoBusinessParticipant.COMPENSATE, participantId);
	    try {
	    uba.begin();
	    
	    bpm = bam.enlistForBusinessAgreementWithParticipantCompletion(p, participantId);

	    bpm.completed();
        } catch (Exception eouter) {
            try {
                uba.cancel();
            } catch(Exception einner) {
            }
            throw eouter;
        }
	    
	    uba.cancel();

        assertTrue(p.passed());
    }
}
