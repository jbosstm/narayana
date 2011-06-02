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

package com.arjuna.wsc11.tests.junit;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        /*
        com.arjuna.wsc11.tests.junit.ActivationTestCase.class,
        com.arjuna.wsc11.tests.junit.RegistrationTestCase.class,
        com.arjuna.wsc11.tests.junit.ActivationServiceTestCase.class,
        com.arjuna.wsc11.tests.junit.ActivationServiceExceptionTestCase.class,
        com.arjuna.wsc11.tests.junit.RegistrationServiceTestCase.class,
        com.arjuna.wsc11.tests.junit.RegistrationServiceExceptionTestCase.class,
        com.arjuna.wsc11.tests.junit.EnduranceTestCase.class
        */
        com.arjuna.wsc11.tests.junit.ActivationServiceTestCase.class
})
public class TestSuite
{
}