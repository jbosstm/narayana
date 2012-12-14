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
import org.jboss.narayana.txframework.api.management.WSBATxControl;
import org.jboss.narayana.txframework.functional.common.EventLog;
import org.jboss.narayana.txframework.functional.common.ServiceCommand;
import org.jboss.narayana.txframework.functional.common.SomeApplicationException;
import org.jboss.narayana.txframework.functional.interfaces.BAParticipantCompletion;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import java.lang.annotation.Annotation;
import java.util.Map;

import static org.jboss.narayana.txframework.functional.common.ServiceCommand.*;


/**
 * @author Paul Robinson (paul.robinson@redhat.com)
 */
@Stateless
@WebService(serviceName = "BAParticipantCompletionService", portName = "BAParticipantCompletionService",
        name = "BAParticipantCompletion", targetNamespace = "http://www.jboss.com/functional/ba/participantcompletion/")
@SOAPBinding(style = SOAPBinding.Style.RPC)
@Compensatable(completionType = CompletionType.PARTICIPANT)
public class BAParticipantCompletionService implements BAParticipantCompletion {

    @Inject
    private WSBATxControl txControl;
    @Inject
    private EventLog eventLog = new EventLog();
    @Inject
    private Map TXDataMap;

    @WebMethod
    @ServiceRequest
    @Completes
    public void saveDataAutoComplete(ServiceCommand... serviceCommands) throws SomeApplicationException {

        saveData(serviceCommands);
    }

    @WebMethod
    @ServiceRequest
    public void saveDataManualComplete(ServiceCommand... serviceCommands) throws SomeApplicationException {

        saveData(serviceCommands);
    }

    private void saveData(ServiceCommand[] serviceCommands) throws SomeApplicationException {

        TXDataMap.put("data", "data");
        try {
            if (present(THROW_APPLICATION_EXCEPTION, serviceCommands)) {
                throw new SomeApplicationException("Intentionally thrown Exception");
            }

            if (present(CANNOT_COMPLETE, serviceCommands)) {
                txControl.cannotComplete();
                return;
            }

            if (present(COMPLETE, serviceCommands)) {
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

    @ConfirmCompleted
    @WebMethod(exclude = true)
    private void confirmCompleted(boolean success) {

        logEvent(ConfirmCompleted.class);
    }

    @Error
    @WebMethod(exclude = true)
    private void error() {

        logEvent(Error.class);
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

    private boolean present(ServiceCommand expectedServiceCommand, ServiceCommand... serviceCommands) {

        for (ServiceCommand foundServiceCommand : serviceCommands) {
            if (foundServiceCommand == expectedServiceCommand) {
                return true;
            }
        }
        return false;
    }

    private void logEvent(Class<? extends Annotation> event) {
        //Check data is available
        if (TXDataMap.get("data") == null) {
            eventLog.addDataUnavailable(event);
        }

        eventLog.addEvent(event);
    }
}
