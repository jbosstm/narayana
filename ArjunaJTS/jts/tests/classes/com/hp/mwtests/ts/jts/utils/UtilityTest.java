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
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Util.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.utils;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.PrintWriter;

import org.junit.Test;

import org.omg.CosTransactions.otid_t;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.jts.exceptions.ExceptionCodes;
import com.arjuna.ats.jts.utils.Utility;

public class UtilityTest
{
    @Test
    public void test()
    {
        Uid txId = new Uid();
        otid_t tid = Utility.uidToOtid(txId);
        Uid u = Utility.otidToUid(tid);

        assertTrue(txId.equals(u));
    }
    
    @Test
    public void testPrint ()
    {
        Utility.printStatus(new PrintWriter(System.err), org.omg.CosTransactions.Status.StatusNoTransaction);
        
        String vote = Utility.stringVote(org.omg.CosTransactions.Vote.VoteCommit);
        
        assertTrue(vote != null);
        assertEquals(vote, "CosTransactions::VoteCommit");
        
        String status = Utility.stringStatus(org.omg.CosTransactions.Status.StatusNoTransaction);
        
        assertTrue(status != null);
        assertEquals(status, "CosTransactions::StatusNoTransaction");
    }
    
    @Test
    public void testExceptions ()
    {
        ExceptionCodes x = new ExceptionCodes();
        
        for (int i = ExceptionCodes.OTS_GENERAL_BASE; i < ExceptionCodes.NO_TXCONTEXT; i++)
        {
            assertTrue(Utility.exceptionCode(i).length() > 1);
        }
    }
}
