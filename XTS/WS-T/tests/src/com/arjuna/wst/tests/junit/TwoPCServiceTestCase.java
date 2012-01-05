/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (c) 2003, 2004,
 *
 * Arjuna Technologies Limited.
 *
 * $Id: TwoPCServiceTestCase.java,v 1.5.2.1 2005/11/22 10:37:45 kconner Exp $
 */

package com.arjuna.wst.tests.junit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.arjuna.webservices.SoapRegistry;
import com.arjuna.webservices.wsaddr.AttributedURIType;
import com.arjuna.webservices.wsaddr.EndpointReferenceType;
import com.arjuna.webservices.wsarj.InstanceIdentifier;
import com.arjuna.webservices.wsat.AtomicTransactionConstants;
import com.arjuna.wst.Participant;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.Vote;
import com.arjuna.wst.WrongStateException;
import com.arjuna.wst.stub.ParticipantStub;
import com.arjuna.wst.tests.TestUtil;

public class TwoPCServiceTestCase 
{
    @Before
    public void setUp()
        throws Exception
    {
        final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
        final String participantServiceURI = soapRegistry.getServiceURI(AtomicTransactionConstants.SERVICE_PARTICIPANT) ;

        EndpointReferenceType preparedVoteCoordinator = new EndpointReferenceType(new AttributedURIType(participantServiceURI));
        InstanceIdentifier.setEndpointInstanceIdentifier(preparedVoteCoordinator, TestUtil.PREPAREDVOTE_PARTICIPANT_IDENTIFIER);

        EndpointReferenceType abortedVoteCoordinator = new EndpointReferenceType(new AttributedURIType(participantServiceURI));
        InstanceIdentifier.setEndpointInstanceIdentifier(abortedVoteCoordinator, TestUtil.ABORTEDVOTE_PARTICIPANT_IDENTIFIER);

        EndpointReferenceType readOnlyVoteCoordinator = new EndpointReferenceType(new AttributedURIType(participantServiceURI));
        InstanceIdentifier.setEndpointInstanceIdentifier(readOnlyVoteCoordinator, TestUtil.READONLYVOTE_PARTICIPANT_IDENTIFIER);

        EndpointReferenceType noExceptionCoordinator = new EndpointReferenceType(new AttributedURIType(participantServiceURI));
        InstanceIdentifier.setEndpointInstanceIdentifier(noExceptionCoordinator, TestUtil.NOEXCEPTION_PARTICIPANT_IDENTIFIER);

        EndpointReferenceType wrongStateExceptionCoordinator = new EndpointReferenceType(new AttributedURIType(participantServiceURI));
        InstanceIdentifier.setEndpointInstanceIdentifier(wrongStateExceptionCoordinator, TestUtil.WRONGSTATEEXCEPTION_PARTICIPANT_IDENTIFIER);

        EndpointReferenceType systemExceptionCoordinator = new EndpointReferenceType(new AttributedURIType(participantServiceURI));
        InstanceIdentifier.setEndpointInstanceIdentifier(systemExceptionCoordinator, TestUtil.SYSTEMEXCEPTION_PARTICIPANT_IDENTIFIER);

        EndpointReferenceType nonexistentCoordinator = new EndpointReferenceType(new AttributedURIType(participantServiceURI));
        InstanceIdentifier.setEndpointInstanceIdentifier(nonexistentCoordinator, TestUtil.NONEXISTENT_PARTICIPANT_IDENTIFIER);

        _preparedVoteStub                   = new ParticipantStub("twoPCCoordinator", false, preparedVoteCoordinator);
        _abortedVoteStub                    = new ParticipantStub("twoPCCoordinator", false, abortedVoteCoordinator);
        _readOnlyVoteStub                   = new ParticipantStub("twoPCCoordinator", false, readOnlyVoteCoordinator);
        _noExceptionStub                    = new ParticipantStub("twoPCCoordinator", false, noExceptionCoordinator);
        _wrongStateExceptionStub            = new ParticipantStub("twoPCCoordinator", false, wrongStateExceptionCoordinator);
        _systemExceptionStub                = new ParticipantStub("twoPCCoordinator", false, systemExceptionCoordinator);
        _nonexistentStub                    = new ParticipantStub("twoPCCoordinator", false, nonexistentCoordinator);
    }

    @Test
    public void testPrepareWithPreparedVote()
        throws Exception
    {
        Vote vote = _preparedVoteStub.prepare();

        assertNotNull(vote);
        assertTrue("Expected vote \"Prepared\" got \"" + vote.getClass().getName() + "\"", vote instanceof com.arjuna.wst.Prepared);
    }

