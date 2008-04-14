package com.arjuna.webservices11.wsat;

import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices.SoapFault;

import javax.xml.ws.addressing.AddressingProperties;
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
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void commit(final Notification commit, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext) ;

    /**
     * Handle the prepare event.
     * @param prepare The prepare notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void prepare(final Notification prepare, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext) ;

    /**
     * Handle the rollback event.
     * @param rollback The rollback notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void rollback(final Notification rollback, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext) ;

    /**
     * Handle the soap fault event.
     * @param soapFault The soap fault.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void soapFault(final SoapFault soapFault, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext) ;
    /**
     * Return the endpoint reference for the coordinator associated with this participant.
     * @return the coordinator endpoint reference
     */
    public W3CEndpointReference getCoordinator();
}
