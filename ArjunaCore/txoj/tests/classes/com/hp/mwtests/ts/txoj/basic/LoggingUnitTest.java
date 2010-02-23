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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package com.hp.mwtests.ts.txoj.basic;

import org.junit.Test;

import com.arjuna.ats.txoj.logging.FacilityCode;

import static org.junit.Assert.*;

public class LoggingUnitTest
{
    @Test
    public void test ()
    {
	FacilityCode fc = new FacilityCode();
	
	assertEquals(fc.getLevel("FAC_CONCURRENCY_CONTROL"), FacilityCode.FAC_CONCURRENCY_CONTROL);
	assertEquals(fc.getLevel("FAC_LOCK_STORE"), FacilityCode.FAC_LOCK_STORE);
	assertEquals(fc.getLevel("foobar"), FacilityCode.FAC_NONE);

	assertEquals(fc.printString(FacilityCode.FAC_ALL), "FAC_ALL");
	assertEquals(fc.printString(FacilityCode.FAC_NONE), "FAC_NONE");
	assertEquals(fc.printString(FacilityCode.FAC_CONCURRENCY_CONTROL), "FAC_CONCURRENCY_CONTROL");
	assertEquals(fc.printString(FacilityCode.FAC_LOCK_STORE), "FAC_LOCK_STORE");
    }
}
