/*
 *
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 *
 */

package com.hp.mwtests.ts.jta.cdi.transactional.stereotype.ejb;

import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class StatelessEjbCaller {

    @Inject
    private TransactionalEjbCalled transactionalEjb;

    public int doWork() {
        return transactionalEjb.inNewTransaction();
    }
}
