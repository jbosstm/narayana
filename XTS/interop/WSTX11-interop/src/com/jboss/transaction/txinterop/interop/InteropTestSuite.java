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
package com.jboss.transaction.txinterop.interop;


import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Interop testsuite to initialise the participant uri on addition.
 * @author kevin
 */
public class InteropTestSuite extends TestSuite
{
    /**
     * The participant URI for this test.
     */
    private final String participantURI ;
    /**
     * The test timeout.
     */
    private final long testTimeout ;
    /**
     * The asynchronous test flag.
     */
    private final boolean asyncTest ;
    
    /**
     * Construct an empty test suite with the specified participant URI.
     * @param testTimeout The test timeout.
     * @param participantURI The participant URI.
     */
    public InteropTestSuite(final String participantURI, final long testTimeout, final boolean asyncTest)
    {
        super() ;
        this.participantURI = participantURI ;
        this.testTimeout = testTimeout ;
        this.asyncTest = asyncTest ;
    }
    
    /**
     * Construct an empty test suite with the specified participant URI and name.
     * @param participantURI The participant URI.
     * @param testTimeout The test timeout.
     * @param name The name of the test suite.
     */
    public InteropTestSuite(final String participantURI, final long testTimeout, final boolean asyncTest, final String name)
    {
        super(name) ;
        this.participantURI = participantURI ;
        this.testTimeout = testTimeout ;
        this.asyncTest = asyncTest ;
    }

    /**
     * Constructs a TestSuite from the given class using the specified participant URI.
     * @param participantURI The participant URI.
     * @param testTimeout The test timeout.
     * @param clazz The class containing the tests.
     */
    public InteropTestSuite(final String participantURI, final long testTimeout, final boolean asyncTest, final Class clazz)
    {
        super(clazz) ;
        this.participantURI = participantURI ;
        this.testTimeout = testTimeout ;
        this.asyncTest = asyncTest ;
        final int numTests = countTestCases() ;
        for(int count = 0 ; count < numTests ; count++)
        {
            initialiseTest(testAt(count)) ;
        }
    }
    
    /**
     * Constructs a TestSuite from the given class using the specified participant URI and name.
     * @param participantURI The participant URI.
     * @param testTimeout The test timeout.
     * @param clazz The class containing the tests.
     * @param name The name of the test suite.
     */
    public InteropTestSuite(final String participantURI, final long testTimeout, final boolean asyncTest, final Class clazz, final String name)
    {
        super(clazz, name) ;
        this.participantURI = participantURI ;
        this.testTimeout = testTimeout ;
        this.asyncTest = asyncTest ;
    }

    /**
     * Add a test to the test suite.
     * @param test The test to add.
     */
    public void addTest(final Test test)
    {
        initialiseTest(test) ;
        super.addTest(test) ;
    }
    
    /**
     * Perform initialisation on the test.
     * @param test The test to initialise.
     */
    private void initialiseTest(final Test test)
    {
        if (test instanceof InteropTestCase)
        {
            final InteropTestCase interopTestCase = (InteropTestCase)test ;
            interopTestCase.setParticipantURI(participantURI) ;
            interopTestCase.setTestTimeout(testTimeout) ;
            interopTestCase.setAsyncTest(asyncTest) ;
        }
    }
}
