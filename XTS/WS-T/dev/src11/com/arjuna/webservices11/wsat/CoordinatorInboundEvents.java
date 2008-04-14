package com.arjuna.webservices11.wsat;

import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices.SoapFault;

import javax.xml.ws.addressing.AddressingProperties;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import org.oasis_open.docs.ws_tx.wsat._2006._06.Notification;

/**
 * The Coordinator events.
 */
public interface CoordinatorInboundEvents
{
    /**
     * Handle the aborted event.
     * @param aborted The aborted notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void aborted(final Notification aborted, final AddressingProperties addressingContext, final ArjunaContext arjunaContext) ;

    /**
     * Handle the committed event.
     * @param committed The committed notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void committed(final Notification committed, final AddressingProperties addressingContext, final ArjunaContext arjunaContext) ;

    /**
     * Handle the prepared event.
     * @param prepared The prepared notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void prepared(final Notification prepared, final AddressingProperties addressingContext, final ArjunaContext arjunaContext) ;

    /**
     * Handle the readOnly event.
     * @param readOnly The readOnly notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void readOnly(final Notification readOnly, final AddressingProperties addressingContext, final ArjunaContext arjunaContext) ;

    /**
     * Handle the soap fault event.
     * @param soapFault The soap fault.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void soapFault(final SoapFault soapFault, final AddressingProperties addressingContext, final ArjunaContext arjunaContext) ;

    /**
     * Get the participant endpoint reference
     * @return The participant endpoint reference
     */
    public W3CEndpointReference getParticipant();
}
