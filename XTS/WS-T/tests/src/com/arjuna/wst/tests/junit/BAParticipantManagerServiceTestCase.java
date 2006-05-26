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
 * $Id: BAParticipantManagerServiceTestCase.java,v 1.7.6.1 2005/11/22 10:37:49 kconner Exp $
 */

package com.arjuna.wst.tests.junit;

import junit.framework.TestCase;

import com.arjuna.webservices.SoapRegistry;
import com.arjuna.webservices.wsaddr.AttributedURIType;
import com.arjuna.webservices.wsaddr.EndpointReferenceType;
import com.arjuna.webservices.wsarj.InstanceIdentifier;
import com.arjuna.webservices.wsarjtx.ArjunaTXConstants;
import com.arjuna.wst.BAParticipantManager;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.UnknownTransactionException;
import com.arjuna.wst.WrongStateException;
import com.arjuna.wst.stub.BAParticipantManagerStub;
import com.arjuna.wst.tests.TestUtil;

public class BAParticipantManagerServiceTestCase extends TestCase
{
    protected void setUp()
        throws Exception
    {
        final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
        final String participantManagerParticipantServiceURI = soapRegistry.getServiceURI(ArjunaTXConstants.SERVICE_PARTICIPANT_MANAGER_PARTICIPANT) ;

        EndpointReferenceType noExceptionBAParticipantManagerCoordinator = new EndpointReferenceType(new AttributedURIType(participantManagerParticipantServiceURI));
        InstanceIdentifier.setEndpointInstanceIdentifier(noExceptionBAParticipantManagerCoordinator, TestUtil.NOEXCEPTION_PARTICIPANT_IDENTIFIER);

        EndpointReferenceType wrongStateExceptionBAParticipantManagerCoordinator = new EndpointReferenceType(new AttributedURIType(participantManagerParticipantServiceURI));
        InstanceIdentifier.setEndpointInstanceIdentifier(wrongStateExceptionBAParticipantManagerCoordinator, TestUtil.WRONGSTATEEXCEPTION_PARTICIPANT_IDENTIFIER);

        EndpointReferenceType systemExceptionBAParticipantManagerCoordinator = new EndpointReferenceType(new AttributedURIType(participantManagerParticipantServiceURI));
        InstanceIdentifier.setEndpointInstanceIdentifier(systemExceptionBAParticipantManagerCoordinator, TestUtil.SYSTEMEXCEPTION_PARTICIPANT_IDENTIFIER);

        EndpointReferenceType nonexistentBAParticipantManagerCoordinator = new EndpointReferenceType(new AttributedURIType(participantManagerParticipantServiceURI));
        InstanceIdentifier.setEndpointInstanceIdentifier(nonexistentBAParticipantManagerCoordinator, TestUtil.NONEXISTENT_PARTICIPANT_IDENTIFIER);

        _noExceptionBAParticipantManagerStub                    = new BAParticipantManagerStub("participantManagerCoordinator", noExceptionBAParticipantManagerCoordinator);
        _wrongStateExceptionBAParticipantManagerStub            = new BAParticipantManagerStub("participantManagerCoordinator", wrongStateExceptionBAParticipantManagerCoordinator);
        _systemExceptionBAParticipantManagerStub                = new BAParticipantManagerStub("participantManagerCoordinator", systemExceptionBAParticipantManagerCoordinator);
        _nonexistentBAParticipantManagerStub                    = new BAParticipantManagerStub("participantManagerCoordinator", nonexistentBAParticipantManagerCoordinator);
    }

    public void testCompletedWithNoException ()
        throws Exception
    {
        _noExceptionBAParticipantManagerStub.completed();
    }

    public void testExitWithNoException()
        throws Exception
    {
        _noExceptionBAParticipantManagerStub.exit();
    }

    public void testUnknownWithNoException()
        throws Exception
    {
        _noExceptionBAParticipantManagerStub.unknown();
    }

    public void testFaultWithNoException()
        throws Exception
    {
        _noExceptionBAParticipantManagerStub.fault();
    }

    public void testErrorWithNoException()
        throws Exception
    {
        _noExceptionBAParticipantManagerStub.error();
    }

    public void testCompletedWithWrongStateException()
        throws Exception
    {
        try
        {
            _wrongStateExceptionBAParticipantManagerStub.completed();
            fail("Expected exception \"WrongStateException\"");
        }
        catch (WrongStateException wrongStateException)
        {
        }
    }

    public void testExitWithWrongStateException()
        throws Exception
    {
        try
        {
            _wrongStateExceptionBAParticipantManagerStub.exit();
            fail("Expected exception \"WrongStateException\"");
        }
        catch (WrongStateException wrongStateException)
        {
        }
    }

    public void testCompletedWithSystemException()
        throws Exception
    {
        try
        {
            _systemExceptionBAParticipantManagerStub.completed();
            fail("Expected exception \"SystemException\"");
        }
        catch (SystemException systemException)
        {
        }
    }

    public void testExitWithSystemException()
        throws Exception
    {
        try
        {
            _systemExceptionBAParticipantManagerStub.exit();
            fail("Expected exception \"SystemException\"");
        }
        catch (SystemException systemException)
        {
        }
    }

    public void testFaultWithSystemException ()
        throws Exception
    {
        try
        {
            _systemExceptionBAParticipantManagerStub.fault();
            fail("Expected exception \"SystemException\"");
        }
        catch (SystemException systemException)
        {
        }
    }

    public void testUnknownWithSystemException ()
        throws Exception
    {
	_systemExceptionBAParticipantManagerStub.unknown();
    } 

    public void testErrorWithSystemException ()
        throws Exception
    {
	_systemExceptionBAParticipantManagerStub.error();
    }

    public void testCompletedWithNonExistent()
        throws Exception
    {
        try
        {
            _nonexistentBAParticipantManagerStub.completed();
            fail("Expected exception \"UnknownTransactionException\"");
        }
        catch (UnknownTransactionException unknownTransactionException)
        {
        }
    }

    public void testExitWithNonExistent()
        throws Exception
    {
        try
        {
            _nonexistentBAParticipantManagerStub.exit();
            fail("Expected exception \"UnknownTransactionException\"");
        }
        catch (UnknownTransactionException unknownTransactionException)
        {
        }
    }

    public void testFaultWithNonExistent()
        throws Exception
    {
        try
        {
            _nonexistentBAParticipantManagerStub.fault();
            fail("Expected exception \"SystemException\"");
        }
        catch (SystemException systemException)
        {
        }
    }

    public void testUnknownWithNonExistent()
        throws Exception
    {
        _nonexistentBAParticipantManagerStub.unknown();
    }

    public void testErrorWithNonExistent()
        throws Exception
    {
        _nonexistentBAParticipantManagerStub.error();
    }

    protected void tearDown()
        throws Exception
    {
        _noExceptionBAParticipantManagerStub                    = null;
        _wrongStateExceptionBAParticipantManagerStub            = null;
        _systemExceptionBAParticipantManagerStub                = null;
        _nonexistentBAParticipantManagerStub                    = null;
    }

    private BAParticipantManager _noExceptionBAParticipantManagerStub = null;
    private BAParticipantManager _wrongStateExceptionBAParticipantManagerStub = null;
    private BAParticipantManager _systemExceptionBAParticipantManagerStub = null;
    private BAParticipantManager _nonexistentBAParticipantManagerStub = null;
}
