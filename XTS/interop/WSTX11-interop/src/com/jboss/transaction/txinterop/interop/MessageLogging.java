package com.jboss.transaction.txinterop.interop;

/**
 * Class providing test message logging.
 * @author kevin
 */
public class MessageLogging
{
    /**
     * The thread local message log.
     */
    private static final ThreadLocal MESSAGE_LOG = new ThreadLocal() ;
    
    /**
     * Clear the log for the current thread.
     */
    public static void clearThreadLog()
    {
        MESSAGE_LOG.set(null) ;
    }
    
    /**
     * Get the thread log.
     * @return The thread log.
     */
    public static String getThreadLog()
    {
        final Object value = MESSAGE_LOG.get() ;
        return (value == null ? "" : value.toString()) ;
    }
    
    /**
     * Append a message to the thread log.
     * @param message The thread message to append.
     */
    public static void appendThreadLog(final String message)
    {
        final Object value = MESSAGE_LOG.get() ;
        final StringBuffer buffer ;
        if (value == null)
        {
            buffer = new StringBuffer(message) ;
            MESSAGE_LOG.set(buffer) ;
        }
        else
        {
            buffer = (StringBuffer)value ;
            buffer.append(message) ;
        }
    }
}
