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

package org.jboss.narayana.compensations.impl;

import org.jboss.narayana.compensations.api.Action;
import org.jboss.narayana.compensations.api.Compensatable;
import org.jboss.narayana.compensations.api.CompensationHandler;
import org.jboss.narayana.compensations.api.ConfirmationHandler;
import org.jboss.narayana.compensations.api.Transaction;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class TransactionImpl implements Transaction {

    private List<ActionInfo> actions = new LinkedList<>();

    @Override
    public Transaction doAction(Action action) {
        return doAction(action, null, null);
    }

    @Override
    public Transaction doAction(Action action,
            CompensationHandler compensationHandler) {

        return doAction(action, compensationHandler, null);
    }

    @Override
    public Transaction doAction(Action action,
            ConfirmationHandler confirmationHandler) {

        return doAction(action, null, confirmationHandler);
    }

    @Override
    public Transaction doAction(Action action,
            CompensationHandler compensationHandler,
            ConfirmationHandler confirmationHandler) {

        actions.add(new ActionInfo(action, compensationHandler, confirmationHandler));

        return this;
    }

    @Override
    @Compensatable
    public void execute() {
        actions.forEach(actionInfo -> {
            Set<ParticipantManager> managers = enlist(
                    actionInfo.compensationHandler,
                    actionInfo.confirmationHandler);
            try {
                actionInfo.action.execute();
            } catch (RuntimeException e) {
                exit(managers);
                throw e;
            }
            complete(managers);
        });
        actions.clear();
    }

    private Set<ParticipantManager> enlist(CompensationHandler compensationHandler,
            ConfirmationHandler confirmationHandler) {

        Set<ParticipantManager> managers = new HashSet<>();

        if (compensationHandler != null) {
            try {
                managers.add(BAControllerFactory.getInstance().enlist(
                        compensationHandler, null, null));
            } catch (Exception e) {
                throw new RuntimeException(
                        "Failed to enlist compensation handler", e);
            }
        }

        if (confirmationHandler != null) {
            try {
                managers.add(BAControllerFactory.getInstance().enlist(
                        null, confirmationHandler, null));
            } catch (Exception e) {
                throw new RuntimeException(
                        "Failed to enlist confirmation handler", e);
            }
        }

        return managers;
    }

    private void complete(Set<ParticipantManager> managers) {
        managers.forEach(pm -> {
            try {
                pm.completed();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void exit(Set<ParticipantManager> managers) {
        managers.forEach(pm -> {
            try {
                pm.exit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private class ActionInfo {

        private Action action;

        private CompensationHandler compensationHandler;

        private ConfirmationHandler confirmationHandler;

        public ActionInfo(Action action,
                CompensationHandler compensationHandler,
                ConfirmationHandler confirmationHandler) {

            this.action = action;
            this.compensationHandler = compensationHandler;
            this.confirmationHandler = confirmationHandler;
        }

    }

}
