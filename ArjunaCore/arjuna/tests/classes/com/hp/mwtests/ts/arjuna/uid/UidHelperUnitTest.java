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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;

public class UidHelperUnitTest
{
    @Test
    public void test () throws Exception
    {
        Uid u = new Uid("hello", true);  // should be invalid!
        
        assertEquals(u.valid(), false);
        
        try
        {
            u = UidHelper.unpackFrom(null);
            
            fail();
        }
        catch (final IllegalArgumentException ex)
        {
        }
        
        try
        {
            UidHelper.packInto(null, new OutputObjectState());
            
            fail();
        }
        catch (final IllegalArgumentException ex)
        {
        }
        
        try
        {
            UidHelper.packInto(u, new OutputObjectState());
            
            fail();
        }
        catch (final IllegalArgumentException ex)
        {
        }
    }
}
