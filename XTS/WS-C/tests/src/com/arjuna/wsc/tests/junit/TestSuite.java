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
 * Copyright (c) 2002, 2003, Arjuna Technologies Limited.
 *
 * TestSuite.java
 */

package com.arjuna.wsc.tests.junit;

public class TestSuite extends junit.framework.TestSuite
{
    public TestSuite()
    {
        addTest(new junit.framework.TestSuite(com.arjuna.wsc.tests.junit.ActivationTestCase.class));
        addTest(new junit.framework.TestSuite(com.arjuna.wsc.tests.junit.RegistrationTestCase.class));

        addTest(new junit.framework.TestSuite(com.arjuna.wsc.tests.junit.ActivationServiceTestCase.class));
        addTest(new junit.framework.TestSuite(com.arjuna.wsc.tests.junit.ActivationServiceExceptionTestCase.class));
        addTest(new junit.framework.TestSuite(com.arjuna.wsc.tests.junit.RegistrationServiceTestCase.class));
        addTest(new junit.framework.TestSuite(com.arjuna.wsc.tests.junit.RegistrationServiceExceptionTestCase.class));

        addTest(new junit.framework.TestSuite(com.arjuna.wsc.tests.junit.EnduranceTestCase.class));
        addTest(new junit.framework.TestSuite(com.arjuna.wsc.tests.junit.ThreadedEnduranceTestCase.class));
    }
}
