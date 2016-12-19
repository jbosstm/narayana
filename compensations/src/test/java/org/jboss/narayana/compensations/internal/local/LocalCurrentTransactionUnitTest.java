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

package org.jboss.narayana.compensations.internal.local;

import com.arjuna.mw.wsas.activity.ActivityHandle;
import com.arjuna.mw.wsas.activity.ActivityHierarchy;
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
public class LocalCurrentTransactionUnitTest {

    private static final String TEST_VALUE = "Test value";

    private static final String OTHER_TEST_VALUE = "Other test value";

    @Mock
    private ActivityHierarchy activityHierarchy;

    @Mock
    private ActivityHierarchy otherActivityHierarchy;

    @Mock
    private ActivityHandle activityHandle;

    @Mock
    private ActivityHandle otherActivityHandle;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldCreateLocalCurrentTransaction() {
        when(activityHandle.tid()).thenReturn(TEST_VALUE);
        when(activityHierarchy.current()).thenReturn(activityHandle);

        CurrentTransaction currentTransaction = new LocalCurrentTransaction(activityHierarchy);

        assertEquals(ActivityHierarchy.class, currentTransaction.getDelegateClass());
        assertEquals(activityHierarchy, currentTransaction.getDelegate());
        assertEquals(TEST_VALUE, currentTransaction.getId());
    }

    @Test
    public void shouldCompareTwoEqualTransactions() {
        CurrentTransaction currentTransaction = new LocalCurrentTransaction(activityHierarchy);
        CurrentTransaction otherCurrentTransaction = new LocalCurrentTransaction(activityHierarchy);

        assertEquals(currentTransaction, otherCurrentTransaction);
    }

    @Test
    public void shouldCompareTwoDifferentTransactions() {
        when(activityHandle.tid()).thenReturn(TEST_VALUE);
        when(activityHierarchy.current()).thenReturn(activityHandle);
        when(otherActivityHandle.tid()).thenReturn(OTHER_TEST_VALUE);
        when(otherActivityHierarchy.current()).thenReturn(otherActivityHandle);

        CurrentTransaction currentTransaction = new LocalCurrentTransaction(activityHierarchy);
        CurrentTransaction otherCurrentTransaction = new LocalCurrentTransaction(otherActivityHierarchy);

        assertNotEquals(currentTransaction, otherCurrentTransaction);
    }

}
