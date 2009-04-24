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
