/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2007,
 * @author JBoss, a division of Red Hat.
 */
package com.hp.mwtests.ts.arjuna.common;

import org.junit.Test;

import com.arjuna.ats.arjuna.exceptions.FatalError;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreError;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;

import static org.junit.Assert.*;

public class ExceptionsUnitTest
{
    @Test
    public void test ()
    {
        FatalError fe = new FatalError();
        
        fe = new FatalError("problem");
        fe = new FatalError("problem", new NullPointerException());
        fe = new FatalError(new NullPointerException());
        
        ObjectStoreError os = new ObjectStoreError();
        
        os = new ObjectStoreError("problem");
        os = new ObjectStoreError("problem", new NullPointerException());
        os = new ObjectStoreError(new NullPointerException());
        
        ObjectStoreException ox = new ObjectStoreException();
        
        ox = new ObjectStoreException("problem");
        ox = new ObjectStoreException("problem", new NullPointerException());
        ox = new ObjectStoreException(new NullPointerException());
    }
}
