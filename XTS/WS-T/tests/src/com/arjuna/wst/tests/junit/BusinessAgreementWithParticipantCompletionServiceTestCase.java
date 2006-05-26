/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
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
 * $Id: BusinessAgreementWithParticipantCompletionServiceTestCase.java,v 1.1.2.2 2004/06/18 15:06:10 nmcl Exp $
 */

package com.arjuna.wst.tests.junit;

import junit.framework.TestCase;

import com.arjuna.webservices.SoapRegistry;
import com.arjuna.webservices.wsaddr.AttributedURIType;
import com.arjuna.webservices.wsaddr.EndpointReferenceType;
import com.arjuna.webservices.wsarj.InstanceIdentifier;
import com.arjuna.webservices.wsba.BusinessActivityConstants;
import com.arjuna.wst.BusinessAgreementWithParticipantCompletionParticipant;
import com.arjuna.wst.FaultedException;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.WrongStateException;
import com.arjuna.wst.stub.BusinessAgreementWithParticipantCompletionStub;
import com.arjuna.wst.tests.TestUtil;

public class BusinessAgreementWithParticipantCompletionServiceTestCase extends TestCase
{
    protected void setUp()
        throws Exception
    {
        final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
        final String participantCompletionParticipantServiceURI = soapRegistry.getServiceURI(BusinessActivityConstants.SERVICE_PARTICIPANT_COMPLETION_PARTICIPANT) ;

        EndpointReferenceType noExceptionBusinessAgreementWithParticipantCompletionCoordinator = new EndpointReferenceType(new AttributedURIType(participantCompletionParticipantServiceURI));
        InstanceIdentifier.setEndpointInstanceIdentifier(noExceptionBusinessAgreementWithParticipantCompletionCoordinator, TestUtil.NOEXCEPTION_PARTICIPANT_IDENTIFIER);

        EndpointReferenceType wrongStateExceptionBusinessAgreementWithParticipantCompletionCoordinator = new EndpointReferenceType(new AttributedURIType(participantCompletionParticipantServiceURI));
        InstanceIdentifier.setEndpointInstanceIdentifier(wrongStateExceptionBusinessAgreementWithParticipantCompletionCoordinator, TestUtil.WRONGSTATEEXCEPTION_PARTICIPANT_IDENTIFIER);

        EndpointReferenceType systemExceptionBusinessAgreementWithParticipantCompletionCoordinator = new EndpointReferenceType(new AttributedURIType(participantCompletionParticipantServiceURI));
        InstanceIdentifier.setEndpointInstanceIdentifier(systemExceptionBusinessAgreementWithParticipantCompletionCoordinator, TestUtil.SYSTEMEXCEPTION_PARTICIPANT_IDENTIFIER);

        EndpointReferenceType nonexistentBusinessAgreementWithParticipantCompletionCoordinator = new EndpointReferenceType(new AttributedURIType(participantCompletionParticipantServiceURI));
        InstanceIdentifier.setEndpointInstanceIdentifier(nonexistentBusinessAgreementWithParticipantCompletionCoordinator, TestUtil.NONEXISTENT_PARTICIPANT_IDENTIFIER);

        EndpointReferenceType faultedExceptionBusinessAgreementWithParticipantCompletionCoordinator = new EndpointReferenceType(new AttributedURIType(participantCompletionParticipantServiceURI));
        InstanceIdentifier.setEndpointInstanceIdentifier(faultedExceptionBusinessAgreementWithParticipantCompletionCoordinator, TestUtil.FAULTEDEXCEPTION_PARTICIPANT_IDENTIFIER);

        _noExceptionBusinessAgreementWithParticipantCompletionStub                    = new BusinessAgreementWithParticipantCompletionStub("businessAgreementWithParticipantCompletionCoordinator", noExceptionBusinessAgreementWithParticipantCompletionCoordinator);
        _wrongStateExceptionBusinessAgreementWithParticipantCompletionStub            = new BusinessAgreementWithParticipantCompletionStub("businessAgreementWithParticipantCompletionCoordinator", wrongStateExceptionBusinessAgreementWithParticipantCompletionCoordinator);
        _systemExceptionBusinessAgreementWithParticipantCompletionStub                = new BusinessAgreementWithParticipantCompletionStub("businessAgreementWithParticipantCompletionCoordinator", systemExceptionBusinessAgreementWithParticipantCompletionCoordinator);
        _faultedExceptionBusinessAgreementWithParticipantCompletionStub                = new BusinessAgreementWithParticipantCompletionStub("businessAgreementWithParticipantCompletionCoordinator", faultedExceptionBusinessAgreementWithParticipantCompletionCoordinator);
    }

