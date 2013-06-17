package org.jboss.narayana.blacktie.jatmibroker.xatmi.services;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Connection;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Response;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Service;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.TPSVCINFO;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.X_COMMON;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.X_OCTET;

public class TPCallXCommonService implements Service {
    private static final Logger log = LogManager.getLogger(TPCallXCommonService.class);

    public Response tpservice(TPSVCINFO svcinfo) throws ConnectionException, ConfigurationException {

        log.info("test_tpcall_x_common_service");
        boolean ok = false;
        X_COMMON dptr = (X_COMMON) svcinfo.getBuffer();

        if (dptr.getLong("acct_no") == 12345678 && dptr.getShort("amount") == 50) {
            ok = true;
        }

        int len = 60;
        X_OCTET toReturn = (X_OCTET) svcinfo.getConnection().tpalloc("X_OCTET", null);
        if (ok) {
            toReturn.setByteArray("tpcall_x_common".getBytes());
        } else {
            toReturn.setByteArray("fail".getBytes());
        }
        return new Response(Connection.TPSUCCESS, 22, toReturn, 0);
    }
}
