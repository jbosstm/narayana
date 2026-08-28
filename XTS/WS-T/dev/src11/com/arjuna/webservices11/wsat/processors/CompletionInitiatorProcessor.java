package com.arjuna.webservices11.wsat.processors;

import com.arjuna.webservices11.wsarj.processor.BaseNotificationProcessor;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import org.jboss.ws.api.addressing.MAP;
import com.arjuna.webservices.base.processors.Callback;
import com.arjuna.webservices.SoapFault;
import org.oasis_open.docs.ws_tx.wsat._2006._06.Notification;

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
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void handleAborted(final Notification aborted, final MAP map,
        final ArjunaContext arjunaContext)
    {
        handleCallbacks(new CallbackExecutorAdapter() {
            public void execute(final Callback callback) {
                ((CompletionInitiatorCallback)callback).aborted(aborted, map, arjunaContext) ;
            }
        }, getIDs(arjunaContext)) ;
    }

    /**
     * Handle a committed response.
     * @param committed The committed notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void handleCommitted(final Notification committed, final MAP map,
        final ArjunaContext arjunaContext)
    {
        handleCallbacks(new CallbackExecutorAdapter() {
            public void execute(final Callback callback) {
                ((CompletionInitiatorCallback)callback).committed(committed, map, arjunaContext) ;
            }
        }, getIDs(arjunaContext)) ;
    }

    /**
     * Handle a SOAP fault response.
     * @param soapFault The SOAP fault.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void handleSoapFault(final SoapFault soapFault, final MAP map,
        final ArjunaContext arjunaContext)
    {
        handleCallbacks(new CallbackExecutorAdapter() {
            public void execute(final Callback callback) {
                ((CompletionInitiatorCallback)callback).soapFault(soapFault, map, arjunaContext) ;
            }
        }, getIDs(arjunaContext)) ;
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
