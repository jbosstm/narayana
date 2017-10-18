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

package org.jboss.narayana.compensations.functional.recovery.deployment;

import org.jboss.logging.Logger;
import org.jboss.narayana.compensations.api.Compensatable;
import org.jboss.narayana.compensations.api.CompensatableAction;
import org.jboss.narayana.compensations.api.CompensationManager;
import org.jboss.narayana.compensations.api.EnlistException;

import javax.inject.Inject;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class Executor {

    private static final Logger LOGGER = Logger.getLogger(Executor.class);

    @Inject
    private CompensationManager compensationManager;

    @Inject
    private CompensationScopedData compensationScopedData;

    @Inject
    private CompensatableAction compensatableAction;

    @Inject
    private CompensatableBean compensatableBean;

    @Compensatable
    public void executeInLocalTransaction(Options options) throws EnlistException {
        LOGGER.info("executing in local transaction");

        LOGGER.info("triggering byteman rule");
        BytemanHelper.getInstance().triggerRule(options.getTestName());

        LOGGER.info("settings compensation scoped data to '" + options.getCompensationScopedData() + "'");
        compensationScopedData.setData(options.getCompensationScopedData());

        if (options.isCompensate()) {
            LOGGER.info("setting compensate only");
            compensationManager.setCompensateOnly();
        }

        if (options.isTxCompensate()) {
            LOGGER.info("enabling @TxCompensate");
            compensatableBean.enableTxCompensate();
        }

        if (options.isTxConfirm()) {
            LOGGER.info("enabling @TxConfirm");
            compensatableBean.enableTxConfirm();
        }

        if (options.isCompensatableActionCompensation()) {
            LOGGER.info("registering compensatable action with compensation handler with data '"
                    + options.getCompensatableActionData() + "'");
            compensatableAction.addWork(() -> System.out.println("Compensatable action with compensation handler"),
                    new CompensatableActionCompensationHandler(options.getCompensatableActionData())).execute();
        }

        if (options.isCompensatableActionConfirmation()) {
            LOGGER.info("registering compensatable action with confirmation handler with data '"
                    + options.getCompensatableActionData() + "'");
            compensatableAction.addWork(() -> System.out.println("Compensatable action with confirmation handler"),
                    new CompensatableActionConfirmationHandler(options.getCompensatableActionData())).execute();
        }
    }

    @Compensatable(distributed = true)
    public void executeInDistributedTransaction(Options options) throws EnlistException {
        LOGGER.info("executing in distributed transaction");
        executeInLocalTransaction(options);
    }

}
