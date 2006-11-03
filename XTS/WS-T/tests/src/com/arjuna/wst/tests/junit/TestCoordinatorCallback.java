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
package com.arjuna.wst.tests.junit;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.wsaddr.AddressingContext;
import com.arjuna.webservices.wsarj.ArjunaContext;
import com.arjuna.webservices.wsat.NotificationType;
import com.arjuna.webservices.wsat.processors.CoordinatorCallback;

/**
 * Base callback class for tests.
 * @author kevin
 */
public class TestCoordinatorCallback extends CoordinatorCallback
{
    /**
     * An aborted response.
     * @param aborted The aborted notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void aborted(final NotificationType aborted, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext)
    {
        throw new RuntimeException("Unexpected aborted response") ;
    }
    
    /**
     * A committed response.
     * @param committed The committed notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void committed(final NotificationType committed, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext)
    {
        throw new RuntimeException("Unexpected committed response") ;
    }
    
    /**
     * A prepared response.
     * @param prepared The prepared notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void prepared(final NotificationType prepared, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext)
    {
        throw new RuntimeException("Unexpected prepared response") ;
    }
    
    /**
     * A read only response.
     * @param readOnly The read only notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void readOnly(final NotificationType readOnly, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext)
    {
        throw new RuntimeException("Unexpected read only response") ;
    }
    
    /**
     * A replay response.
     * @param replay The replay notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void replay(final NotificationType replay, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext)
    {
        throw new RuntimeException("Unexpected replay response") ;
    }
    
    /**
     * A SOAP fault response.
     * @param soapFault The SOAP fault.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void soapFault(final SoapFault soapFault, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext)
    {
        throw new RuntimeException("Unexpected SOAP fault response") ;
    }
}
