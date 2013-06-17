/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and others contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */

#ifndef TX_H
#define TX_H

#include "atmiBrokerTxMacro.h"
#include "xa.h"

#define TX_NOT_SUPPORTED   1   /* normal execution */
#define TX_OK              0   /* normal execution */
#define TX_OUTSIDE        -1   /* application is in an RM local
                                  transaction */
#define TX_ROLLBACK       -2   /* transaction was rolled back */
#define TX_MIXED          -3   /* transaction was partially committed
                                  and partially rolled back */
#define TX_HAZARD         -4   /* transaction may have been partially
                                  committed and partially rolled back*/
#define TX_PROTOCOL_ERROR -5   /* routine invoked in an improper
                                  context */
#define TX_ERROR          -6   /* transient error */
#define TX_FAIL           -7   /* fatal error */
#define TX_EINVAL         -8   /* invalid arguments were given */
#define TX_COMMITTED      -9   /* the transaction was heuristically
                                  committed */
#define TX_NO_BEGIN       -100 /* transaction committed plus new
                                  transaction could not be started */
#define TX_ROLLBACK_NO_BEGIN (TX_ROLLBACK+TX_NO_BEGIN)
/* transaction rollback plus new
 transaction could not be started */
#define TX_MIXED_NO_BEGIN (TX_MIXED+TX_NO_BEGIN)
/* mixed plus transaction could not
 be started */
#define TX_HAZARD_NO_BEGIN (TX_HAZARD+TX_NO_BEGIN)
/* hazard plus transaction could not
 be started */
#define TX_COMMITTED_NO_BEGIN (TX_COMMITTED+TX_NO_BEGIN)
/* heuristically committed plus
 transaction could not be started */

/*
 * Definitions for tx_*() routines
 */

/* commit return values */
typedef long COMMIT_RETURN;
#define TX_COMMIT_COMPLETED 0
#define TX_COMMIT_DECISION_LOGGED 1

/* transaction control values */
typedef long TRANSACTION_CONTROL;
#define TX_UNCHAINED 0
#define TX_CHAINED 1

/* type of transaction timeouts */
typedef long TRANSACTION_TIMEOUT;

/* transaction state values */
typedef long TRANSACTION_STATE;
#define TX_ACTIVE 0
#define TX_TIMEOUT_ROLLBACK_ONLY 1
#define TX_ROLLBACK_ONLY 2

/* structure populated by tx_info() */
struct tx_info_t {
 XID                 xid;
 COMMIT_RETURN       when_return;
 TRANSACTION_CONTROL transaction_control;
 TRANSACTION_TIMEOUT transaction_timeout;
 TRANSACTION_STATE   transaction_state;
};
typedef struct tx_info_t TXINFO;

#ifdef __cplusplus
extern "C" {
#endif
extern BLACKTIE_TX_DLL int tx_begin(void);
extern BLACKTIE_TX_DLL int tx_close(void);
extern BLACKTIE_TX_DLL int tx_commit(void);
extern BLACKTIE_TX_DLL int tx_open(void);
extern BLACKTIE_TX_DLL int tx_rollback(void);
extern BLACKTIE_TX_DLL int tx_set_commit_return(COMMIT_RETURN);
extern BLACKTIE_TX_DLL int tx_set_transaction_control(TRANSACTION_CONTROL control);
extern BLACKTIE_TX_DLL int tx_set_transaction_timeout(TRANSACTION_TIMEOUT timeout);
extern BLACKTIE_TX_DLL int tx_info(TXINFO *);
#ifdef __cplusplus
}
#endif

#endif // END TX_H
