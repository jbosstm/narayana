package com.jboss.transaction.txinterop.proxy;

import org.xml.sax.ContentHandler;

/**
 * Interface representing a conversation state.
 */
public interface ProxyConversationState
{
    /**
     * Handle the next action in the sequence.
     * @param action The SOAP action.
     * @param identifier The identifier associated with the endpoint.
     * @return true if the message should be dropped, false otherwise.
     */
    public boolean handleAction(final String action, final String identifier) ;

    /**
     * Get the Handler for rewriting the XML.
     * @param nextHandler The next handler in the sequence.
     * @return The handler or null if no rewriting required.
     */
    public ContentHandler getHandler(final ContentHandler nextHandler) ;
}
