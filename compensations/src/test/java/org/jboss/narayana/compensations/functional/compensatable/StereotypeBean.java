/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.narayana.compensations.functional.compensatable;

import org.jboss.narayana.compensations.internal.BAControllerFactory;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@TestStereotype
public class StereotypeBean {

    public void doSomething() throws TestException {

        try {
            final Object txContext = BAControllerFactory.getInstance().getCurrentTransaction();

            if (txContext == null) {
                throw new RuntimeException("No transaction is active");
            }
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error looking up current transaction", e);
        }

        throw new TestException();
    }

}