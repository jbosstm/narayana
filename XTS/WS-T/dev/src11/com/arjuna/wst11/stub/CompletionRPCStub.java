package com.arjuna.wst11.stub;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.wsarjtx.ArjunaTXConstants;
import com.arjuna.webservices11.SoapFault11;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wsat.client.CompletionCoordinatorClient;
import com.arjuna.webservices11.wsat.client.CompletionCoordinatorRPCClient;
import com.arjuna.webservices11.wsat.processors.CompletionInitiatorCallback;
import com.arjuna.webservices11.wsat.processors.CompletionInitiatorProcessor;
import com.arjuna.wsc11.messaging.MessageId;
import com.arjuna.wst.CompletionCoordinatorParticipant;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.TransactionRolledBackException;
import com.arjuna.wst.UnknownTransactionException;
import org.jboss.ws.api.addressing.MAP;
import org.oasis_open.docs.ws_tx.wsat._2006._06.Notification;

import javax.xml.ws.wsaddressing.W3CEndpointReference;

public class CompletionRPCStub implements
        CompletionCoordinatorParticipant
{
    private W3CEndpointReference _completionCoordinator = null;
    private String _id;

	public CompletionRPCStub(final String id, final W3CEndpointReference completionCoordinator)
			throws Exception
	{
		_completionCoordinator = completionCoordinator;
		_id = id;
	}

	public void commit () throws TransactionRolledBackException,
            UnknownTransactionException, SystemException
    {
        final MAP map = AddressingHelper.createNotificationContext(MessageId.getMessageId()) ;
        boolean result;

        try
        {
            result = CompletionCoordinatorRPCClient.getClient().sendCommit(_completionCoordinator, map) ;
        }
        catch (final SoapFault soapFault)
        {
            if (ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME.equals(soapFault.getSubcode())) {
                throw new UnknownTransactionException(soapFault.getMessage());
            }
            throw new SystemException(soapFault.getMessage()) ;
        }
        catch (final Exception e)
        {
            throw new SystemException(e.getMessage()) ;
        }
        catch (final Throwable th)
        {
            th.printStackTrace() ;
            throw new SystemException(th.getMessage()) ;
        }

        if (!result) {
            throw new TransactionRolledBackException() ;
        }
	}

	public void rollback () throws UnknownTransactionException, SystemException
	{
        final MAP map = AddressingHelper.createNotificationContext(MessageId.getMessageId()) ;

        try
        {
            CompletionCoordinatorRPCClient.getClient().sendRollback(_completionCoordinator, map) ;
        }
        catch (final SoapFault soapFault)
        {
            if (ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME.equals(soapFault.getSubcode())) {
                throw new UnknownTransactionException(soapFault.getMessage());
            }
            throw new SystemException(soapFault.getMessage()) ;
        }
        catch (final Throwable th)
        {
            th.printStackTrace() ;
            throw new SystemException(th.getMessage()) ;
        }
	}
}
