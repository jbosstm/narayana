package com.arjuna.webservices11.wsat.processors;

import com.arjuna.webservices.base.processors.Callback;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsaddr.map.MAP;
import com.arjuna.webservices.SoapFault;
import org.oasis_open.docs.ws_tx.wsat._2006._06.Notification;

/**
 * The Completion Initiator callback.
 * @author kevin
 */
public abstract class CompletionInitiatorCallback extends Callback
{
    /**
     * An aborted response.
     * @param aborted The aborted notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void aborted(final Notification aborted, final MAP map,
        final ArjunaContext arjunaContext) ;

    /**
     * A committed response.
     * @param committed The committed notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void committed(final Notification committed, final MAP map,
        final ArjunaContext arjunaContext) ;

    /**
     * A SOAP fault response.
     * @param soapFault The SOAP fault.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void soapFault(final SoapFault soapFault, final MAP map,
        final ArjunaContext arjunaContext) ;
}
