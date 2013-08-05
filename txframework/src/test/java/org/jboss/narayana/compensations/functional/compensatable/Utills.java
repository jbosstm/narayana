/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.narayana.compensations.functional.compensatable;

import com.arjuna.mw.wst.TxContext;
import com.arjuna.mw.wst11.BusinessActivityManagerFactory;
import com.arjuna.wst.SystemException;
import org.junit.Assert;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class Utills {

    public static void assertTransactionActive(final boolean expectActive) throws Exception {
        final TxContext txContext = BusinessActivityManagerFactory.businessActivityManager().currentTransaction();

        if (expectActive) {
            if (txContext == null) {
                Assert.fail("Transactions was expected to be active, but was not");
            }
        } else {
            if (txContext != null) {
                Assert.fail("Transactions was expected to be inactive, but was active");
            }
        }
    }

    public static void assertSameTransaction(final TxContext txContext) throws SystemException {
        Assert.assertTrue("Expected transaction to be the same, but it wasn't",
                txContext == BusinessActivityManagerFactory.businessActivityManager().currentTransaction());
    }

    public static void assertDifferentTransaction(final TxContext txContext) throws SystemException {
        Assert.assertTrue("Expected transaction to be different, but it wasn't",
                txContext != BusinessActivityManagerFactory.businessActivityManager().currentTransaction());
    }

}
