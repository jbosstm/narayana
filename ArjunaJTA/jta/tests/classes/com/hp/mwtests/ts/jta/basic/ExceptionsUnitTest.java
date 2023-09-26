/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.hp.mwtests.ts.jta.basic;

import org.junit.Test;

import com.arjuna.ats.jta.exceptions.InactiveTransactionException;
import com.arjuna.ats.jta.exceptions.NotImplementedException;
import com.arjuna.ats.jta.exceptions.RollbackException;
import com.arjuna.ats.jta.exceptions.UnexpectedConditionException;

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