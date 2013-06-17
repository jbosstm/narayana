package org.jboss.narayana.blacktie.jatmibroker.tx.services;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException;
import org.jboss.narayana.blacktie.jatmibroker.tx.TX;
import org.jboss.narayana.blacktie.jatmibroker.tx.TXINFO;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Buffer;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Response;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Service;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.TPSVCINFO;

public class RollbackOnlyTprecvTPEVDISCONIMMService implements Service {
    private static final Logger log = LogManager.getLogger(RollbackOnlyTprecvTPEVDISCONIMMService.class);

    public Response tpservice(TPSVCINFO svcinfo) throws ConnectionException, ConfigurationException {
        log.info("test_tprecv_TPEV_DISCONIMM_service");
        Buffer status = svcinfo.getSession().tprecv(0);
        TXINFO txinfo = new TXINFO();
        int inTx = TX.tx_info(txinfo);
        boolean rbkOnly = (txinfo.transaction_state == TX.TX_ROLLBACK_ONLY);
        log.info("status=%d, inTx=%d, rbkOnly=%d" + status + " " + inTx + " " + rbkOnly);
        return null;
    }
}
