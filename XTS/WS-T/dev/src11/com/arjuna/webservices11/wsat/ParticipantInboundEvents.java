package com.arjuna.webservices11.wsat;

import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsaddr.map.MAP;
import com.arjuna.webservices.SoapFault;

import javax.xml.ws.wsaddressing.W3CEndpointReference;

import org.oasis_open.docs.ws_tx.wsat._2006._06.Notification;

/**
 * The Participant events.
 */
public interface ParticipantInboundEvents
{
    /**
     * Handle the commit event.
     * @param commit The commit notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void commit(final Notification commit, final MAP map, final ArjunaContext arjunaContext) ;

    /**
     * Handle the prepare event.
     * @param prepare The prepare notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void prepare(final Notification prepare, final MAP map, final ArjunaContext arjunaContext) ;

    /**
     * Handle the rollback event.
     * @param rollback The rollback notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void rollback(final Notification rollback, final MAP map, final ArjunaContext arjunaContext) ;

    /**
     * Handle the soap fault event.
     * @param soapFault The soap fault.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void soapFault(final SoapFault soapFault, final MAP map, final ArjunaContext arjunaContext) ;
    /**
     * Return the endpoint reference for the coordinator associated with this participant.
     * @return the coordinator endpoint reference
     */
    public W3CEndpointReference getCoordinator();
}
