package org.jboss.narayana.txframework.functional.interfaces;

import org.jboss.narayana.txframework.functional.common.EventLog;
import org.jboss.narayana.txframework.functional.common.ServiceCommand;
import org.jboss.narayana.txframework.functional.common.SomeApplicationException;
import javax.ejb.Remote;
import javax.jws.WebMethod;

@Remote
public interface ATStatefull
{
    @WebMethod
    public void invoke1(ServiceCommand... serviceCommands) throws SomeApplicationException;

    @WebMethod
    public void invoke2(ServiceCommand... serviceCommands) throws SomeApplicationException;

    @WebMethod
    public EventLog getEventLog();

    @WebMethod
    public void clearLogs();

}
