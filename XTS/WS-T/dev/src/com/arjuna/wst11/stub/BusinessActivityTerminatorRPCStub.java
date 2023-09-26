/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.wst11.stub;

import com.arjuna.schemas.ws._2005._10.wsarjtx.NotificationType;
import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.wsarjtx.ArjunaTXConstants;
import com.arjuna.webservices11.SoapFault11;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wsarjtx.client.TerminationCoordinatorClient;
import com.arjuna.webservices11.wsarjtx.client.TerminationCoordinatorRPCClient;
import com.arjuna.webservices11.wsarjtx.processors.TerminationParticipantCallback;
import com.arjuna.webservices11.wsarjtx.processors.TerminationParticipantProcessor;
import com.arjuna.wsc11.messaging.MessageId;
import com.arjuna.wst.FaultedException;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.TransactionRolledBackException;
import com.arjuna.wst.UnknownTransactionException;
import com.arjuna.wst11.BusinessActivityTerminator;
import org.jboss.ws.api.addressing.MAP;

import javax.xml.namespace.QName;
import jakarta.xml.ws.wsaddressing.W3CEndpointReference;

public class BusinessActivityTerminatorRPCStub implements BusinessActivityTerminator
{
    private W3CEndpointReference _terminationCoordinator = null;
    private final String _id ;

    public BusinessActivityTerminatorRPCStub(final String id, final W3CEndpointReference terminationCoordinator)
        throws Exception
    {
        _terminationCoordinator = terminationCoordinator;
        _id = id ;
    }

    public void close ()
        throws TransactionRolledBackException, UnknownTransactionException, SystemException
    {
        final MAP map = AddressingHelper.createNotificationContext(MessageId.getMessageId()) ;

        try
        {
            TerminationCoordinatorRPCClient.getClient().sendClose(_terminationCoordinator, map, new InstanceIdentifier(_id)) ;
        }
        catch (SoapFault11 soapFault)
        {
            if ((ArjunaTXConstants.TRANSACTIONROLLEDBACK_ERROR_CODE_QNAME.equals(soapFault.getSubcode()))) {
                throw new TransactionRolledBackException(soapFault.getMessage());
            }
            else if (ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME.equals(soapFault.getSubcode()))
            {
                throw new UnknownTransactionException(soapFault.getMessage()) ;
            }

            throw new SystemException(soapFault.getMessage()) ;
        }
        catch (Exception e)
        {
            throw new SystemException();
        }
    }

    public void cancel ()
        throws FaultedException, UnknownTransactionException, SystemException
    {
        final MAP map = AddressingHelper.createNotificationContext(MessageId.getMessageId()) ;

        try
        {
            TerminationCoordinatorRPCClient.getClient().sendCancel(_terminationCoordinator, map, new InstanceIdentifier(_id)) ;
        }
        catch (SoapFault11 soapFault)
        {
            if (ArjunaTXConstants.FAULTED_ERROR_CODE_QNAME.equals(soapFault.getSubcode()))
            {
                throw new FaultedException(soapFault.getMessage()) ;
            }
            else if (ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME.equals(soapFault.getSubcode()))
            {
                throw new UnknownTransactionException(soapFault.getMessage()) ;
            }

            throw new SystemException(soapFault.getMessage()) ;
        }
        catch (Exception e)
        {
            throw new SystemException(e.getMessage());
        }
        catch (final Throwable th)
        {
            th.printStackTrace() ;
            throw new SystemException(th.getMessage()) ;
        }
    }

    public void complete ()
        throws FaultedException, UnknownTransactionException, SystemException
    {
        final MAP map = AddressingHelper.createNotificationContext(MessageId.getMessageId()) ;

        try
        {
            TerminationCoordinatorRPCClient.getClient().sendComplete(_terminationCoordinator, map, new InstanceIdentifier(_id)) ;
        }
        catch (SoapFault11 soapFault)
        {
            if (ArjunaTXConstants.FAULTED_ERROR_CODE_QNAME.equals(soapFault.getSubcode()))
            {
                throw new FaultedException(soapFault.getMessage()) ;
            }
            else if (ArjunaTXConstants.UNKNOWNTRANSACTION_ERROR_CODE_QNAME.equals(soapFault.getSubcode()))
            {
                throw new UnknownTransactionException(soapFault.getMessage()) ;
            }

            throw new SystemException(soapFault.getMessage()) ;
        }
        catch (Exception e)
        {
            throw new SystemException(e.getMessage());
        }
        catch (final Throwable th)
        {
            th.printStackTrace() ;
            throw new SystemException(th.getMessage()) ;
        }
    }

    public W3CEndpointReference getEndpoint()
    {
        return _terminationCoordinator;
    }
    /*
     * this never gets called
     */
    public void unknown ()
        throws SystemException
    {
        error() ;
    }

    /*
     * this never gets called
     */
    public void error ()
        throws SystemException
    {
/*
 * Since it is never used this has been decommissioned due to problems with using the soap fault service to
  * send a fault via a W3C endpoint. the latter is broken now that CXF helpfully puts metadata into the endpoint.
        final MAP map = AddressingHelper.createNotificationContext(MessageId.getMessageId()) ;
        final SoapFault soapFault = new SoapFault11(SoapFaultType.FAULT_SENDER, ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME,
                WSTLogger.i18NLogger.get_wst11_stub_BusinessActivityTerminatorStub_1()) ;
        try
        {
            TerminationCoordinatorClient.getClient().sendSoapFault(_terminationCoordinator, map, soapFault, new InstanceIdentifier(_id)) ;
        }
        catch (final Throwable th)
        {
            throw new SystemException() ;
        }
*/
    }

}