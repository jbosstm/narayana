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

import com.arjuna.wst.Aborted;
import com.arjuna.wst.Prepared;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.Vote;
import org.jboss.narayana.txframework.api.annotation.lifecycle.wsat.*;
import org.jboss.narayana.txframework.api.annotation.lifecycle.wsat.Error;
import org.jboss.narayana.txframework.api.annotation.management.TxManagement;
import org.jboss.narayana.txframework.api.annotation.service.ServiceRequest;
import org.jboss.narayana.txframework.api.annotation.transaction.WSAT;
import org.jboss.narayana.txframework.api.configuration.BridgeType;
import org.jboss.narayana.txframework.api.management.ATTxControl;
import org.jboss.narayana.txframework.api.management.DataControl;
import org.jboss.narayana.txframework.functional.common.EventLog;
import org.jboss.narayana.txframework.functional.common.ServiceCommand;
import org.jboss.narayana.txframework.functional.common.SomeApplicationException;
import org.jboss.narayana.txframework.functional.interfaces.AT;
import org.jboss.narayana.txframework.functional.interfaces.ATStatefull;
import org.jboss.narayana.txframework.impl.TXControlException;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jws.HandlerChain;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import java.lang.annotation.Annotation;

/**
 * @author Paul Robinson (paul.robinson@redhat.com)
 */
@Remote(ATStatefull.class)
@WSAT(bridgeType = BridgeType.NONE)
@Stateless
@WebService(serviceName = "ATStatefullService", portName = "ATStatefull",
        name = "AT", targetNamespace = "http://www.jboss.com/functional/atstatefull/")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class ATStatefullService implements ATStatefull
{
    @Inject
    DataControl dataControl;
    @TxManagement
    public ATTxControl txControl;
    private boolean rollback = false;
    private EventLog eventLog = new EventLog();

    @WebMethod
    @ServiceRequest
    public void invoke1(ServiceCommand[] serviceCommands) throws SomeApplicationException
    {
        //Check data is unavailable
        if (dataControl.get("data") != null)
        {
            throw new RuntimeException("data should be clear when invoke1 is called");
        }

        dataControl.put("data", "data");
        try
        {
            if (isPresent(ServiceCommand.THROW_APPLICATION_EXCEPTION, serviceCommands))
            {
                throw new SomeApplicationException("Intentionally thrown Exception");
            }

            if (isPresent(ServiceCommand.READ_ONLY, serviceCommands))
            {
                //todo: is this right?
                txControl.readOnly();
            }

            if (isPresent(ServiceCommand.VOTE_ROLLBACK, serviceCommands))
            {
                rollback = true;
            }
        }
        catch (TXControlException e)
        {
            throw new RuntimeException("Error invoking lifecycle methods on the TXControl", e);
        }
    }

    @WebMethod
    @ServiceRequest
    public void invoke2(ServiceCommand[] serviceCommands) throws SomeApplicationException
    {
        //Check data is available
        if (dataControl.get("data") == null)
        {
            throw new RuntimeException("data set in call to 'invoke' was unavailable in call to 'invoke2'");
        }
        try
        {
            if (isPresent(ServiceCommand.THROW_APPLICATION_EXCEPTION, serviceCommands))
            {
                throw new SomeApplicationException("Intentionally thrown Exception");
            }

            if (isPresent(ServiceCommand.READ_ONLY, serviceCommands))
            {
                //todo: is this right?
                txControl.readOnly();
            }

            if (isPresent(ServiceCommand.VOTE_ROLLBACK, serviceCommands))
            {
                rollback = true;
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
    public void clearLogs()
    {
        eventLog.clear();
    }

    @Commit
    @WebMethod(exclude = true)
    public void commit()
    {
        logEvent(Commit.class);
    }

    @Rollback
    @WebMethod(exclude = true)
    public void rollback()
    {
        logEvent(Rollback.class);
    }

    @Prepare
    @WebMethod(exclude = true)
    public Vote prepare()
    {
        logEvent(Prepare.class);
        if (rollback)
        {
            return new Aborted();
        }
        else
        {
            return new Prepared();
        }
    }

    @Unknown
    @WebMethod(exclude = true)
    public void unknown() throws SystemException
    {
        logEvent(Unknown.class);
    }

    @Error
    @WebMethod(exclude = true)
    public void error() throws SystemException
    {
        logEvent(Error.class);
    }

    private boolean isPresent(ServiceCommand expectedServiceCommand, ServiceCommand... serviceCommands)
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

    private void logEvent(Class<? extends Annotation> event)
    {
        //Check data is available
        if (dataControl.get("data") == null)
        {
            eventLog.addDataUnavailable(event);
        }

        eventLog.addEvent(event);
    }

}
