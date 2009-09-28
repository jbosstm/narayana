/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package com.arjuna.wst11.tests.junit;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsat.processors.CompletionInitiatorCallback;
import org.jboss.wsf.common.addressing.MAP;
import org.oasis_open.docs.ws_tx.wsat._2006._06.Notification;

/**              B
 * Base callback class for tests.
 * @author kevin
 */
public class TestCompletionInitiatorCallback extends CompletionInitiatorCallback
{
    /**
     * An aborted response.
     * @param aborted The aborted notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void aborted(final Notification aborted, final MAP map,
        final ArjunaContext arjunaContext)
    {
        throw new RuntimeException("Unexpected aborted response") ;
    }

    /**
     * A committed response.
     * @param committed The committed notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void committed(final Notification committed, final MAP map,
        final ArjunaContext arjunaContext)
    {
        throw new RuntimeException("Unexpected committed response") ;
    }

    /**
     * A SOAP fault response.
     * @param soapFault The SOAP fault.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void soapFault(final SoapFault soapFault, final MAP map,
        final ArjunaContext arjunaContext)
    {
        throw new RuntimeException("Unexpected SOAP fault response") ;
    }
}