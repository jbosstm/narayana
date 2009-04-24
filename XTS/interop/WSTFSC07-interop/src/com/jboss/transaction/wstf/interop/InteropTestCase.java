package com.jboss.transaction.wstf.interop;

import junit.framework.TestCase;
import junit.framework.TestResult;

import com.jboss.transaction.wstf.proxy.ProxyConversation;

/**
 * Base class for interop tests.
 * @author kevin
 */
public class InteropTestCase extends TestCase
{
    /**
     * The log message prefix start.
     */
    private static final String LOG_MESSAGE_PREFIX_START = "<log:message testcase=\"" ;
    /**
     * The log message prefix centre.
     */
    private static final String LOG_MESSAGE_PREFIX_CENTRE = "\" message=\"" ;
    /**
     * The log message prefix end.
     */
    private static final String LOG_MESSAGE_PREFIX_END = "\"><log:content>" ;
    /**
     * The log message suffix.
     */
    private static final String LOG_MESSAGE_SUFFIX = "</log:content></log:message>" ;
    
    /**
     * The URI of the participant.
     */
    private String participantURI ;
    
    /**
     * The conversation id for the test.
     */
    private String conversationId ;
    
    /**
     * The test timeout value.
     */
    private long testTimeout ;
    /**
     * The asynchronous test flag.
     */
    private boolean asyncTest ;
    
    /**
     * Set the participant URI.
     * @param participantURI The participant URI.
     */
    public void setParticipantURI(final String participantURI)
    {
        this.participantURI = participantURI ;
    }
    
    /**
     * Get the participant URI.
     * @return The participant URI.
     */
    protected String getParticipantURI()
    {
        return participantURI ;
    }
    
    /**
     * Set the test timeout value.
     * testTimeout The test timeout value.
     */
    public void setTestTimeout(final long testTimeout)
    {
        this.testTimeout = testTimeout ;
    }
    
    /**
     * Get the asynchronous test flag.
     * @return The asynchronous test flag.
     */
    protected boolean getAsyncTest()
    {
        return asyncTest ;
    }
    
    /**
     * Set the asynchronous test flag.
     * asyncTest The asynchronous test flag.
     */
    public void setAsyncTest(final boolean asyncTest)
    {
        this.asyncTest = asyncTest ;
    }
    
    /**
     * Get the test timeout value.
     * @return The test timeout value.
     */
    protected long getTestTimeout()
    {
        return testTimeout ;
    }
    
    /**
     * Get the conversation id.
     * @return The conversation id.
     */
    protected String getConversationId()
    {
        return conversationId ;
    }
    
    /**
     * Wraps the run method to include logging of message interactions.
     * @param result The test result. 
     */
    public void run(final TestResult result)
    {
        // get a conversation id, start the log, clear it on finish.
        conversationId = ProxyConversation.createConversation() ;
        try
        {
            super.run(result) ;
        }
        finally
        {
            final String[] messages = ProxyConversation.removeConversation(conversationId) ;
            conversationId = null ;
            if (messages != null)
            {
                final int numMessages = messages.length ;
                final StringBuffer buffer = new StringBuffer() ;
                int count = 0 ;
                while(count < numMessages)
                {
                    final String message = messages[count] ;
                    count++ ;
                    if (message != null)
                    {
                        buffer.append(LOG_MESSAGE_PREFIX_START) ;
                        buffer.append(getName()) ;
                        buffer.append(LOG_MESSAGE_PREFIX_CENTRE) ;
                        buffer.append(count) ;
                        buffer.append(LOG_MESSAGE_PREFIX_END) ;
                        buffer.append(message) ;
                        buffer.append(LOG_MESSAGE_SUFFIX) ;
                    }
                }
                MessageLogging.appendThreadLog(buffer.toString()) ;
            }
        }
    }
    
    /**
     * Return the string represenation of this test.
     * @return the string representation.
     */
    public String toString()
    {
        return getName() ;
    }
}
