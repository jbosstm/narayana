package org.jboss.jbossts.txframework.functional.interfaces;

import org.jboss.jbossts.txframework.functional.common.EventLog;
import org.jboss.jbossts.txframework.functional.common.ServiceCommand;
import org.jboss.jbossts.txframework.functional.common.SomeApplicationException;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.HandlerChain;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import java.lang.annotation.Annotation;
import java.util.List;

@Remote
public interface AT
{
    @WebMethod
    public void invoke(ServiceCommand... serviceCommands) throws SomeApplicationException;

    @WebMethod
    public EventLog getEventLog();

    @WebMethod
    public void clearEventLog();

}
