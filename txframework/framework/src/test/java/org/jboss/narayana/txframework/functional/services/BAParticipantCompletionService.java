/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package org.jboss.narayana.txframework.functional.services;

import org.jboss.narayana.txframework.api.annotation.lifecycle.wsba.Error;
import org.jboss.narayana.txframework.api.annotation.lifecycle.wsba.*;
import org.jboss.narayana.txframework.api.annotation.management.TxManagement;
import org.jboss.narayana.txframework.api.annotation.service.ServiceRequest;
import org.jboss.narayana.txframework.api.annotation.transaction.WSBA;
import org.jboss.narayana.txframework.api.configuration.transaction.CompletionType;
import org.jboss.narayana.txframework.api.management.WSBATxControl;
import org.jboss.narayana.txframework.functional.common.SomeApplicationException;
import org.jboss.narayana.txframework.functional.interfaces.BAParticipantCompletion;
import org.jboss.narayana.txframework.functional.common.EventLog;
import org.jboss.narayana.txframework.functional.common.ServiceCommand;
import org.jboss.narayana.txframework.impl.TXControlException;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jws.HandlerChain;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

/**
 * @author Paul Robinson (paul.robinson@redhat.com)
 */
@Stateless
@WebService(serviceName = "BAParticipantCompletionService", portName = "BAParticipantCompletionService",
        name = "BAParticipantCompletion", targetNamespace = "http://www.jboss.com/functional/ba/participantcompletion/")
//todo: Can the framework specify the handlerchain if not present? Would have to be added earlier in the chain than we currently intercept
@HandlerChain(file = "/context-handlers.xml", name = "Context Handlers")
@SOAPBinding(style = SOAPBinding.Style.RPC)
@WSBA(completionType = CompletionType.PARTICIPANT)
public class BAParticipantCompletionService implements BAParticipantCompletion
{
    @TxManagement
    public WSBATxControl txControl;
    @Inject
    private EventLog eventLog = new EventLog();

    @WebMethod
    @ServiceRequest
    @Completes
    public void saveDataAutoComplete(ServiceCommand... serviceCommands) throws SomeApplicationException
    {
        saveData(serviceCommands);
    }

    @WebMethod
    @ServiceRequest
    public void saveDataManualComplete(ServiceCommand... serviceCommands) throws SomeApplicationException
    {
        saveData(serviceCommands);
    }

    private void saveData(ServiceCommand[] serviceCommands) throws SomeApplicationException
    {
        try
        {
            if (present(THROW_APPLICATION_EXCEPTION, serviceCommands))
            {
                throw new SomeApplicationException("Intentionally thrown Exception");
            }

            if (present(CANNOT_COMPLETE, serviceCommands))
            {
                txControl.cannotComplete();
                return;
            }

            if (present(COMPLETE, serviceCommands))
            {
                txControl.completed();
            }
        }
        catch (TXControlException e)
        {
            throw new RuntimeException("Error invoking lifecycle methods on the TXControl", e);
        }
    }

    @WebMethod
    public EventLog getEventLog()
    {
        return eventLog;
    }

    @WebMethod
    public void clearEventLog()
    {
        eventLog.clear();
    }

    @Compensate
    @WebMethod(exclude = true)
    public void compensate()
    {
        eventLog.add(Compensate.class);
    }

    @ConfirmCompleted
    @WebMethod(exclude = true)
    public void confirmCompleted(Boolean success)
    {
        eventLog.add(ConfirmCompleted.class);
    }

    @Cancel
    @WebMethod(exclude = true)
    public void cancel()
    {
        eventLog.add(Cancel.class);
    }

    @Close
    @WebMethod(exclude = true)
    public void close()
    {
        eventLog.add(Close.class);
    }

    @ConfirmCompleted
    @WebMethod(exclude = true)
    public void confirmCompleted(boolean success)
    {
        eventLog.add(ConfirmCompleted.class);
    }

    @Error
    @WebMethod(exclude = true)
    public void error()
    {
        eventLog.add(org.jboss.narayana.txframework.api.annotation.lifecycle.wsba.Error.class);
    }

    @Status
    @WebMethod(exclude = true)
    public String status()
    {
        eventLog.add(Status.class);
        return null;
    }

    @Unknown
    @WebMethod(exclude = true)
    public void unknown()
    {
        eventLog.add(Unknown.class);
    }

    private boolean present(ServiceCommand expectedServiceCommand, ServiceCommand... serviceCommands)
    {
        for (ServiceCommand foundServiceCommand : serviceCommands)
        {
            if (foundServiceCommand == expectedServiceCommand)
            {
                return true;
            }
        }
        return false;
    }
}
