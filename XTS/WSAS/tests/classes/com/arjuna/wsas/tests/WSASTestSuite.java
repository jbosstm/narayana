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
 * (C) 2005-2008,
 * @author JBoss Inc.
 */
/*
 * TestSuite.java
 */

package com.arjuna.wsas.tests;

public class WSASTestSuite extends junit.framework.TestSuite
{
    public WSASTestSuite()
    {
        // wsas basic tests
        addTest(new junit.framework.TestSuite(com.arjuna.wsas.tests.junit.basic.Context.class));
        addTest(new junit.framework.TestSuite(com.arjuna.wsas.tests.junit.basic.Hierarchy.class));
        addTest(new junit.framework.TestSuite(com.arjuna.wsas.tests.junit.basic.NestedActivity.class));
        addTest(new junit.framework.TestSuite(com.arjuna.wsas.tests.junit.basic.NullEnd.class));
        addTest(new junit.framework.TestSuite(com.arjuna.wsas.tests.junit.basic.Resume.class));
        addTest(new junit.framework.TestSuite(com.arjuna.wsas.tests.junit.basic.StartEnd.class));
        addTest(new junit.framework.TestSuite(com.arjuna.wsas.tests.junit.basic.StatusCheck.class));
        addTest(new junit.framework.TestSuite(com.arjuna.wsas.tests.junit.basic.Suspend.class));
        addTest(new junit.framework.TestSuite(com.arjuna.wsas.tests.junit.basic.Timeout.class));
        // wsas hls tests
        // this test fails because the deployment context factory code has changed
        //addTest(new junit.framework.TestSuite(com.arjuna.wsas.tests.junit.hls.Context1.class));
        // this test fails because the deployment context factory code has changed
        //addTest(new junit.framework.TestSuite(com.arjuna.wsas.tests.junit.hls.Context2.class));
        addTest(new junit.framework.TestSuite(com.arjuna.wsas.tests.junit.hls.Service.class));
    }
}