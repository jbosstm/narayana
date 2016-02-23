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
import org.jboss.narayana.blacktie.jatmibroker.xatmi.TestTPConversation;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.X_OCTET;

public class TPReturnTpurcodeService implements Service {
    private static final Logger log = LogManager.getLogger(TPReturnTpurcodeService.class);

    public Response tpservice(TPSVCINFO svcinfo) throws ConnectionException, ConfigurationException {
        log.info("testtpreturn_service_tpurcode");
        X_OCTET toReturn = (X_OCTET) svcinfo.getConnection().tpalloc("X_OCTET", null);
        toReturn.setByteArray(new byte[]{0});
        if (TestTPConversation.strcmp(svcinfo.getBuffer(), "24") == 0) {
            return new Response(Connection.TPSUCCESS, 24, toReturn, 0);
        } else {
            return new Response(Connection.TPSUCCESS, 77, toReturn, 0);
        }
    }
}
