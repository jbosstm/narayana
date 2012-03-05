package org.jboss.narayana.txframework.functional.interfaces;

import org.jboss.narayana.txframework.functional.common.SomeApplicationException;
import org.jboss.narayana.txframework.functional.common.EventLog;
import org.jboss.narayana.txframework.functional.common.ServiceCommand;

import javax.jws.WebMethod;

public interface AT
{
    @WebMethod
    public void invoke(ServiceCommand... serviceCommands) throws SomeApplicationException;

    @WebMethod
    public EventLog getEventLog();

    @WebMethod
    public void clearLogs();

}