    @Test
    public void testPrepareWithAbortedVote()
        throws Exception
    {
        Vote vote = _abortedVoteStub.prepare();

        assertNotNull(vote);
        assertTrue("Expected vote \"Aborted\" got \"" + vote.getClass().getName() + "\"", vote instanceof com.arjuna.wst.Aborted);
    }

    @Test
    public void testPrepareWithReadOnlyVote()
        throws Exception
    {
        Vote vote = _readOnlyVoteStub.prepare();

        assertNotNull(vote);
        assertTrue("Expected vote \"ReadOnly\" got \"" + vote.getClass().getName() + "\"", vote instanceof com.arjuna.wst.ReadOnly);
    }

    @Test
    public void testCommitWithNoException()
        throws Exception
    {
        _noExceptionStub.commit();
    }

    @Test
    public void testRollbackWithNoException()
        throws Exception
    {
        _noExceptionStub.rollback();
    }

    @Test
    public void testUnknownWithNoException()
        throws Exception
    {
        _noExceptionStub.unknown();
    }

    @Test
    public void testErrorWithNoException()
        throws Exception
    {
        _noExceptionStub.error();
    }

    @Test
    public void testPrepareWithWrongStateException()
        throws Exception
    {
        try
        {
            _wrongStateExceptionStub.prepare();
            fail("Expected exception \"WrongStateException\"");
        }
        catch (WrongStateException wrongStateException)
        {
        }
    }

    @Test
    public void testCommitWithWrongStateException()
        throws Exception
    {
        try
        {
            _wrongStateExceptionStub.commit();
            fail("Expected exception \"WrongStateException\"");
        }
        catch (WrongStateException wrongStateException)
        {
        }
    }

    @Test
    public void testRollbackWithWrongStateException()
        throws Exception
    {
        try
        {
            _wrongStateExceptionStub.rollback();
            fail("Expected exception \"WrongStateException\"");
        }
        catch (WrongStateException wrongStateException)
        {
        }
    }

    @Test
    public void testPrepareWithSystemException()
        throws Exception
    {
        try
        {
            _systemExceptionStub.prepare();
            fail("Expected exception \"SystemException\"");
        }
        catch (SystemException systemException)
        {
        }
    }

    @Test
    public void testCommitWithSystemException()
        throws Exception
    {
        try
        {
            _systemExceptionStub.commit();
            fail("Expected exception \"SystemException\"");
        }
        catch (SystemException systemException)
        {
        }
    }

    @Test
    public void testRollbackWithSystemException()
        throws Exception
    {
        try
        {
            _systemExceptionStub.rollback();
            fail("Expected exception \"SystemException\"");
        }
        catch (SystemException systemException)
        {
        }
    }

    @Test
    public void testUnknownWithSystemException()
        throws Exception
    {
        _systemExceptionStub.unknown();
    }

    @Test
    public void testErrorWithSystemException()
        throws Exception
    {
        _systemExceptionStub.error();
    }

    @Test
    public void testPrepareWithNonExistent()
        throws Exception
    {
        try
        {
            _nonexistentStub.prepare();
            fail("Expected exception \"SystemException\"");
        }
        catch (SystemException systemException)
        {
        }
    }

    @Test
    public void testCommitWithNonExistent()
        throws Exception
    {
        try
        {
            _nonexistentStub.commit();
            fail("Expected exception \"SystemException\"");
        }
        catch (SystemException systemException)
        {
        }
    }

    @Test
    public void testRollbackWithNonExistent()
        throws Exception
    {
        try
        {
            _nonexistentStub.rollback();
            fail("Expected exception \"SystemException\"");
        }
        catch (SystemException systemException)
        {
        }
    }

    @Test
    public void testUnknownWithNonExistent()
        throws Exception
    {
        _nonexistentStub.unknown();
    }

    @Test
    public void testErrorWithNonExistent()
        throws Exception
    {
        _nonexistentStub.error();
    }

    @After
    public void tearDown()
        throws Exception
    {
        _preparedVoteStub                   = null;
        _abortedVoteStub                    = null;
        _readOnlyVoteStub                   = null;
        _noExceptionStub                    = null;
        _wrongStateExceptionStub            = null;
        _systemExceptionStub                = null;
        _nonexistentStub                    = null;
    }

    private Participant _preparedVoteStub                   = null;
    private Participant _abortedVoteStub                    = null;
    private Participant _readOnlyVoteStub                   = null;
    private Participant _noExceptionStub                    = null;
    private Participant _wrongStateExceptionStub            = null;
    private Participant _systemExceptionStub                = null;
    private Participant _nonexistentStub                    = null;
}
