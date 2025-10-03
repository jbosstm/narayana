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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package com.jboss.transaction.txinterop.test;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.AssertionFailedError;

import java.util.List;
import java.util.LinkedList;

/**
 * TestResult which holds list of passed tests, not only failed
 * or error tests, and time of executions of them.
 * @author <a href="mailto:istudens@redhat.com">Ivo Studensky</a>
 * @version <tt>$Revision$</tt>
 */
public class FullTestResult extends TestResult
{
   private List _passedTests;
   private List _failedTests;
   private List _errorTests;

   private long                 _startTime            = 0;
   private boolean              _failed               = false;
   private boolean              _error                = false;
   private AssertionFailedError _assertionFailedError = null;
   private Throwable            _throwable            = null;

   public FullTestResult()
   {
      super();

      _passedTests        = new LinkedList();
      _failedTests        = new LinkedList();
      _errorTests         = new LinkedList();
   }

   public List getPassedTests()
   {
      return _passedTests;
   }

   public List getFailedTests()
   {
      return _failedTests;
   }

   public List getErrorTests()
   {
      return _errorTests;
   }

   /*-----------  OVERWRITES  ----------*/
   public void startTest(Test test)
   {
      super.startTest(test);

      _startTime            = System.currentTimeMillis();
      _failed               = false;
      _error                = false;
      _assertionFailedError = null;
      _throwable            = null;
   }

   public void addError(Test test, Throwable throwable)
   {
      super.addError(test, throwable);

      _error     = true;
      _throwable = throwable;
   }

   public void addFailure(Test test, AssertionFailedError assertionFailedError)
   {
      super.addFailure(test, assertionFailedError);

      _failed               = true;
      _assertionFailedError = assertionFailedError;
   }

   public void endTest(Test test)
   {
      super.endTest(test);

      if (_failed)
      {
          FailedTest failedTest           = new FailedTest();
          failedTest.test                 = test;
          failedTest.duration             = System.currentTimeMillis() - _startTime;
          failedTest.assertionFailedError = _assertionFailedError;
          _failedTests.add(failedTest);
      }
      else if (_error)
      {
          ErrorTest errorTest = new ErrorTest();
          errorTest.test      = test;
          errorTest.duration  = System.currentTimeMillis() - _startTime;
          errorTest.throwable = _throwable;
          _errorTests.add(errorTest);
      }
      else
      {
          PassedTest passedTest = new PassedTest();
          passedTest.test       = test;
          passedTest.duration   = System.currentTimeMillis() - _startTime;
          _passedTests.add(passedTest);
      }
   }

   /*----------   SUBCLASSES   -----------*/
   public class PassedTest
   {
      public Test test;
      public long duration;
   }

   public class FailedTest
   {
      public Test                 test;
      public long                 duration;
      public AssertionFailedError assertionFailedError;
   }

   public class ErrorTest
   {
      public Test      test;
      public long      duration;
      public Throwable throwable;
   }

}
