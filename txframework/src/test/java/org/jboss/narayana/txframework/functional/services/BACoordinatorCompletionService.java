/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.narayana.txframework.functional.services;

import org.jboss.narayana.txframework.api.annotation.lifecycle.ba.*;
import org.jboss.narayana.txframework.api.annotation.lifecycle.ba.Error;
import org.jboss.narayana.txframework.api.annotation.service.ServiceRequest;
import org.jboss.narayana.txframework.api.annotation.transaction.Compensatable;
import org.jboss.narayana.txframework.api.configuration.transaction.CompletionType;
import org.jboss.narayana.txframework.api.exception.TXControlException;
import org.jboss.narayana.txframework.api.management.TXDataMap;
import org.jboss.narayana.txframework.api.management.WSBATxControl;
import org.jboss.narayana.txframework.functional.common.EventLog;
import org.jboss.narayana.txframework.functional.common.ServiceCommand;
import org.jboss.narayana.txframework.functional.common.SomeApplicationException;
import org.jboss.narayana.txframework.functional.interfaces.BACoordinatorCompletion;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * @author Paul Robinson (paul.robinson@redhat.com)
 */
@Stateless
@WebService(serviceName = "BACoordinatorCompletionService", portName = "BACoordinatorCompletionService",
        name = "BACoordinatorCompletion", targetNamespace = "http://www.jboss.com/functional/ba/coordinatorcompletion/")
@SOAPBinding(style = SOAPBinding.Style.RPC)
@Compensatable(completionType = CompletionType.COORDINATOR)
public class BACoordinatorCompletionService implements BACoordinatorCompletion {

    @Inject
    private WSBATxControl txControl;
    private EventLog eventLog = new EventLog();
    @Inject
    private TXDataMap<String, String> txDataMap;

    @WebMethod
    @ServiceRequest
    //todo: batch up data and only addEvent during confirmCompleted
    public void saveData(ServiceCommand[] serviceCommands) throws SomeApplicationException {

        txDataMap.put("data", "data");
        try {
            if (isPresent(ServiceCommand.THROW_APPLICATION_EXCEPTION, serviceCommands)) {
                throw new SomeApplicationException("Intentionally thrown Exception");
            }

            if (isPresent(ServiceCommand.CANNOT_COMPLETE, serviceCommands)) {
                txControl.cannotComplete();
                return;
            }

            if (isPresent(ServiceCommand.COMPLETE, serviceCommands)) {
                txControl.completed();
            }
        } catch (TXControlException e) {
            throw new RuntimeException("Error invoking lifecycle methods on the TXControl", e);
        }
    }

    @WebMethod
    public EventLog getEventLog() {

        return eventLog;
    }

    @WebMethod
    public void clearEventLog() {

        eventLog.clear();
    }

    //todo: why is this never invoked? Always true for CoordinationCompletion?
    @Compensate
    @WebMethod(exclude = true)
    private void compensate() {

        logEvent(Compensate.class);
    }

    @ConfirmCompleted
    @WebMethod(exclude = true)
    private void confirmCompleted(Boolean success) {

        logEvent(ConfirmCompleted.class);
    }

    @Cancel
    @WebMethod(exclude = true)
    private void cancel() {

        logEvent(Cancel.class);
    }

    @Close
    @WebMethod(exclude = true)
    private void close() {

        logEvent(Close.class);
    }

    @Complete
    @WebMethod(exclude = true)
    private void complete() {

        logEvent(Complete.class);
    }

    @ConfirmCompleted
    @WebMethod(exclude = true)
    private void confirmCompleted(boolean success) {

        logEvent(ConfirmCompleted.class);
    }

    @Error
    @WebMethod(exclude = true)
    private void error() {

        logEvent(org.jboss.narayana.txframework.api.annotation.lifecycle.ba.Error.class);
    }

    @Status
    @WebMethod(exclude = true)
    private String status() {

        logEvent(Status.class);
        return null;
    }

    @Unknown
    @WebMethod(exclude = true)
    private void unknown() {

        logEvent(Unknown.class);
    }

    private boolean isPresent(ServiceCommand expectedServiceCommand, ServiceCommand... serviceCommands) {

        for (ServiceCommand foundServiceCommand : serviceCommands) {
            if (foundServiceCommand == expectedServiceCommand) {
                return true;
            }
        }
        return false;
    }

    private void logEvent(Class<? extends Annotation> event) {
        //Check data is available
        if (txDataMap == null || txDataMap.get("data") == null) {
            eventLog.addDataUnavailable(event);
        }

        eventLog.addEvent(event);
    }
}
