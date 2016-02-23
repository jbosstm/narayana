package org.jboss.narayana.blacktie.jatmibroker.xatmi.services;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.RunServer;
import org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Connection;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Response;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Service;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.TPSVCINFO;

public class TPReturnOpenSession1Service implements Service {
    private static final Logger log = LogManager.getLogger(TPReturnOpenSession1Service.class);

    public Response tpservice(TPSVCINFO svcinfo) throws ConfigurationException, ConnectionException {
        log.info("testtpreturn_service_opensession1");
        svcinfo.getConnection().tpacall(RunServer.getServiceNameTestTPReturn2(), svcinfo.getBuffer(), svcinfo.getFlags());
        return new Response(Connection.TPSUCCESS, 0, svcinfo.getBuffer(), 0);
    }
}
