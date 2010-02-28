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

import com.arjuna.ats.jta.exceptions.InactiveTransactionException;
import com.arjuna.ats.jta.exceptions.NotImplementedException;
import com.arjuna.ats.jta.exceptions.RollbackException;
import com.arjuna.ats.jta.exceptions.UnexpectedConditionException;

import static org.junit.Assert.*;

public class ExceptionsUnitTest
{
    @Test
    public void test () throws Exception
    {
        InactiveTransactionException ex = new InactiveTransactionException();
        
        ex = new InactiveTransactionException("foobar");
        
        NotImplementedException exp = new NotImplementedException();
        
        exp = new NotImplementedException("foobar");
        exp = new NotImplementedException("foobar", new NullPointerException());
        exp = new NotImplementedException(new NullPointerException());
        
        RollbackException exp2 = new RollbackException();
        
        exp2 = new RollbackException("foobar");
        exp2 = new RollbackException("foobar", new NullPointerException());
        exp2 = new RollbackException(new NullPointerException());
        
        UnexpectedConditionException ex2 = new UnexpectedConditionException();
        
        ex2 = new UnexpectedConditionException("foobar");
    }

}
