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
 * CompletionServiceTestCase.java
 */

package com.arjuna.wst.tests.junit;

import junit.framework.TestCase;

import com.arjuna.webservices.SoapRegistry;
import com.arjuna.webservices.wsaddr.AttributedURIType;
import com.arjuna.webservices.wsaddr.EndpointReferenceType;
import com.arjuna.webservices.wsarj.InstanceIdentifier;
import com.arjuna.webservices.wsat.AtomicTransactionConstants;
import com.arjuna.wst.CompletionCoordinatorParticipant;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.TransactionRolledBackException;
import com.arjuna.wst.UnknownTransactionException;
import com.arjuna.wst.stub.CompletionStub;
import com.arjuna.wst.tests.TestUtil;

public class CompletionServiceTestCase extends TestCase
{
    protected void setUp()
        throws Exception
    {
        final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
        final String completionCoordinatorServiceURI = soapRegistry.getServiceURI(AtomicTransactionConstants.SERVICE_COMPLETION_COORDINATOR) ;

        EndpointReferenceType noExceptionCompletionCoordinator = new EndpointReferenceType(new AttributedURIType(completionCoordinatorServiceURI));
        InstanceIdentifier.setEndpointInstanceIdentifier(noExceptionCompletionCoordinator, TestUtil.NOEXCEPTION_TRANSACTION_IDENTIFIER);

        EndpointReferenceType transactionRolledBackExceptionCompletionCoordinator = new EndpointReferenceType(new AttributedURIType(completionCoordinatorServiceURI));
        InstanceIdentifier.setEndpointInstanceIdentifier(transactionRolledBackExceptionCompletionCoordinator, TestUtil.TRANSACTIONROLLEDBACKEXCEPTION_TRANSACTION_IDENTIFIER);

        EndpointReferenceType unknownTransactionExceptionCompletionCoordinator = new EndpointReferenceType(new AttributedURIType(completionCoordinatorServiceURI));
        InstanceIdentifier.setEndpointInstanceIdentifier(unknownTransactionExceptionCompletionCoordinator, TestUtil.UNKNOWNTRANSACTIONEXCEPTION_TRANSACTION_IDENTIFIER);

        EndpointReferenceType systemExceptionCompletionCoordinator = new EndpointReferenceType(new AttributedURIType(completionCoordinatorServiceURI));
        InstanceIdentifier.setEndpointInstanceIdentifier(systemExceptionCompletionCoordinator, TestUtil.SYSTEMEXCEPTION_TRANSACTION_IDENTIFIER);

        EndpointReferenceType nonexistentCompletionCoordinator = new EndpointReferenceType(new AttributedURIType(completionCoordinatorServiceURI));
        InstanceIdentifier.setEndpointInstanceIdentifier(nonexistentCompletionCoordinator, TestUtil.NONEXISTENT_TRANSACTION_IDENTIFIER);

        _noExceptionCompletionStub                    = new CompletionStub("completionParticipant", noExceptionCompletionCoordinator);
        _transactionRolledBackExceptionCompletionStub = new CompletionStub("completionParticipant", transactionRolledBackExceptionCompletionCoordinator);
        _unknownExceptionExceptionCompletionStub      = new CompletionStub("completionParticipant", unknownTransactionExceptionCompletionCoordinator);
        _systemExceptionCompletionStub                = new CompletionStub("completionParticipant", systemExceptionCompletionCoordinator);
        _nonexistentCompletionStub                    = new CompletionStub("completionParticipant", nonexistentCompletionCoordinator);
    }

    public void testCommitWithNoException()
        throws Exception
    {
        _noExceptionCompletionStub.commit();
    }

    public void testRollbackWithNoException()
        throws Exception
    {
        _noExceptionCompletionStub.rollback();
    }

    public void testCommitWithTransactionRolledBackException()
        throws Exception
    {
        try
        {
            _transactionRolledBackExceptionCompletionStub.commit();
            fail("Expected exception \"TransactionRolledBackException\"");
        }
        catch (TransactionRolledBackException transactionRolledBackException)
        {
        }
    }

    public void testCommitWithUnknownTransactionException()
        throws Exception
    {
        try
        {
            _unknownExceptionExceptionCompletionStub.commit();
            fail("Expected exception \"UnknownTransactionException\"");
        }
        catch (UnknownTransactionException unknownTransactionException)
        {
        }
    }

    public void testRollbackWithUnknownTransactionException()
        throws Exception
    {
        try
        {
            _unknownExceptionExceptionCompletionStub.rollback();
            fail("Expected exception \"UnknownTransactionException\"");
        }
        catch (UnknownTransactionException unknownTransactionException)
        {
        }
    }

    public void testCommitWithSystemException()
        throws Exception
    {
        try
        {
            _systemExceptionCompletionStub.commit();
            fail("Expected exception \"SystemException\"");
        }
        catch (SystemException systemException)
        {
        }
    }

    public void testRollbackWithSystemException()
        throws Exception
    {
        try
        {
            _systemExceptionCompletionStub.rollback();
            fail("Expected exception \"SystemException\"");
        }
        catch (SystemException systemException)
        {
        }
    }

    public void testCommitWithNonExistent()
        throws Exception
    {
        try
        {
            _nonexistentCompletionStub.commit();
            fail("Expected exception \"UnknownTransactionException\"");
        }
        catch (UnknownTransactionException unknownTransactionException)
        {
        }
    }

    public void testRollbackWithNonExistent()
        throws Exception
    {
        try
        {
            _nonexistentCompletionStub.rollback();
            fail("Expected exception \"UnknownTransactionException\"");
        }
        catch (UnknownTransactionException unknownTransactionException)
        {
        }
    }

    protected void tearDown()
        throws Exception
    {
        _noExceptionCompletionStub                    = null;
        _transactionRolledBackExceptionCompletionStub = null;
        _unknownExceptionExceptionCompletionStub      = null;
        _systemExceptionCompletionStub                = null;
        _nonexistentCompletionStub                    = null;
    }

    private CompletionCoordinatorParticipant _noExceptionCompletionStub                    = null;
    private CompletionCoordinatorParticipant _transactionRolledBackExceptionCompletionStub = null;
    private CompletionCoordinatorParticipant _unknownExceptionExceptionCompletionStub      = null;
    private CompletionCoordinatorParticipant _systemExceptionCompletionStub                = null;
    private CompletionCoordinatorParticipant _nonexistentCompletionStub                    = null;
}
