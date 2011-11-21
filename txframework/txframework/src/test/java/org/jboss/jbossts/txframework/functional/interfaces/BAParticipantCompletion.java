package org.jboss.jbossts.txframework.functional.interfaces;

import org.jboss.jbossts.txframework.functional.common.EventLog;
import org.jboss.jbossts.txframework.functional.common.ServiceCommand;
import org.jboss.jbossts.txframework.functional.common.SomeApplicationException;
import javax.ejb.Remote;
import javax.jws.WebMethod;

@Remote
public interface BAParticipantCompletion
{
    @WebMethod
    public void saveDataAutoComplete(ServiceCommand... serviceCommands) throws SomeApplicationException;

    @WebMethod
    public void saveDataManualComplete(ServiceCommand... serviceCommands) throws SomeApplicationException;

    @WebMethod
    public EventLog getEventLog();

    @WebMethod
    public void clearEventLog();

}
