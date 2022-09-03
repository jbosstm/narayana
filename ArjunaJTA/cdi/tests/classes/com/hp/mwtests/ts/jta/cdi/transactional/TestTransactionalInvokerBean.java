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
