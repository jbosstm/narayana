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
/*
 * Copyright (C) 2004,
 *
 * Arjuna Technologies Ltd,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: xidcheck.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.recovery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.omg.CosTransactions.Status;

import com.arjuna.ats.internal.jts.recovery.transactions.ExpiredServerScanner;
import com.arjuna.ats.internal.jts.recovery.transactions.ExpiredToplevelScanner;
import com.hp.mwtests.ts.jts.resources.TestBase;

public class ExpiredAssumedCompleteScannerUnitTest extends TestBase
{
    @Test
    public void testServerScanner () throws Exception
    {
        ExpiredServerScanner scanner = new ExpiredServerScanner();
        
        assertTrue(scanner.toBeUsed());
        
        scanner.scan();
    }
    
    @Test
    public void testToplevelScanner () throws Exception
    {
        ExpiredToplevelScanner scanner = new ExpiredToplevelScanner();
        
        assertTrue(scanner.toBeUsed());
        
        scanner.scan();
    }
}
