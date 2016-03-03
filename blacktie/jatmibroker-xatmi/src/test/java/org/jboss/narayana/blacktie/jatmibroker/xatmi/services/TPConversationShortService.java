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

public class TPConversationShortService implements Service {
    private static final Logger log = LogManager.getLogger(TPConversationShortService.class);

    public Response tpservice(TPSVCINFO svcinfo) throws ConnectionException, ConfigurationException {
        log.info("testTPConversation_short_service");
        int sendlen = 4;
        X_OCTET sendbuf = (X_OCTET) svcinfo.getConnection().tpalloc("X_OCTET", null);
        sendbuf.setByteArray("hi0".getBytes());
        svcinfo.getSession().tpsend(sendbuf, 0);
        sendbuf.setByteArray("hi1".getBytes());
        return new Response(Connection.TPSUCCESS, 0, sendbuf, 0);
    }
}
