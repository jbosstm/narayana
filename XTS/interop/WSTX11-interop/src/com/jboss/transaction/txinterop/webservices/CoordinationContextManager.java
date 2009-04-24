package com.jboss.transaction.txinterop.webservices;

import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;

import javax.xml.ws.handler.MessageContext;

/**
 * Helper class for passing a coordination context in a SOAP header when using JaxWS
 */
public class CoordinationContextManager {
    /**
     * The key used for the coordination context within a message exchange.
     */
    private static final String COORDINATION_CONTEXT_PROPERTY = "org.jboss.xts.message.context.coordination.context.property";

    /**
     * The coordination context associated with the thread.
     */
    private static final ThreadLocal THREAD_CONTEXT = new ThreadLocal() ;

    /**
     * Get the coordination context from the message context if present.
     * @param messageContext The message context.
     * @return The coordination context or null if not present.
     */
    public static CoordinationContextType getContext(final MessageContext messageContext)
    {
        return (CoordinationContextType)messageContext.get(COORDINATION_CONTEXT_PROPERTY) ;
    }

    /**
     * Set the coordination context for the message context.
     * @param messageContext The message context.
     * @param coordinationContext The coordination context.
     */
    public static void setContext(final MessageContext messageContext, final CoordinationContextType coordinationContext)
    {
        messageContext.put(COORDINATION_CONTEXT_PROPERTY, coordinationContext) ;
        messageContext.setScope(COORDINATION_CONTEXT_PROPERTY, MessageContext.Scope.APPLICATION);
    }

    /**
     * Get the coordination context from the current thread if present.
     * @return The coordination context or null if not present.
     */
    public static CoordinationContextType getThreadContext()
    {
        return (CoordinationContextType)THREAD_CONTEXT.get() ;
    }

    /**
     * Set the coordination context for the current thread.
     * @param coordinationContext The coordination context.
     */
    public static void setThreadContext(final CoordinationContextType coordinationContext)
    {
        THREAD_CONTEXT.set(coordinationContext) ;
    }
}
