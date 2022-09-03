/*
 *
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 *
 */

package com.hp.mwtests.ts.jta.cdi.transactional.stereotype.ejb;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

@Stateless
public class StatelessEjbCaller {

    @Inject
    private TransactionalEjbCalled transactionalEjb;

    public int doWork() {
        return transactionalEjb.inNewTransaction();
    }
}
