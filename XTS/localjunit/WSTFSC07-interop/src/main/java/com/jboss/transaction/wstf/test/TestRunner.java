/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.jboss.transaction.wstf.test;

import java.lang.reflect.InvocationTargetException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import com.jboss.transaction.wstf.interop.Sc007TestCase;
import com.jboss.transaction.wstf.interop.InteropTestCase;
import com.jboss.transaction.wstf.interop.InteropTestSuite;
import com.jboss.transaction.wstf.interop.MessageLogging;

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
    private static final Class SC007_TEST_CLASS = Sc007TestCase.class ;

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
            testSuite.addTest(new InteropTestSuite(participantURI, testTimeout, asyncTest, SC007_TEST_CLASS)) ;
            test = testSuite ;
        }
        else if (testName.startsWith(TestConstants.PREFIX_TESTS))
        {
            final Class testClass = SC007_TEST_CLASS;
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
        throws IllegalAccessException, InstantiationException, IllegalArgumentException, NoSuchMethodException, InvocationTargetException
    {
        final Object testObject = testClass.getDeclaredConstructor().newInstance();
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