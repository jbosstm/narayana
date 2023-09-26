/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.narayana.jta.jms;

import jakarta.jms.JMSException;
import jakarta.jms.Session;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class SessionClosingSynchronizationTests {

    @Mock
    private Session sessionMock;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldCloseConnection() throws JMSException {
        Synchronization synchronization = new SessionClosingSynchronization(sessionMock);

        synchronization.afterCompletion(Status.STATUS_COMMITTED);

        verify(sessionMock, times(1)).close();
    }

}