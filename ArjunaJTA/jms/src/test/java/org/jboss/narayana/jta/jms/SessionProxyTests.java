/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.narayana.jta.jms;

import javax.jms.Session;
import javax.jms.XASession;
import javax.transaction.Synchronization;
import javax.transaction.xa.XAResource;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class SessionProxyTests {

    @Mock
    private XASession xaSessionMock;

    @Mock
    private XAResource xaResourceMock;

    @Mock
    private TransactionHelper transactionHelperMock;

    private Session session;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        session = new SessionProxy(xaSessionMock, transactionHelperMock);
    }

    @Test
    public void shouldCloseSession() throws Exception {
        when(transactionHelperMock.isTransactionAvailable()).thenReturn(true);
        when(xaSessionMock.getXAResource()).thenReturn(xaResourceMock);

        List<Synchronization> synchronizations = new ArrayList<>(1);
        doAnswer(i -> synchronizations.add(i.getArgument(0))).when(transactionHelperMock)
                .registerSynchronization(any(Synchronization.class));

        session.close();

        // Will check if the correct session was registered for closing
        synchronizations.get(0).afterCompletion(0);

        verify(transactionHelperMock, times(1)).isTransactionAvailable();
        verify(transactionHelperMock, times(1)).deregisterXAResource(xaResourceMock);
        verify(transactionHelperMock, times(1)).registerSynchronization(any(SessionClosingSynchronization.class));
        verify(xaSessionMock, times(1)).close();
    }

}
