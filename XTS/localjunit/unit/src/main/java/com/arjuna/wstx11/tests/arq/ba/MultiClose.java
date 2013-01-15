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
 * $Id: MultiClose.java,v 1.3.8.1 2005/11/22 10:36:18 kconner Exp $
 */

package com.arjuna.wstx11.tests.arq.ba;

import javax.inject.Named;

import com.arjuna.mw.wst11.BusinessActivityManager;
import com.arjuna.mw.wst11.UserBusinessActivity;
import com.arjuna.wstx.tests.common.DemoBusinessParticipant;
import com.arjuna.wstx.tests.common.FailureBusinessParticipant;
import static org.junit.Assert.*;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: MultiClose.java,v 1.3.8.1 2005/11/22 10:36:18 kconner Exp $
 * @since 1.0.
 */

@Named
public class MultiClose
{
    public void testMultiClose()
            throws Exception
    {
	    UserBusinessActivity uba = UserBusinessActivity.getUserBusinessActivity();

        BusinessActivityManager bam = BusinessActivityManager.getBusinessActivityManager();
        com.arjuna.wst11.BAParticipantManager bpm1 = null;
        com.arjuna.wst11.BAParticipantManager bpm2 = null;
        DemoBusinessParticipant p = new DemoBusinessParticipant(DemoBusinessParticipant.CLOSE, "1240");
	    FailureBusinessParticipant fp = new FailureBusinessParticipant(FailureBusinessParticipant.FAIL_IN_CLOSE, "5679");

        try {
	    uba.begin();

	    bpm1 = bam.enlistForBusinessAgreementWithParticipantCompletion(p, "1240");
	    bpm2 = bam.enlistForBusinessAgreementWithParticipantCompletion(fp, "5679");
        bpm1.completed();
        bpm2.completed();
        } catch (Exception eouter) {
            try {
                uba.cancel();
            } catch(Exception einner) {
            }
            throw eouter;
        }
	    uba.close();
        assertTrue(p.passed());
    }
}