package org.jboss.narayana.blacktie.jatmibroker.xatmi.services;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Response;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Service;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.TPSVCINFO;

public class TPReturnService implements Service {
    private static final Logger log = LogManager.getLogger(TPReturnService.class);

    public Response tpservice(TPSVCINFO svcinfo) {
        log.info("testtpreturn_service");
        throw new RuntimeException("tpreturn exception");
    }
}
