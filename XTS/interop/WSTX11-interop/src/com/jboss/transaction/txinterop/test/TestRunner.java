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

import com.jboss.transaction.txinterop.interop.ATTestCase;
import com.jboss.transaction.txinterop.interop.BATestCase;
import com.jboss.transaction.txinterop.interop.InteropTestCase;
import com.jboss.transaction.txinterop.interop.InteropTestSuite;
import com.jboss.transaction.txinterop.interop.MessageLogging;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

/**
 * Class responsible for executing the appropriate JUnit test.
 * @author kevin
 */
public class TestRunner
{
    /**
     * The log message prefix.
     */
    private static final String LOG_MESSAGE_PREFIX = "<log:log xmlns:log=\"http://docs.oasis-open.org/ws-tx/logs/\">" ;
    /**
     * The log message suffix.
     */
    private static final String LOG_MESSAGE_SUFFIX = "</log:log>";
    
    /**
     * The AT test class.
     */
    private static final Class AT_TEST_CLASS = ATTestCase.class ;
    /**
     * The BA test class.
     */
    private static final Class BA_TEST_CLASS = BATestCase.class ;
    
    /**
     * Execute the specific test against the specified participant.
     * @param participantURI The URI of the participant.
     * @param testTimeout The test timeout.
     * @param asyncTest The asynchronous test flag.
     * @param testName The name of the test to execute.
     * @return The test result.
     */
    public static TestResult execute(final String participantURI, final long testTimeout, final boolean asyncTest, final String testName)
    {
        MessageLogging.clearThreadLog() ;
        final Test test ;
        if (TestConstants.NAME_ALL_TESTS.equals(testName))
        {
            final TestSuite testSuite = new TestSuite() ;
            testSuite.addTest(new InteropTestSuite(participantURI, testTimeout, asyncTest, AT_TEST_CLASS)) ;
            testSuite.addTest(new InteropTestSuite(participantURI, testTimeout, asyncTest, BA_TEST_CLASS)) ;
            test = testSuite ;
        }
        else if (TestConstants.NAME_ALL_AT_TESTS.equals(testName))
        {
            test = new InteropTestSuite(participantURI, testTimeout, asyncTest, AT_TEST_CLASS) ;
        }
        else if (TestConstants.NAME_ALL_BA_TESTS.equals(testName))
        {
            test = new InteropTestSuite(participantURI, testTimeout, asyncTest, BA_TEST_CLASS) ;
        }
        else if (testName.startsWith(TestConstants.PREFIX_AT_TESTS))
        {
            final Class testClass = AT_TEST_CLASS ;
            try
            {
                test = createTest(testClass, participantURI, testTimeout, asyncTest, testName) ;
            }
            catch (final Throwable th)
            {
                System.err.println("Unexpected error instantiating test class: " + th) ;
                return null ;
            }
        }
        else if (testName.startsWith(TestConstants.PREFIX_BA_TESTS))
        {
            final Class testClass = BA_TEST_CLASS ;
            try
            {
                test = createTest(testClass, participantURI, testTimeout, asyncTest, testName) ;
            }
            catch (final Throwable th)
            {
                System.err.println("Unexpected error instantiating test class: " + th) ;
                return null ;
            }
        }
        else
        {
            System.err.println("Unidentified test name: " + testName) ;
            return null ;
        }
        MessageLogging.appendThreadLog(LOG_MESSAGE_PREFIX) ;
        final TestResult testResult = new FullTestResult() ;
        test.run(testResult) ;
        MessageLogging.appendThreadLog(LOG_MESSAGE_SUFFIX) ;
        return testResult ;
    }
    
    /**
     * Create the test instance.
     * @param testClass The test class name.
     * @param participantURI The participant URI.
     * @param testTimeout The test timeout.
     * @param asyncTest The asynchronous test flag.
     * @param testName The test name.
     * @return The test instance.
     * @throws IllegalAccessException For access exception instantiating the test class.
     * @throws InstantiationException For errors instantiating the test class.
     * @throws IllegalArgumentException For an invalid test class.
     */
    private static TestCase createTest(final Class testClass, final String participantURI, final long testTimeout, final boolean asyncTest, final String testName)
        throws IllegalAccessException, InstantiationException, IllegalArgumentException
    {
        final Object testObject = testClass.newInstance() ;
        if (testObject instanceof InteropTestCase)
        {
            final InteropTestCase interopTestCase = (InteropTestCase)testObject ;
            interopTestCase.setParticipantURI(participantURI) ; 
            interopTestCase.setTestTimeout(testTimeout) ; 
            interopTestCase.setAsyncTest(asyncTest) ; 
            interopTestCase.setName(testName) ;
            return interopTestCase ;
        }
        else if (testObject instanceof TestCase)
        {
            final TestCase test = (TestCase)testObject ;
            test.setName(testName) ;
            return test ;
        }
        throw new IllegalArgumentException("Invalid class: " + testClass.getName()) ;
    }

}
