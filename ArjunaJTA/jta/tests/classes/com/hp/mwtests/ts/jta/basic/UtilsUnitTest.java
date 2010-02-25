/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
 * (C) 2005-2010,
 * @author JBoss Inc.
 */

package com.hp.mwtests.ts.jta.basic;

import org.junit.Test;

import com.arjuna.ats.jta.utils.JTAHelper;
import com.arjuna.ats.jta.utils.XAHelper;

import static org.junit.Assert.*;

public class UtilsUnitTest
{
    @Test
    public void testJTAHelper () throws Exception
    {
        assertEquals(JTAHelper.stringForm(javax.transaction.Status.STATUS_ACTIVE), "javax.transaction.Status.STATUS_ACTIVE");
        assertEquals(JTAHelper.stringForm(javax.transaction.Status.STATUS_COMMITTED), "javax.transaction.Status.STATUS_COMMITTED");
        assertEquals(JTAHelper.stringForm(javax.transaction.Status.STATUS_MARKED_ROLLBACK), "javax.transaction.Status.STATUS_MARKED_ROLLBACK");
        assertEquals(JTAHelper.stringForm(javax.transaction.Status.STATUS_NO_TRANSACTION), "javax.transaction.Status.STATUS_NO_TRANSACTION");
        assertEquals(JTAHelper.stringForm(javax.transaction.Status.STATUS_PREPARED), "javax.transaction.Status.STATUS_PREPARED");
        assertEquals(JTAHelper.stringForm(javax.transaction.Status.STATUS_PREPARING), "javax.transaction.Status.STATUS_PREPARING");
        assertEquals(JTAHelper.stringForm(javax.transaction.Status.STATUS_ROLLEDBACK), "javax.transaction.Status.STATUS_ROLLEDBACK");
        assertEquals(JTAHelper.stringForm(javax.transaction.Status.STATUS_ROLLING_BACK), "javax.transaction.Status.STATUS_ROLLING_BACK");
        assertEquals(JTAHelper.stringForm(javax.transaction.Status.STATUS_UNKNOWN), "javax.transaction.Status.STATUS_UNKNOWN");
    }
    
    @Test
    public void testXAHelper () throws Exception
    {
        assertTrue(XAHelper.printXAErrorCode(null) != null);
    }
}
