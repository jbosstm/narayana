package com.arjuna.webservices11.wsat.processors;

import com.arjuna.webservices11.wsarj.processor.BaseNotificationProcessor;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsat.processors.CompletionInitiatorCallback;
import com.arjuna.webservices.base.processors.Callback;
import com.arjuna.webservices.SoapFault;
import org.oasis_open.docs.ws_tx.wsat._2006._06.Notification;

import javax.xml.ws.addressing.AddressingProperties;

/**
 * The Completion Initiator processor.
 * @author kevin
 */
public class CompletionInitiatorProcessor extends BaseNotificationProcessor
{
    /**
     * The initiator singleton.
     */
    private static final CompletionInitiatorProcessor PROCESSOR = new CompletionInitiatorProcessor() ;

    /**
     * Get the processor singleton.
     * @return The singleton.
     */
    public static CompletionInitiatorProcessor getProcessor()
    {
        return PROCESSOR;
    }

    /**
     * Handle an aborted response.
     * @param aborted The aborted notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void handleAborted(final Notification aborted, final AddressingProperties addressingProperties,
        final ArjunaContext arjunaContext)
    {
        handleCallbacks(new CallbackExecutorAdapter() {
            public void execute(final Callback callback) {
                ((CompletionInitiatorCallback)callback).aborted(aborted, addressingProperties, arjunaContext) ;
            }
        }, getIDs(addressingProperties, arjunaContext)) ;
    }

    /**
     * Handle a committed response.
     * @param committed The committed notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void handleCommitted(final Notification committed, final AddressingProperties addressingProperties,
        final ArjunaContext arjunaContext)
    {
        handleCallbacks(new CallbackExecutorAdapter() {
            public void execute(final Callback callback) {
                ((CompletionInitiatorCallback)callback).committed(committed, addressingProperties, arjunaContext) ;
            }
        }, getIDs(addressingProperties, arjunaContext)) ;
    }

    /**
     * Handle a SOAP fault response.
     * @param soapFault The SOAP fault.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void handleSoapFault(final SoapFault soapFault, final AddressingProperties addressingProperties,
        final ArjunaContext arjunaContext)
    {
        handleCallbacks(new CallbackExecutorAdapter() {
            public void execute(final Callback callback) {
                ((CompletionInitiatorCallback)callback).soapFault(soapFault, addressingProperties, arjunaContext) ;
            }
        }, getIDs(addressingProperties, arjunaContext)) ;
    }

    /**
     * Register a callback for the specific instance identifier.
     * @param instanceIdentifier The instance identifier.
     * @param callback The callback for the response.
     */
    public void registerCallback(final String instanceIdentifier, final CompletionInitiatorCallback callback)
    {
        register(instanceIdentifier, callback) ;
    }
}
