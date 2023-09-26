/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.cdi.transactional;

@Boundary
public class BusinessLogic {
    public void doSomething() throws TestException {
        try {
            if (Utills.getCurrentTransaction() == null)
                throw new RuntimeException("No transaction is active");
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error looking up current transaction", e);
        }

        throw new TestException();
    }
}