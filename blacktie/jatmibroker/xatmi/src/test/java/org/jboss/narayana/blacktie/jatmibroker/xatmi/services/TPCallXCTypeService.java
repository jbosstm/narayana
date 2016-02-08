package org.jboss.narayana.blacktie.jatmibroker.xatmi.services;

import java.util.Arrays;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Connection;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Response;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Service;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.TPSVCINFO;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.X_C_TYPE;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.X_OCTET;

public class TPCallXCTypeService implements Service {
    private static final Logger log = LogManager.getLogger(TPCallXCTypeService.class);

    public Response tpservice(TPSVCINFO svcinfo) throws ConnectionException, ConfigurationException {
        log.info("test_tpcall_x_c_type_service");
        boolean ok = false;
        X_C_TYPE aptr = (X_C_TYPE) svcinfo.getBuffer();

        byte[] receivedName = new byte[3];
        byte[] charArray = aptr.getByteArray("name");
        System.arraycopy(charArray, 0, receivedName, 0, 3);
        byte[] expectedName = "TOM".getBytes();
        long accountNumber = aptr.getLong("acct_no");

        float fooOne = aptr.getFloatArray("foo")[0];
        float fooTwo = aptr.getFloatArray("foo")[1];

        double balanceOne = aptr.getDoubleArray("balances")[0];
        double balanceTwo = aptr.getDoubleArray("balances")[1];
        if (accountNumber == 12345678 && Arrays.equals(receivedName, expectedName) && fooOne == 1.1F && fooTwo == 2.2F
                && balanceOne == 1.1 && balanceTwo == 2.2) {
            ok = true;
        }
        int len = 60;
        X_OCTET toReturn = (X_OCTET) svcinfo.getConnection().tpalloc("X_OCTET", null);
        if (ok) {
            toReturn.setByteArray("tpcall_x_c_type".getBytes());
        } else {
            toReturn.setByteArray("fail".getBytes());
        }
        return new Response(Connection.TPSUCCESS, 23, toReturn, 0);
    }
}
