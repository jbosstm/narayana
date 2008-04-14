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
package com.arjuna.webservices.wsaddr.policy;

import com.arjuna.webservices.HandlerRegistry;
import com.arjuna.webservices.wsaddr.AddressingConstants;
import com.arjuna.webservices.wsaddr.handlers.ActionHandler;
import com.arjuna.webservices.wsaddr.handlers.AddressingContextHandler;
import com.arjuna.webservices.wsaddr.handlers.FaultToHandler;
import com.arjuna.webservices.wsaddr.handlers.FromHandler;
import com.arjuna.webservices.wsaddr.handlers.MessageIDHandler;
import com.arjuna.webservices.wsaddr.handlers.RelationshipHandler;
import com.arjuna.webservices.wsaddr.handlers.ReplyToHandler;
import com.arjuna.webservices.wsaddr.handlers.ToHandler;

/**
 * Policy responsible for binding in the WS-Addressing header handlers.
 * @author kevin
 */
public class AddressingPolicy
{
    /**
     * Add this policy to the registry.
     * @param registry The registry containing the policy.
     */
    public static void register(final HandlerRegistry registry)
    {
        registry.registerHeaderHandler(AddressingConstants.WSA_ELEMENT_TO_QNAME, new ToHandler()) ;
        registry.registerHeaderHandler(AddressingConstants.WSA_ELEMENT_ACTION_QNAME, new ActionHandler()) ;
        registry.registerHeaderHandler(AddressingConstants.WSA_ELEMENT_MESSAGE_ID_QNAME, new MessageIDHandler()) ;
        registry.registerHeaderHandler(AddressingConstants.WSA_ELEMENT_FROM_QNAME, new FromHandler()) ;
        registry.registerHeaderHandler(AddressingConstants.WSA_ELEMENT_REPLY_TO_QNAME, new ReplyToHandler()) ;
        registry.registerHeaderHandler(AddressingConstants.WSA_ELEMENT_FAULT_TO_QNAME, new FaultToHandler()) ;
        registry.registerHeaderHandler(AddressingConstants.WSA_ELEMENT_RELATES_TO_QNAME, new RelationshipHandler()) ;
        registry.registerHeaderHandler(null, new AddressingContextHandler()) ;
    }

    /**
     * Remove this policy from the registry.
     * @param registry The registry containing the policy.
     */
    public static void remove(final HandlerRegistry registry)
    {
        registry.removeHeaderHandler(AddressingConstants.WSA_ELEMENT_TO_QNAME) ;
        registry.removeHeaderHandler(AddressingConstants.WSA_ELEMENT_ACTION_QNAME) ;
        registry.removeHeaderHandler(AddressingConstants.WSA_ELEMENT_MESSAGE_ID_QNAME) ;
        registry.removeHeaderHandler(AddressingConstants.WSA_ELEMENT_FROM_QNAME) ;
        registry.removeHeaderHandler(AddressingConstants.WSA_ELEMENT_REPLY_TO_QNAME) ;
        registry.removeHeaderHandler(AddressingConstants.WSA_ELEMENT_FAULT_TO_QNAME) ;
        registry.removeHeaderHandler(AddressingConstants.WSA_ELEMENT_RELATES_TO_QNAME) ;
    }
}
