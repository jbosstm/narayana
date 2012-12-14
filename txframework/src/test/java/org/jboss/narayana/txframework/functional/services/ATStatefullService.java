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

import org.jboss.narayana.txframework.api.annotation.lifecycle.at.*;
import org.jboss.narayana.txframework.api.annotation.lifecycle.at.Error;
import org.jboss.narayana.txframework.api.annotation.service.ServiceRequest;
import org.jboss.narayana.txframework.api.annotation.transaction.Transactional;
import org.jboss.narayana.txframework.api.configuration.BridgeType;
import org.jboss.narayana.txframework.functional.common.EventLog;
import org.jboss.narayana.txframework.functional.common.ServiceCommand;
import org.jboss.narayana.txframework.functional.common.SomeApplicationException;
import org.jboss.narayana.txframework.functional.interfaces.ATStatefull;

import javax.ejb.Remote;
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
@Remote(ATStatefull.class)
@Transactional(bridgeType = BridgeType.NONE)
@Stateless
@WebService(serviceName = "ATStatefullService", portName = "ATStatefull",
        name = "AT", targetNamespace = "http://www.jboss.com/functional/atstatefull/")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class ATStatefullService implements ATStatefull {

    @Inject
    private Map TXDataMap;
    private boolean rollback = false;
    private EventLog eventLog = new EventLog();

    @WebMethod
    @ServiceRequest
    public void invoke1(ServiceCommand[] serviceCommands) throws SomeApplicationException {
        //Check data is unavailable
        if (TXDataMap.get("data") != null) {
            throw new RuntimeException("data should be clear when invoke1 is called");
        }

        TXDataMap.put("data", "data");
        if (isPresent(ServiceCommand.THROW_APPLICATION_EXCEPTION, serviceCommands)) {
            throw new SomeApplicationException("Intentionally thrown Exception");
        }

        if (isPresent(ServiceCommand.VOTE_ROLLBACK, serviceCommands)) {
            rollback = true;
        }
    }

    @WebMethod
    @ServiceRequest
    public void invoke2(ServiceCommand[] serviceCommands) throws SomeApplicationException {
        //Check data is available
        if (TXDataMap.get("data") == null) {
            throw new RuntimeException("data set in call to 'invoke' was unavailable in call to 'invoke2'");
        }
        if (isPresent(ServiceCommand.THROW_APPLICATION_EXCEPTION, serviceCommands)) {
            throw new SomeApplicationException("Intentionally thrown Exception");
        }

        if (isPresent(ServiceCommand.VOTE_ROLLBACK, serviceCommands)) {
            rollback = true;
        }
    }

    @WebMethod
    public EventLog getEventLog() {

        return eventLog;
    }

    @WebMethod
    public void clearLogs() {

        eventLog.clear();
    }

    @Commit
    @WebMethod(exclude = true)
    public void commit() {

        logEvent(Commit.class);
    }

    @Rollback
    @WebMethod(exclude = true)
    public void rollback() {

        logEvent(Rollback.class);
    }

    @Prepare
    @WebMethod(exclude = true)
    public Boolean prepare() {

        logEvent(Prepare.class);
        if (rollback) {
            return false;
        } else {
            return true;
        }
    }

    @Unknown
    @WebMethod(exclude = true)
    public void unknown() {

        logEvent(Unknown.class);
    }

    @Error
    @WebMethod(exclude = true)
    public void error() {

        logEvent(Error.class);
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
        if (TXDataMap.get("data") == null) {
            eventLog.addDataUnavailable(event);
        }

        eventLog.addEvent(event);
    }

}
