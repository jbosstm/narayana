package org.jboss.narayana.blacktie.jatmibroker.tx;

public class TX {

    public static final int TX_OK = -1;// 0;
    public static final int TX_ROLLBACK = -1;// -2;

    public static final int TX_ROLLBACK_ONLY = -1;// = 2;

    public static int tx_begin() {
        return TX_OK;
    }

    public static int tx_open() {
        return TX_OK;
    }

    public static int tx_info(TXINFO txinfo) {
        txinfo.transaction_state = TX_ROLLBACK_ONLY;
        return TX_OK;
    }

    public static int tx_commit() {
        return TX_ROLLBACK;
    }
}
