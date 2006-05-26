/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
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
package com.arjuna.webservices.wsaddr2005.policy;

import com.arjuna.webservices.HandlerRegistry;
import com.arjuna.webservices.InterceptorHandler;
import com.arjuna.webservices.wsaddr2005.AddressingConstants;
import com.arjuna.webservices.wsaddr2005.handlers.ActionHandler;
import com.arjuna.webservices.wsaddr2005.handlers.AddressingContextHandler;
import com.arjuna.webservices.wsaddr2005.handlers.AddressingInterceptorHandler;
import com.arjuna.webservices.wsaddr2005.handlers.FaultToHandler;
import com.arjuna.webservices.wsaddr2005.handlers.FromHandler;
import com.arjuna.webservices.wsaddr2005.handlers.MessageIDHandler;
import com.arjuna.webservices.wsaddr2005.handlers.RelationshipHandler;
import com.arjuna.webservices.wsaddr2005.handlers.ReplyToHandler;
import com.arjuna.webservices.wsaddr2005.handlers.ToHandler;

/**
 * Policy responsible for binding in the WS-Addressing header handlers.
 * @author kevin
 */
public class AddressingPolicy
{
    /**
     * The addressing interceptor handler.
     */
    private static final InterceptorHandler interceptorHandler = new AddressingInterceptorHandler() ;
    
    /**
     * Add this policy to the registry.
     * @param registry The registry containing the policy.
     */
    public static void register(final HandlerRegistry registry)
    {
        registry.registerHeaderHandler(AddressingConstants.WSA_ELEMENT_QNAME_TO, new ToHandler()) ;
        registry.registerHeaderHandler(AddressingConstants.WSA_ELEMENT_QNAME_ACTION, new ActionHandler()) ;
        registry.registerHeaderHandler(AddressingConstants.WSA_ELEMENT_QNAME_MESSAGE_ID, new MessageIDHandler()) ;
        registry.registerHeaderHandler(AddressingConstants.WSA_ELEMENT_QNAME_FROM, new FromHandler()) ;
        registry.registerHeaderHandler(AddressingConstants.WSA_ELEMENT_QNAME_REPLY_TO, new ReplyToHandler()) ;
        registry.registerHeaderHandler(AddressingConstants.WSA_ELEMENT_QNAME_FAULT_TO, new FaultToHandler()) ;
        registry.registerHeaderHandler(AddressingConstants.WSA_ELEMENT_QNAME_RELATES_TO, new RelationshipHandler()) ;
        registry.registerHeaderHandler(null, new AddressingContextHandler()) ;
        registry.registerInterceptorHandler(interceptorHandler) ;
    }

    /**
     * Remove this policy from the registry.
     * @param registry The registry containing the policy.
     */
    public static void remove(final HandlerRegistry registry)
    {
        registry.removeHeaderHandler(AddressingConstants.WSA_ELEMENT_QNAME_TO) ;
        registry.removeHeaderHandler(AddressingConstants.WSA_ELEMENT_QNAME_ACTION) ;
        registry.removeHeaderHandler(AddressingConstants.WSA_ELEMENT_QNAME_MESSAGE_ID) ;
        registry.removeHeaderHandler(AddressingConstants.WSA_ELEMENT_QNAME_FROM) ;
        registry.removeHeaderHandler(AddressingConstants.WSA_ELEMENT_QNAME_REPLY_TO) ;
        registry.removeHeaderHandler(AddressingConstants.WSA_ELEMENT_QNAME_FAULT_TO) ;
        registry.removeHeaderHandler(AddressingConstants.WSA_ELEMENT_QNAME_RELATES_TO) ;
        registry.removeHeaderHandler(null) ;
        registry.removeInterceptorHandler(interceptorHandler) ;
    }
}
