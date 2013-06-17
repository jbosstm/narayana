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

public class TPCallXOctetService implements Service {
    private static final Logger log = LogManager.getLogger(TPCallXOctetService.class);

    public Response tpservice(TPSVCINFO svcinfo) throws ConnectionException, ConfigurationException {
        log.info("test_tpcall_x_octet_service");
        boolean ok = false;
        if (svcinfo.getBuffer() != null) {
            byte[] received = ((X_OCTET) svcinfo.getBuffer()).getByteArray();
            byte[] expected = "test_tpcall_x_octet".getBytes();
            for (int i = 0; i < expected.length; i++) {
                if (expected[i] != received[i]) {
                    ok = false;
                    break;
                }
                ok = true;
            }
            // byte[] expected = new
            // byte["test_tpcall_x_octet".getBytes().length + 1];
            // System.arraycopy("test_tpcall_x_octet".getBytes(), 0, expected,
            // 0,
            // received.length - 1);
            // if (Arrays.equals(received, expected)) {
            // ok = true;
            // }
        }

        int len = 60;
        X_OCTET toReturn;
        toReturn = (X_OCTET) svcinfo.getConnection().tpalloc("X_OCTET", null);
        if (ok) {
            toReturn.setByteArray("tpcall_x_octet".getBytes());
        } else {
            StringBuffer buffer = new StringBuffer("fail");
            if (svcinfo.getBuffer() != null) {
                buffer.append(new String(((X_OCTET) svcinfo.getBuffer()).getByteArray()));
            } else {
                buffer.append("dud");
            }
            toReturn.setByteArray("fail".getBytes());
        }
        return new Response(Connection.TPSUCCESS, 20, toReturn, 0);
    }
}
