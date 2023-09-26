/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.wst.tests.arq;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsat.processors.CompletionInitiatorCallback;
import org.jboss.ws.api.addressing.MAP;
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