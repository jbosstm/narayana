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
 * Copyright (c) 2003, Arjuna Technologies Limited.
 *
 * TestSuite.java
 */

package com.arjuna.wst11.tests.junit;

import com.arjuna.wst11.tests.junit.*;

public class TestSuite extends junit.framework.TestSuite
{
    public TestSuite()
    {
        // the service tests are no longer working
        addTest(new junit.framework.TestSuite(CompletionParticipantTestCase.class));
        addTest(new junit.framework.TestSuite(CompletionCoordinatorTestCase.class));
        addTest(new junit.framework.TestSuite(TwoPCParticipantTestCase.class));
        addTest(new junit.framework.TestSuite(TwoPCCoordinatorTestCase.class));
        //addTest(new junit.framework.TestSuite(CompletionServiceTestCase.class));
        //addTest(new junit.framework.TestSuite(TwoPCServiceTestCase.class));
        //addTest(new junit.framework.TestSuite(BusinessAgreementWithParticipantCompletionServiceTestCase.class));
        addTest(new junit.framework.TestSuite(BusinessAgreementWithParticipantCompletionParticipantTestCase.class));
        addTest(new junit.framework.TestSuite(BusinessAgreementWithParticipantCompletionCoordinatorTestCase.class));
        //addTest(new junit.framework.TestSuite(BusinessAgreementWithCoordinatorCompletionServiceTestCase.class));
        addTest(new junit.framework.TestSuite(BusinessAgreementWithCoordinatorCompletionParticipantTestCase.class));
        addTest(new junit.framework.TestSuite(BusinessAgreementWithCoordinatorCompletionCoordinatorTestCase.class));
        // these tests refer to a sercie which no longer exists
        //addTest(new junit.framework.TestSuite(BAParticipantManagerServiceTestCase.class));
        //addTest(new junit.framework.TestSuite(BAParticipantManagerParticipantTestCase.class));
        //addTest(new junit.framework.TestSuite(BAParticipantManagerCoordinatorTestCase.class));
        //addTest(new junit.framework.TestSuite(BusinessActivityTerminatorServiceTestCase.class));
    }

}