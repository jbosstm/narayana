/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.narayana.compensations.internal;

import org.jboss.logging.Logger;
import org.jboss.narayana.compensations.api.CompensatableWork;
import org.jboss.narayana.compensations.api.Compensatable;
import org.jboss.narayana.compensations.api.CompensatableAction;
import org.jboss.narayana.compensations.api.CompensationHandler;
import org.jboss.narayana.compensations.api.CompensationManager;
import org.jboss.narayana.compensations.api.ConfirmationHandler;
import org.jboss.narayana.compensations.api.EnlistException;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@Dependent
public class CompensatableActionImpl implements CompensatableAction {

    private static final Logger LOGGER = Logger.getLogger(CompensatableActionImpl.class);

    @Inject
    private CompensationManager compensationManager;

    private List<WorkInfo> workList = new LinkedList<>();

    @Override
    public CompensatableAction addWork(CompensatableWork compensatableWork, CompensationHandler compensationHandler) {
        return addWork(compensatableWork, compensationHandler, null);
    }

    @Override
    public CompensatableAction addWork(CompensatableWork compensatableWork, ConfirmationHandler confirmationHandler) {
        return addWork(compensatableWork, null, confirmationHandler);
    }

    @Override
    public CompensatableAction addWork(CompensatableWork compensatableWork, CompensationHandler compensationHandler,
            ConfirmationHandler confirmationHandler) {
        workList.add(new WorkInfo(compensatableWork, compensationHandler, confirmationHandler));

        return this;
    }

    @Override
    @Compensatable
    public void execute() throws EnlistException {
        for (WorkInfo workInfo : workList) {
            Set<ParticipantManager> managers = enlistHandlers(workInfo);
            try {
                workInfo.compensatableWork.execute();
            } catch (RuntimeException e) {
                exit(managers); // Following the behaviour of ParticipantInterceptor.
                throw e;
            }
            complete(managers);
        }

        workList.clear();
    }

    private Set<ParticipantManager> enlistHandlers(WorkInfo workInfo) throws EnlistException {
        Set<ParticipantManager> managers = new HashSet<>();

        if (workInfo.compensationHandler != null) {
            try {
                managers.add(BAControllerFactory.getInstance().enlist(workInfo.compensationHandler, null, null));
            } catch (Exception e) {
                compensationManager.setCompensateOnly();
                throw new EnlistException("Failed to enlist compensation handler", e);
            }
        }

        if (workInfo.confirmationHandler != null) {
            try {
                managers.add(BAControllerFactory.getInstance().enlist(null, workInfo.confirmationHandler, null));
            } catch (Exception e) {
                exit(managers); // Telling previous manager to exit.
                compensationManager.setCompensateOnly();
                throw new EnlistException("Failed to enlist confirmation handler", e);
            }
        }

        return managers;
    }

    private void complete(Set<ParticipantManager> managers) {
        managers.forEach(pm -> {
            try {
                pm.completed();
            } catch (Exception e) {
                LOGGER.warn("Failed to complete compensatable action", e);
            }
        });
    }

    private void exit(Set<ParticipantManager> managers) {
        managers.forEach(pm -> {
            try {
                pm.exit();
            } catch (Exception e) {
                LOGGER.warn("Failed to exit compensatable action", e);
            }
        });
    }

    private static class WorkInfo {

        private CompensatableWork compensatableWork;

        private CompensationHandler compensationHandler;

        private ConfirmationHandler confirmationHandler;

        WorkInfo(CompensatableWork compensatableWork, CompensationHandler compensationHandler,
                ConfirmationHandler confirmationHandler) {
            this.compensatableWork = compensatableWork;
            this.compensationHandler = compensationHandler;
            this.confirmationHandler = confirmationHandler;
        }

    }

}