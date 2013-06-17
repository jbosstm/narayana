package org.jboss.narayana.blacktie.jatmibroker.xatmi.services;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Connection;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Response;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Service;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.TPSVCINFO;

public class TPSendNonTPCONVService implements Service {
    private static final Logger log = LogManager.getLogger(TPSendNonTPCONVService.class);

    public Response tpservice(TPSVCINFO svcinfo) throws ConnectionException {
        log.info("testtpsend_tpsendonly_service");
        svcinfo.getSession().tpsend(svcinfo.getBuffer(), 0);
        // This should not have reached here
        return new Response(Connection.TPFAIL, -1, svcinfo.getBuffer(), 0);
    }
}
