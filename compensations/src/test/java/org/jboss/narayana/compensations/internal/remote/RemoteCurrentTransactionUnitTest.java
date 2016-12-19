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

package org.jboss.narayana.compensations.internal.remote;

import com.arjuna.mw.wst.TxContext;
import org.jboss.narayana.compensations.internal.CurrentTransaction;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class RemoteCurrentTransactionUnitTest {

    private static final String TEST_VALUE = "Test value";

    private static final String OTHER_TEST_VALUE = "Other test value";

    @Mock
    private TxContext txContext;

    @Mock
    private TxContext otherTxContext;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldCreateRemoteCurrentTransaction() {
        when(txContext.toString()).thenReturn(TEST_VALUE);

        CurrentTransaction currentTransaction = new RemoteCurrentTransaction(txContext);

        assertEquals(TxContext.class, currentTransaction.getDelegateClass());
        assertEquals(txContext, currentTransaction.getDelegate());
        assertEquals(TEST_VALUE, currentTransaction.getId());
    }

    @Test
    public void shouldCompareTwoEqualTransactions() {
        CurrentTransaction currentTransaction = new RemoteCurrentTransaction(txContext);
        CurrentTransaction otherCurrentTransaction = new RemoteCurrentTransaction(txContext);

        assertEquals(currentTransaction, otherCurrentTransaction);
    }

    @Test
    public void shouldCompareTwoDifferentTransactions() {
        when(txContext.toString()).thenReturn(TEST_VALUE);
        when(otherTxContext.toString()).thenReturn(OTHER_TEST_VALUE);

        CurrentTransaction currentTransaction = new RemoteCurrentTransaction(txContext);
        CurrentTransaction otherCurrentTransaction = new RemoteCurrentTransaction(otherTxContext);

        assertNotEquals(currentTransaction, otherCurrentTransaction);
    }

}
