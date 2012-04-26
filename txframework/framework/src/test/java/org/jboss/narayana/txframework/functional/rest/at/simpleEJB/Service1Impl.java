package org.jboss.narayana.txframework.functional.rest.at.simpleEJB;

import org.jboss.narayana.txframework.api.annotation.lifecycle.at.*;
import org.jboss.narayana.txframework.api.annotation.management.DataManagement;
import org.jboss.narayana.txframework.api.annotation.management.TxManagement;
import org.jboss.narayana.txframework.api.annotation.service.ServiceRequest;
import org.jboss.narayana.txframework.api.annotation.transaction.AT;
import org.jboss.narayana.txframework.functional.common.EventLog;
import org.jboss.narayana.txframework.functional.common.ServiceCommand;
import org.jboss.narayana.txframework.functional.common.SomeApplicationException;
import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.ws.rs.core.Response;
import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * @Author paul.robinson@redhat.com 06/04/2012
 */
@Stateless
@AT
public class Service1Impl implements Service1 {

    @DataManagement
    Map TXDataMap;

    @TxManagement
    private boolean rollback = false;
    private EventLog eventLog = new EventLog();

    @WebMethod
    @ServiceRequest
    public Response someServiceRequest(String serviceCommand) throws SomeApplicationException {
        TXDataMap.put("data", "data");

        if (Service1.THROW_APPLICATION_EXCEPTION.equals(serviceCommand)) {
            throw new SomeApplicationException("Intentionally thrown Exception");
        }

        if (VOTE_ROLLBACK.equals(serviceCommand)) {
            rollback = true;
        }
        return Response.ok().build();
    }

    public Response getEventLog() {

        return Response.ok(EventLog.asString(eventLog.getEventLog())).build();
    }

    public Response clearLogs() {

        eventLog.clear();
        return Response.ok().build();
    }

    @Commit
    private void commit() {
        logEvent(Commit.class);
    }

    @Rollback
    private void rollback() {
        logEvent(Rollback.class);
    }

    @Prepare
    private Boolean prepare() {
        logEvent(Prepare.class);
        if (rollback) {
            return false;
        } else {
            return true;
        }
    }

    private void logEvent(Class<? extends Annotation> event) {
        //Check data is available
        if (TXDataMap.get("data") == null) {
            eventLog.addDataUnavailable(event);
        }

        eventLog.addEvent(event);
    }


}
