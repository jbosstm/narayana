/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package com.arjuna.webservices.wsaddr2005.handlers;

import javax.xml.namespace.QName;

import com.arjuna.webservices.MessageContext;
import com.arjuna.webservices.stax.ElementContent;
import com.arjuna.webservices.wsaddr2005.AddressingContext;

/**
 * Extension of the standard addressing context to include fault information.
 * @author kevin
 */
class HandlerAddressingContext extends AddressingContext
{
    /**
     * The fault header name.
     */
    private QName faultHeaderName ;
    /**
     * The fault header.
     */
    private ElementContent faultHeader ;

    /**
     * The faulted To flag.
     */
    private boolean faultedTo ;
    /**
     * The faulted Action flag.
     */
    private boolean faultedAction ;
    /**
     * The faulted MessageID flag.
     */
    private boolean faultedMessageID ;
    /**
     * The faulted From flag.
     */
    private boolean faultedFrom ;
    /**
     * The faulted ReplyTo flag.
     */
    private boolean faultedReplyTo ;
    /**
     * The faulted FaultTo flag.
     */
    private boolean faultedFaultTo ;

    /**
     * Default constructor.
     */
    private HandlerAddressingContext()
    {
    }

    /**
     * Get the fault header name.
     * @return The fault header name.
     */
    QName getFaultHeaderName()
    {
        return faultHeaderName ;
    }
    
    /**
     * Set the fault header name.
     * @param faultHeaderName The fault header name.
     */
    void setFaultHeaderName(final QName faultHeaderName)
    {
        this.faultHeaderName = faultHeaderName;
    }
    
    /**
     * Get the fault header.
     * @return The fault header.
     */
    ElementContent getFaultHeader()
    {
        return faultHeader ;
    }
    
    /**
     * Set the fault header.
     * @param faultHeader The fault header.
     */
    void setFaultHeader(final ElementContent faultHeader)
    {
        this.faultHeader = faultHeader;
    }

    /**
     * Has the To header been faulted?
     * @return true if it has faulted, false otherwise.
     */
    boolean isFaultedTo()
    {
        return faultedTo ;
    }
    
    /**
     * Set the faulted To flag.
     */
    void setFaultedTo()
    {
        faultedTo = true ;
    }

    /**
     * Has the Action header been faulted?
     * @return true if it has faulted, false otherwise.
     */
    boolean isFaultedAction()
    {
        return faultedAction ;
    }
    
    /**
     * Set the faulted Action flag.
     */
    void setFaultedAction()
    {
        faultedAction = true ;
    }

    /**
     * Has the MessageID header been faulted?
     * @return true if it has faulted, false otherwise.
     */
    boolean isFaultedMessageID()
    {
        return faultedMessageID ;
    }
    
    /**
     * Set the faulted MessageID flag.
     */
    void setFaultedMessageID()
    {
        faultedMessageID = true ;
    }

    /**
     * Has the From header been faulted?
     * @return true if it has faulted, false otherwise.
     */
    boolean isFaultedFrom()
    {
        return faultedFrom ;
    }
    
    /**
     * Set the faulted From flag.
     */
    void setFaultedFrom()
    {
        faultedFrom = true ;
    }

    /**
     * Has the ReplyTo header been faulted?
     * @return true if it has faulted, false otherwise.
     */
    boolean isFaultedReplyTo()
    {
        return faultedReplyTo ;
    }
    
    /**
     * Set the faulted ReplyTo flag.
     */
    void setFaultedReplyTo()
    {
        faultedReplyTo = true ;
    }

    /**
     * Has the FaultTo header been faulted?
     * @return true if it has faulted, false otherwise.
     */
    boolean isFaultedFaultTo()
    {
        return faultedFaultTo ;
    }
    
    /**
     * Set the faulted FaultTo flag.
     */
    void setFaultedFaultTo()
    {
        faultedFaultTo = true ;
    }

    /**
     * Get the addressing context from the message context if present.
     * @param messageContext The message context.
     * @return The addressing context or null if not present.
     */
    public static HandlerAddressingContext getHandlerContext(final MessageContext messageContext)
    {
        final HandlerAddressingContext addressingContext = (HandlerAddressingContext)messageContext.getProperty(ADDRESSING_CONTEXT_PROPERTY) ;
        if (addressingContext != null)
        {
            return addressingContext ;
        }
        final HandlerAddressingContext newAddressingContext = new HandlerAddressingContext() ;
        setContext(messageContext, newAddressingContext) ;
        return newAddressingContext ;
    }
}
