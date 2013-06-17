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

#include <string.h>
#include "btlogger.h"

#include "xatmi.h"
#include "tx.h"

extern void test_tx_tpcall_x_octet_service_without_tx(TPSVCINFO *svcinfo) {
	btlogger(
			(char*) (char*) "TxLog: service running: test_tx_tpcall_x_octet_service_without_tx");
	int len = 60;
	char *toReturn = ::tpalloc((char*) "X_OCTET", NULL, len);
	TXINFO txinfo;
	int inTx = ::tx_info(&txinfo);
	btlogger(
			(char*) (char*) "TxLog: service running: test_tx_tpcall_x_octet_service_without_tx inTx=%d",
			inTx);
	if (inTx == 0) { // or && txinfo.transaction_state != TX_ACTIVE
		strcpy(toReturn, "test_tx_tpcall_x_octet_service_without_tx");
	} else {
		strcpy(toReturn, svcinfo->data);
	}
	tpreturn(TPSUCCESS, 0, toReturn, len, 0);
}

extern void test_tx_tpcall_x_octet_service_with_tx(TPSVCINFO *svcinfo) {
	btlogger(
			(char*) (char*) "TxLog: service running: test_tx_tpcall_x_octet_service_with_tx");
	int len = 60;
	char *toReturn = ::tpalloc((char*) "X_OCTET", NULL, len);
	TXINFO txinfo;
	int inTx = ::tx_info(&txinfo);
	btlogger(
			(char*) (char*) "TxLog: service running: test_tx_tpcall_x_octet_service_with_tx inTx=%d",
			inTx);
	if (inTx == 1) { // or && txinfo.transaction_state == TX_ACTIVE
		strcpy(toReturn, "test_tx_tpcall_x_octet_service_with_tx");
	} else {
		strcpy(toReturn, svcinfo->data);
	}
	tpreturn(TPSUCCESS, 0, toReturn, len, 0);
}
