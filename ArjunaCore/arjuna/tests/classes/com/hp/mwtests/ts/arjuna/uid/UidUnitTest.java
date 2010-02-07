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
package com.hp.mwtests.ts.arjuna.uid;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;

import com.arjuna.ats.arjuna.common.*;
import org.junit.Test;

import static org.junit.Assert.*;

public class UidUnitTest
{
    @Test
    public void test () throws Exception
    {
        long[] dummy = {0, 0};
        
        Uid u = new Uid(dummy, 0, 0, 0);
        
        assertTrue(u.lessThan(Uid.maxUid()));
        assertTrue(u.getHexPid() != null);
        
        u.print(new PrintStream("foo"));
    }
    
    @Test
    public void testSerialization () throws Exception
    {
        Uid u1 = new Uid();
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(bs);
        
        os.writeObject(u1);
        
        ByteArrayInputStream bis = new ByteArrayInputStream(bs.toByteArray());
        ObjectInputStream is = new ObjectInputStream(bis);
        
        Uid u2 = (Uid) is.readObject();
        
        assertTrue(u1.equals(u2));
    }

}
