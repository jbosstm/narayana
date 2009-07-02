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
package com.hp.mwtests.ts.arjuna.recovery;

/*
 * Copyright (C) 1999-2001 by HP Bluestone Software, Inc. All rights Reserved.
 *
 * HP Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: TransactionStatusManagerItemTest.java 2342 2006-03-30 13:06:17Z  $
 */

import java.net.*;

import com.arjuna.ats.arjuna.utils.Utility;
import com.arjuna.ats.internal.arjuna.recovery.TransactionStatusManagerItem;

import org.junit.Test;
import static org.junit.Assert.*;

public class TransactionStatusManagerItemTest
{
    @Test
    public void test() throws Exception
    {
        int _test_port = 4321;
        String _test_host = InetAddress.getLocalHost().getHostAddress();

        // Check that a transaction status manager item can be created correctly.

        assertTrue( TransactionStatusManagerItem.createAndSave(_test_port) );

        TransactionStatusManagerItem _tsmi = TransactionStatusManagerItem.get();

        assertEquals(_test_port, _tsmi.port());
        assertEquals(_test_host, _tsmi.host());

        // Check that a transaction status manager item can be re-created correctly.

        _tsmi = null;
        _tsmi = TransactionStatusManagerItem.recreate(Utility.getProcessUid());

        assertEquals(_test_port, _tsmi.port());
        assertEquals(_test_host, _tsmi.host());

    }
}
