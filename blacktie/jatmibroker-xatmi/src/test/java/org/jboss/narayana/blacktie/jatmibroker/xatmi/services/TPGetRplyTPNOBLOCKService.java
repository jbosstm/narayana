package org.jboss.narayana.blacktie.jatmibroker.xatmi.services;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Connection;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Response;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Service;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.TPSVCINFO;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.X_OCTET;

public class TPGetRplyTPNOBLOCKService implements Service {
    private static final Logger log = LogManager.getLogger(TPGetRplyTPNOBLOCKService.class);

    public Response tpservice(TPSVCINFO svcinfo) throws ConnectionException, ConfigurationException {
        String response = "test_tpgetrply_TPNOBLOCK";
        log.info(response);

        int sendlen = response.length() + 1;
        X_OCTET toReturn = (X_OCTET) svcinfo.getConnection().tpalloc("X_OCTET", null);
        toReturn.setByteArray(response.getBytes());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            log.error("Was interrupted");
        }
        return new Response(Connection.TPSUCCESS, 0, toReturn, 0);
    }
}
