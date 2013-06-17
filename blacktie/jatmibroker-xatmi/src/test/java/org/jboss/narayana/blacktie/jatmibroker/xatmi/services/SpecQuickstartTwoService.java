package org.jboss.narayana.blacktie.jatmibroker.xatmi.services;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Connection;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Response;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Service;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.TPSVCINFO;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.X_C_TYPE;

public class SpecQuickstartTwoService implements Service {
    private static final Logger log = LogManager.getLogger(SpecQuickstartTwoService.class);

    public Response tpservice(TPSVCINFO svcinfo) throws ConnectionException {
        log.info("inquiry_svc");
        short rval;
        /* extract initial typed buffer sent as part of tpconnect() */
        X_C_TYPE ptr = (X_C_TYPE) svcinfo.getBuffer();

        /*
         * Parse input string, ptr->input, and retrieve records. Return 10 records at a time to client. Records are placed in
         * ptr->output, an array of account records.
         */
        for (int i = 0; i < 5; i++) {
            /* gather from DBMS next 10 records into ptr->output array */
            svcinfo.getSession().tpsend(ptr, Connection.TPSIGRSTRT);
        }
        // TODO DO OK AND FAIL
        if (ptr.getInt("failTest") == 0) {
            rval = Connection.TPSUCCESS;
        } else {
            rval = Connection.TPFAIL; /* global transaction will not commit */
        }
        /* terminate service routine, send no data, and */
        /* terminate connection */
        return new Response(rval, 0, null, 0);
    }
}
