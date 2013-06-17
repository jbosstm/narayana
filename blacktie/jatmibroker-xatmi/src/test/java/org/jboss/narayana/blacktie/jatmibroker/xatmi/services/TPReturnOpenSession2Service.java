package org.jboss.narayana.blacktie.jatmibroker.xatmi.services;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Connection;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Response;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Service;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.TPSVCINFO;

public class TPReturnOpenSession2Service implements Service {
    private static final Logger log = LogManager.getLogger(TPReturnOpenSession2Service.class);

    public Response tpservice(TPSVCINFO svcinfo) {
        log.info("testtpreturn_service_opensession2");
        return new Response(Connection.TPSUCCESS, 0, svcinfo.getBuffer(), 0);
    }
}
