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

import org.jboss.narayana.compensations.impl.BAControllerFactory;
import org.junit.Assert;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class Utills {

    public static void assertTransactionActive(final boolean expectActive) throws Exception {
        final Object txContext = BAControllerFactory.getInstance().getCurrentTransaction();

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

    public static void assertSameTransaction(final Object txContext) throws Exception {
        Object currentTx = BAControllerFactory.getInstance().getCurrentTransaction();
        if (txContext == null && currentTx == null)  {
            return;
        }

        if (txContext == null || currentTx == null) {
            Assert.fail();
        }
        Assert.assertTrue("Expected transaction to be the same, but it wasn't. '" + txContext + "' != '" + currentTx + "'" ,
                txContext.equals(currentTx));
    }

    public static void assertDifferentTransaction(final Object txContext) throws Exception {
        Object currentTx = BAControllerFactory.getInstance().getCurrentTransaction();
        if (txContext == null || currentTx == null) {
            return;
        }
        Assert.assertTrue("Expected transaction to be different, but it wasn't. '" + txContext + "' == '" + currentTx + "'",
                !txContext.equals(currentTx));
    }

}