    public void testCloseWithNoException()
        throws Exception
    {
        _noExceptionBusinessAgreementWithParticipantCompletionStub.close();
    }

    public void testCancelWithNoException()
        throws Exception
    {
        _noExceptionBusinessAgreementWithParticipantCompletionStub.cancel();
    }

    public void testCompensateWithNoException()
        throws Exception
    {
        _noExceptionBusinessAgreementWithParticipantCompletionStub.compensate();
    }

    public void testCloseWithWrongStateException()
        throws Exception
    {
        try
        {
            _wrongStateExceptionBusinessAgreementWithParticipantCompletionStub.close();
            fail("Expected exception \"WrongStateException\"");
        }
        catch (WrongStateException wrongStateException)
        {
        }
    }

    public void testCancelWithWrongStateException()
        throws Exception
    {
        try
        {
            _wrongStateExceptionBusinessAgreementWithParticipantCompletionStub.cancel();
            fail("Expected exception \"WrongStateException\"");
        }
        catch (WrongStateException wrongStateException)
        {
        }
    }

    public void testCompensateWithWrongStateException()
        throws Exception
    {
        try
        {
            _wrongStateExceptionBusinessAgreementWithParticipantCompletionStub.compensate();
            fail("Expected exception \"WrongStateException\"");
        }
        catch (WrongStateException wrongStateException)
        {
        }
    }

    public void testCloseWithSystemException()
        throws Exception
    {
        try
        {
            _systemExceptionBusinessAgreementWithParticipantCompletionStub.close();
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
            _systemExceptionBusinessAgreementWithParticipantCompletionStub.cancel();
            fail("Expected exception \"SystemException\"");
        }
        catch (SystemException systemException)
        {
        }
    }

    public void testCompensateWithSystemException()
        throws Exception
    {
        try
        {
            _systemExceptionBusinessAgreementWithParticipantCompletionStub.compensate();
            fail("Expected exception \"SystemException\"");
        }
        catch (SystemException systemException)
        {
        }
    }

    public void testCompensateWithFaultedException ()
        throws Exception
    {
        try
        {
            _faultedExceptionBusinessAgreementWithParticipantCompletionStub.compensate();
            fail("Expected exception \"FaultedException\"");
        }
        catch (FaultedException faultedException)
        {
        }
    }

    protected void tearDown()
        throws Exception
    {
        _noExceptionBusinessAgreementWithParticipantCompletionStub                    = null;
        _wrongStateExceptionBusinessAgreementWithParticipantCompletionStub            = null;
        _systemExceptionBusinessAgreementWithParticipantCompletionStub                = null;
    }

    private BusinessAgreementWithParticipantCompletionParticipant _noExceptionBusinessAgreementWithParticipantCompletionStub                    = null;
    private BusinessAgreementWithParticipantCompletionParticipant _wrongStateExceptionBusinessAgreementWithParticipantCompletionStub            = null;
    private BusinessAgreementWithParticipantCompletionParticipant _systemExceptionBusinessAgreementWithParticipantCompletionStub                = null;
    private BusinessAgreementWithParticipantCompletionParticipant _faultedExceptionBusinessAgreementWithParticipantCompletionStub               = null;

}
