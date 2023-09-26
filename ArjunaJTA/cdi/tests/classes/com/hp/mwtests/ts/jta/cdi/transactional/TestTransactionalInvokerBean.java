/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.hp.mwtests.ts.jta.cdi.transactional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

/**
 * @author <a href="mailto:Tomasz%20Krakowiak%20%3ctomasz.krakowiak@efish.pl%3c">Tomasz Krakowiak
 *         &lt;tomasz.krakowiak@efish.pl&gt;</a>
 */
@ApplicationScoped
public class TestTransactionalInvokerBean {
    @Transactional(Transactional.TxType.REQUIRED)
    public void invokeInTxRequired(Runnable runnable) {
        runnable.run();
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void invokeInTxRequiresNew(Runnable runnable) {
        runnable.run();
    }

    @Transactional(Transactional.TxType.MANDATORY)
    public void invokeInTxMandatory(Runnable runnable) {
        runnable.run();
    }

    @Transactional(Transactional.TxType.NOT_SUPPORTED)
    public void invokeInTxNotSupported(Runnable runnable) {
        runnable.run();
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public void invokeInTxSupports(Runnable runnable) {
        runnable.run();
    }
    @Transactional(Transactional.TxType.NEVER)
    public void invokeInTxNever(Runnable runnable) {
        runnable.run();
    }
}