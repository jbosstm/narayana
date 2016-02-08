package org.jboss.narayana.blacktie.jatmibroker.xatmi.services;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Buffer;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Connection;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Response;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Service;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.TPSVCINFO;

public class TPSendTPSendOnlyService implements Service {
    private static final Logger log = LogManager.getLogger(TPSendTPSendOnlyService.class);

    public Response tpservice(TPSVCINFO svcinfo) throws ConnectionException, ConfigurationException {
        log.info("testtpsend_tpsendonly_service");
        int result = svcinfo.getSession().tpsend(svcinfo.getBuffer(), Connection.TPRECVONLY);
        try {
            Buffer tprecv = svcinfo.getSession().tprecv(0);
        } catch (ConnectionException e) {
            if (e.getTperrno() != Connection.TPEEVENT) {
                log.error("ConnectionException: ", e);
                throw e;
            }
        }
        return null;
    }
}
