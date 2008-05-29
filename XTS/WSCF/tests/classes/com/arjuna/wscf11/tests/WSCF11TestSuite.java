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

package com.arjuna.wscf11.tests;

public class WSCF11TestSuite extends junit.framework.TestSuite
{
    public WSCF11TestSuite()
    {
        // wscf twophase tests
        addTest(new junit.framework.TestSuite(com.arjuna.wscf.tests.junit.model.twophase.StartEnd.class));
        addTest(new junit.framework.TestSuite(com.arjuna.wscf.tests.junit.model.twophase.BeginConfirm.class));
        addTest(new junit.framework.TestSuite(com.arjuna.wscf.tests.junit.model.twophase.BeginCancel.class));
        addTest(new junit.framework.TestSuite(com.arjuna.wscf.tests.junit.model.twophase.Suspend.class));
        addTest(new junit.framework.TestSuite(com.arjuna.wscf.tests.junit.model.twophase.SuspendResume.class));
        addTest(new junit.framework.TestSuite(com.arjuna.wscf.tests.junit.model.twophase.SuspendConfirm.class));
        addTest(new junit.framework.TestSuite(com.arjuna.wscf.tests.junit.model.twophase.CancelOnlyCancel.class));
        addTest(new junit.framework.TestSuite(com.arjuna.wscf.tests.junit.model.twophase.CancelOnlyConfirm.class));
        addTest(new junit.framework.TestSuite(com.arjuna.wscf.tests.junit.model.twophase.AddParticipant.class));
        addTest(new junit.framework.TestSuite(com.arjuna.wscf.tests.junit.model.twophase.SuspendParticipant.class));
        addTest(new junit.framework.TestSuite(com.arjuna.wscf.tests.junit.model.twophase.AddSynchronization.class));
        addTest(new junit.framework.TestSuite(com.arjuna.wscf.tests.junit.model.twophase.ParticipantSynchronization.class));
        // this test relies on an invalid, out of date DOM implementation
        //addTest(new junit.framework.TestSuite(com.arjuna.wscf.tests.junit.model.twophase.ContextOutput.class));
        // TODO -- these tests break because ArjunaContextImple.toString() is broken. fix after 4.3.0 release
        //addTest(new junit.framework.TestSuite(com.arjuna.wscf.tests.junit.model.twophase.WscContext.class));
        //addTest(new junit.framework.TestSuite(com.arjuna.wscf.tests.junit.model.twophase.WscNestedContext.class));
        // this test relies on an invalid, out of date DOM implementation
        //addTest(new junit.framework.TestSuite(com.arjuna.wscf.tests.junit.model.twophase.WscTranslateContext.class));
    }
}