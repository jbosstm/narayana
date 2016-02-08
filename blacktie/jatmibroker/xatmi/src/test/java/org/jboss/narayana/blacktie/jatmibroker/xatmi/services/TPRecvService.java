package org.jboss.narayana.blacktie.jatmibroker.xatmi.services;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Response;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Service;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.TPSVCINFO;

public class TPRecvService implements Service {
    private static final Logger log = LogManager.getLogger(TPRecvService.class);

    public Response tpservice(TPSVCINFO svcinfo) {
        log.info("testtprecv_service");
        return null;
    }
}
