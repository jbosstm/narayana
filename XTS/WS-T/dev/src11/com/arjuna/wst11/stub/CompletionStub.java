package com.arjuna.wst11.stub;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.wsarjtx.ArjunaTXConstants;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import org.jboss.ws.api.addressing.MAP;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wsat.client.CompletionCoordinatorClient;
import com.arjuna.webservices11.wsat.processors.CompletionInitiatorCallback;
import com.arjuna.webservices11.wsat.processors.CompletionInitiatorProcessor;
import com.arjuna.wsc11.messaging.MessageId;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.TransactionRolledBackException;
import com.arjuna.wst.UnknownTransactionException;
import com.arjuna.wst.CompletionCoordinatorParticipant;
import org.oasis_open.docs.ws_tx.wsat._2006._06.Notification;

import javax.xml.ws.wsaddressing.W3CEndpointReference;

public class CompletionStub implements
        CompletionCoordinatorParticipant
{
    private W3CEndpointReference _completionCoordinator = null;
    private String _id;

	public CompletionStub(final String id, final W3CEndpointReference completionCoordinator)
			throws Exception
	{
		_completionCoordinator = completionCoordinator;
		_id = id;
	}

	public void commit () throws TransactionRolledBackException,
            UnknownTransactionException, SystemException
    {
        final MAP map = AddressingHelper.createNotificationContext(MessageId.getMessageId()) ;

        final CompletionStub.RequestCallback callback = new CompletionStub.RequestCallback() ;
        final CompletionInitiatorProcessor completionInitiator = CompletionInitiatorProcessor.getProcessor() ;
        completionInitiator.registerCallback(_id, callback) ;
        try
        {
            CompletionCoordinatorClient.getClient().sendCommit(_completionCoordinator, map, new InstanceIdentifier(_id)) ;
            callback.waitUntilTriggered() ;
        }
        catch (final Throwable th)
        {
            th.printStackTrace() ;
            throw new SystemException() ;
        }
        finally
        {
            completionInitiator.removeCallback(_id) ;
        }

        if (callback.hasTriggered())
        {
            if (callback.receivedCommitted())
            {
                return ;
            }
            else if (callback.receivedAborted())
            {
                throw new TransactionRolledBackException() ;
            }
            final SoapFault soapFault = callback.getSoapFault() ;
            if ((soapFault != null) && ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME.equals(soapFault.getSubcode()))
            {
                throw new UnknownTransactionException();
            }
        }

        throw new SystemException() ;
	}

	public void rollback () throws UnknownTransactionException, SystemException
	{
        final MAP map = AddressingHelper.createNotificationContext(MessageId.getMessageId()) ;

        final CompletionStub.RequestCallback callback = new CompletionStub.RequestCallback() ;
        final CompletionInitiatorProcessor completionInitiator = CompletionInitiatorProcessor.getProcessor() ;
        completionInitiator.registerCallback(_id, callback) ;
        try
        {
            CompletionCoordinatorClient.getClient().sendRollback(_completionCoordinator, map, new InstanceIdentifier(_id)) ;
            callback.waitUntilTriggered() ;
        }
        catch (final Throwable th)
        {
            th.printStackTrace() ;
            throw new SystemException() ;
        }
        finally
        {
            completionInitiator.removeCallback(_id) ;
        }

        if (callback.hasTriggered())
        {
            if (callback.receivedAborted())
            {
                return ;
            }
            final SoapFault soapFault = callback.getSoapFault() ;
            if ((soapFault != null) && ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME.equals(soapFault.getSubcode()))
            {
                throw new UnknownTransactionException();
            }
        }

        throw new SystemException() ;
	}

    private static class RequestCallback extends CompletionInitiatorCallback
    {
        /**
         * The addressing context.
         */
        private MAP map ;
        /**
         * The arjuna context.
         */
        private ArjunaContext arjunaContext ;
        /**
         * The SOAP fault.
         */
        private SoapFault soapFault ;
        /**
         * The aborted notification flag.
         */
        private boolean aborted ;
        /**
         * The committed notification flag.
         */
        private boolean committed ;

        /**
         * Get the addressing context.
         * @return The addressing context.
         */
        MAP getMAP()
        {
            return map ;
        }

        /**
         * Get the arjuna context.
         * @return The arjuna context.
         */
        ArjunaContext getArjunaContext()
        {
            return arjunaContext ;
        }

        /**
         * Get the SOAP fault.
         * @return The SOAP fault.
         */
        SoapFault getSoapFault()
        {
            return soapFault ;
        }

        /**
         * Did we receive a aborted notification?
         * @return True if aborted, false otherwise.
         */
        boolean receivedAborted()
        {
            return aborted ;
        }

        /**
         * Did we receive a committed notification?
         * @return True if committed, false otherwise.
         */
        boolean receivedCommitted()
        {
            return committed ;
        }

        /**
         * A aborted response.
         * @param aborted The aborted notification.
         * @param map The addressing context.
         * @param arjunaContext The arjuna context.
         */
        public void aborted(final Notification aborted, final MAP map,
            final ArjunaContext arjunaContext)
        {
            this.aborted = true ;
            this.map = map ;
            this.arjunaContext = arjunaContext ;
        }

        /**
         * An committed response.
         * @param committed The committed notification.
         * @param map The addressing context.
         * @param arjunaContext The arjuna context.
         */
        public void committed(final Notification committed, final MAP map,
            final ArjunaContext arjunaContext)
        {
            this.committed  = true ;
            this.map = map ;
            this.arjunaContext = arjunaContext ;
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
            this.soapFault = soapFault ;
            this.map = map ;
            this.arjunaContext = arjunaContext ;
        }
    }
}
