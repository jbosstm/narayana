/*
 * Copyright The Narayana Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.hp.mwtests.ts.jta.cdi.transactional;

import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public abstract class AbstractBusinessLogic {

    private static final Logger LOGGER = Logger.getLogger(AbstractBusinessLogic.class);

    public void doSomethingInAbstractClass(final Throwable throwable) throws Throwable {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(getClass().getSimpleName() + ".doSomethingInAbstractClass");
        }

        doSomethingInConcreteClass(throwable);
    }

    protected abstract void doSomethingInConcreteClass(final Throwable throwable) throws Throwable;

}
