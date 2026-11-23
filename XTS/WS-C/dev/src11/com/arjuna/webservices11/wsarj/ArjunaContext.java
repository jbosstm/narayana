package com.arjuna.webservices11.wsarj;

import com.arjuna.webservices11.wsarj.InstanceIdentifier;

import javax.xml.ws.handler.MessageContext;

/**
 * The arjuna context.
 * @author kevin
 */
public class ArjunaContext
{
    /**
     * The key used for the arjuna context within a message exchange.
     */
    private static final String ARJUNAWS_CONTEXT_PROPERTY = "org.jboss.xts.ws.context";

    /**
     * The InstanceIdentifier header.
     */
    private InstanceIdentifier instanceIdentifier ;

    /**
     * Default constructor.
     */
    private ArjunaContext()
    {
    }

    /**
     * Get the instance identifier.
     * @return The instance identifier.
     */
    public InstanceIdentifier getInstanceIdentifier()
    {
        return instanceIdentifier ;
    }

    /**
     * Set the instance identifier.
     * @param instanceIdentifier The instance identifier.
     */
    public void setInstanceIdentifier(final InstanceIdentifier instanceIdentifier)
    {
        this.instanceIdentifier = instanceIdentifier ;
    }

    /**
     * Is the configuration of this element valid?
     * @return true if valid, false otherwise.
     */
    public boolean isValid()
    {
        return ((instanceIdentifier != null) && instanceIdentifier.isValid()) ;
    }

    /**
     * Get the arjuna context from the message context if present.
     * @param messageContext The message context.
     * @return The arjuna context or null if not present.
     */
    public static ArjunaContext getCurrentContext(final MessageContext messageContext)
    {
        return (ArjunaContext)messageContext.get(ARJUNAWS_CONTEXT_PROPERTY) ;
    }

    /**
     * Get the arjuna context from the message context.
     * @param messageContext The message context.
     * @return The arjuna context.
     */
    public static ArjunaContext getContext(final MessageContext messageContext)
    {
        final ArjunaContext current = (ArjunaContext)messageContext.get(ARJUNAWS_CONTEXT_PROPERTY) ;
        if (current != null)
        {
            return current ;
        }
        final ArjunaContext newContext = new ArjunaContext() ;
        messageContext.put(ARJUNAWS_CONTEXT_PROPERTY, newContext) ;
        messageContext.setScope(ARJUNAWS_CONTEXT_PROPERTY, MessageContext.Scope.APPLICATION);
        return newContext ;
    }
}
