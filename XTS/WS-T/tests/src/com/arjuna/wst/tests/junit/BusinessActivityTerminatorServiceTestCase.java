/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (c) 2003, Arjuna Technologies Limited.
 *
 * $Id: BusinessActivityTerminatorServiceTestCase.java,v 1.6.6.1 2005/11/22 10:37:46 kconner Exp $
 */

package com.arjuna.wst.tests.junit;

import junit.framework.TestCase;

import com.arjuna.webservices.SoapRegistry;
import com.arjuna.webservices.wsaddr.AttributedURIType;
import com.arjuna.webservices.wsaddr.EndpointReferenceType;
import com.arjuna.webservices.wsarj.InstanceIdentifier;
import com.arjuna.webservices.wsarjtx.ArjunaTXConstants;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.TransactionRolledBackException;
import com.arjuna.wst.UnknownTransactionException;
import com.arjuna.wst.stub.BusinessActivityTerminatorStub;
import com.arjuna.wst.tests.TestUtil;

public class BusinessActivityTerminatorServiceTestCase extends TestCase
{
    protected void setUp()
        throws Exception
    {
        final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
        final String businessActivityTerminatorParticipantServiceURI = soapRegistry.getServiceURI(ArjunaTXConstants.SERVICE_TERMINATOR_PARTICIPANT);

        EndpointReferenceType noExceptionBusinessActivityTerminatorCoordinator = new EndpointReferenceType(new AttributedURIType(businessActivityTerminatorParticipantServiceURI));
        InstanceIdentifier.setEndpointInstanceIdentifier(noExceptionBusinessActivityTerminatorCoordinator, TestUtil.NOEXCEPTION_PARTICIPANT_IDENTIFIER);

        EndpointReferenceType unknownTransactionExceptionBusinessActivityTerminatorCoordinator = new EndpointReferenceType(new AttributedURIType(businessActivityTerminatorParticipantServiceURI));
        InstanceIdentifier.setEndpointInstanceIdentifier(unknownTransactionExceptionBusinessActivityTerminatorCoordinator, TestUtil.UNKNOWNTRANSACTIONEXCEPTION_TRANSACTION_IDENTIFIER);

        EndpointReferenceType transactionRolledBackExceptionBusinessActivityTerminatorCoordinator = new EndpointReferenceType(new AttributedURIType(businessActivityTerminatorParticipantServiceURI));
        InstanceIdentifier.setEndpointInstanceIdentifier(transactionRolledBackExceptionBusinessActivityTerminatorCoordinator, TestUtil.TRANSACTIONROLLEDBACKEXCEPTION_TRANSACTION_IDENTIFIER);

        EndpointReferenceType systemExceptionBusinessActivityTerminatorCoordinator = new EndpointReferenceType(new AttributedURIType(businessActivityTerminatorParticipantServiceURI));
        InstanceIdentifier.setEndpointInstanceIdentifier(systemExceptionBusinessActivityTerminatorCoordinator, TestUtil.SYSTEMEXCEPTION_PARTICIPANT_IDENTIFIER);

        _noExceptionBusinessActivityTerminatorStub = new BusinessActivityTerminatorStub("businessActivityTerminatorCoordinator", noExceptionBusinessActivityTerminatorCoordinator);
        _transactionRolledBackExceptionBusinessActivityTerminatorStub = new BusinessActivityTerminatorStub("businessActivityTerminatorCoordinator", transactionRolledBackExceptionBusinessActivityTerminatorCoordinator);
        _systemExceptionBusinessActivityTerminatorStub = new BusinessActivityTerminatorStub("businessActivityTerminatorCoordinator", systemExceptionBusinessActivityTerminatorCoordinator);
        _unknownTransactionExceptionBusinessActivityTerminatorStub = new BusinessActivityTerminatorStub("businessActivityTerminatorCoordinator", unknownTransactionExceptionBusinessActivityTerminatorCoordinator);
    }

    public void testCloseWithNoException()
        throws Exception
    {
        _noExceptionBusinessActivityTerminatorStub.close();
    }

    public void testCancelWithNoException()
        throws Exception
    {
        _noExceptionBusinessActivityTerminatorStub.cancel();
    }

    public void testCompleteWithNoException()
        throws Exception
    {
        _noExceptionBusinessActivityTerminatorStub.complete();
    }

    public void testCloseWithUnknownTransactionException()
        throws Exception
    {
        try
        {
            _unknownTransactionExceptionBusinessActivityTerminatorStub.close();
            fail("Expected exception \"UnknownTransactionException\"");
        }
        catch (UnknownTransactionException unknownTransactionException)
        {
        }
    }

    public void testCancelWithUnknownTransactionException()
        throws Exception
    {
        try
        {
            _unknownTransactionExceptionBusinessActivityTerminatorStub.cancel();
            fail("Expected exception \"UnknownTransactionException\"");
        }
        catch (UnknownTransactionException unknownTransactionException)
        {
        }
    }

    public void testCompleteWithUnknownTransactionException()
        throws Exception
    {
        try
        {
            _unknownTransactionExceptionBusinessActivityTerminatorStub.complete();
            fail("Expected exception \"UnknownTransactionException\"");
        }
        catch (UnknownTransactionException unknownTransactionException)
        {
        }
    }

    public void testCloseWithTransactionRolledBackException()
        throws Exception
    {
        try
        {
            _transactionRolledBackExceptionBusinessActivityTerminatorStub.close();
            fail("Expected exception \"TransactionRolledBackException\"");
        }
        catch (TransactionRolledBackException transactionRolledBackException)
        {
        }
    }

    public void testCloseWithSystemException()
        throws Exception
    {
        try
        {
            _systemExceptionBusinessActivityTerminatorStub.close();
            fail("Expected exception \"SystemException\"");
        }
        catch (SystemException systemException)
        {
        }
    }

    public void testCancelWithSystemException()
        throws Exception
    {
        try
        {
            _systemExceptionBusinessActivityTerminatorStub.cancel();
            fail("Expected exception \"SystemException\"");
        }
        catch (SystemException systemException)
        {
        }
    }

    public void testCompleteWithSystemException()
        throws Exception
    {
	try
	{
	    _systemExceptionBusinessActivityTerminatorStub.complete();
            fail("Expected exception \"SystemException\"");
        }
        catch (SystemException systemException)
        {
        }
    }

    protected void tearDown()
        throws Exception
    {
        _noExceptionBusinessActivityTerminatorStub                    = null;
        _transactionRolledBackExceptionBusinessActivityTerminatorStub            = null;
        _systemExceptionBusinessActivityTerminatorStub                = null;
    }

    private BusinessActivityTerminatorStub _noExceptionBusinessActivityTerminatorStub                    = null;
    private BusinessActivityTerminatorStub _transactionRolledBackExceptionBusinessActivityTerminatorStub            = null;
    private BusinessActivityTerminatorStub _systemExceptionBusinessActivityTerminatorStub                = null;
    private BusinessActivityTerminatorStub _unknownTransactionExceptionBusinessActivityTerminatorStub                = null;

}
