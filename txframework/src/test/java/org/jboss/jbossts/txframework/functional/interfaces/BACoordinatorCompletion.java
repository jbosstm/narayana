package org.jboss.jbossts.txframework.functional.interfaces;

import org.jboss.jbossts.txframework.functional.common.EventLog;
import org.jboss.jbossts.txframework.functional.common.ServiceCommand;
import org.jboss.jbossts.txframework.functional.common.SomeApplicationException;
import javax.ejb.Remote;
import javax.jws.WebMethod;

@Remote
public interface BACoordinatorCompletion
{
    @WebMethod
    public void saveData(ServiceCommand... serviceCommands) throws SomeApplicationException;

    @WebMethod
    public EventLog getEventLog();

    @WebMethod
    public void clearEventLog();

}
