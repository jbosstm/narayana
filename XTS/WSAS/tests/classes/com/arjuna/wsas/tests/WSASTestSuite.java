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
 * (C) 2005-2008,
 * @author JBoss Inc.
 */
/*
 * TestSuite.java
 */

package com.arjuna.wsas.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
    @Suite.SuiteClasses({
            com.arjuna.wsas.tests.junit.basic.Context.class,
            com.arjuna.wsas.tests.junit.basic.Hierarchy.class,
            com.arjuna.wsas.tests.junit.basic.NestedActivity.class,
            com.arjuna.wsas.tests.junit.basic.NullEnd.class,
            com.arjuna.wsas.tests.junit.basic.Resume.class,
            com.arjuna.wsas.tests.junit.basic.StartEnd.class,
            com.arjuna.wsas.tests.junit.basic.StatusCheck.class,
            com.arjuna.wsas.tests.junit.basic.Suspend.class,
            com.arjuna.wsas.tests.junit.basic.Timeout.class,
            com.arjuna.wsas.tests.junit.hls.Context1.class,
            com.arjuna.wsas.tests.junit.hls.Context2.class,
            com.arjuna.wsas.tests.junit.hls.Service.class
    })
public class WSASTestSuite
{
}