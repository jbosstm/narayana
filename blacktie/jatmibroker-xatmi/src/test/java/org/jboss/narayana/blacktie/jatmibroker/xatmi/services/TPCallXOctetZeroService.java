package org.jboss.narayana.blacktie.jatmibroker.xatmi.services;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Response;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Service;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.TPSVCINFO;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.X_OCTET;

public class TPCallXOctetZeroService implements Service {
    private static final Logger log = LogManager.getLogger(TPCallXOctetZeroService.class);

    public Response tpservice(TPSVCINFO svcinfo) throws ConnectionException, ConfigurationException {
        byte[] recv = ((X_OCTET) svcinfo.getBuffer()).getByteArray();
        byte[] toSend = new byte[recv.length];
        int j = recv.length;
        for (int i = 0; i < toSend.length; i++) {
            toSend[i] = recv[--j];
        }
        X_OCTET toReturn = (X_OCTET) svcinfo.getConnection().tpalloc("X_OCTET", null);
        toReturn.setByteArray(toSend);
        return new Response((short) 0, 0, toReturn, 0);
    }
}
