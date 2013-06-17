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
#include "Sleeper.h"

extern void test_tpcall_TPETIME_service(TPSVCINFO *svcinfo) {
	btlogger((char*) "test_tpcall_TPETIME_service");
	::sleeper(60);

	int len = 60;
	char *toReturn = ::tpalloc((char*) "X_OCTET", NULL, len);
	strcpy(toReturn, "test_tpcall_TPETIME_service");
	tpreturn(TPSUCCESS, 0, toReturn, len, 0);
}

extern void test_tpcall_TPEOTYPE_service(TPSVCINFO *svcinfo) {
	btlogger((char*) "test_tpcall_TPEOTYPE_service");
	char *toReturn = ::tpalloc((char*) "X_C_TYPE", (char*) "test", 0);
	strcpy(toReturn, "TPEOTYPE");
	tpreturn(TPSUCCESS, 0, toReturn, 0, 0);
}

extern void test_tpcall_TPESVCFAIL_service(TPSVCINFO *svcinfo) {
	btlogger((char*) "test_tpcall_TPESVCFAIL_service");
	int len = 60;
	char *toReturn = ::tpalloc((char*) "X_OCTET", NULL, len);
	strcpy(toReturn, "test_tpcall_TPESVCFAIL_service");
	tpreturn(TPFAIL, 0, toReturn, len, 0);
}

extern void test_tprecv_TPEV_DISCONIMM_service(TPSVCINFO *svcinfo) {
	btlogger((char*) "test_tprecv_TPEV_DISCONIMM_service");
	long rcvlen = 60;
	long revent = 0;
	char* rcvbuf = (char *) tpalloc((char*) "X_OCTET", NULL, rcvlen);

	int status = ::tprecv(svcinfo->cd, (char **) &rcvbuf, &rcvlen, (long) 0,
			&revent);
	TXINFO txinfo;
	int inTx = ::tx_info(&txinfo);
	bool rbkOnly = (txinfo.transaction_state == TX_ROLLBACK_ONLY);
	btlogger((char*) "status=%d, inTx=%d, rbkOnly=%d", status, inTx, rbkOnly);
}

extern void test_tprecv_TPEV_SVCFAIL_service(TPSVCINFO *svcinfo) {
	btlogger((char*) "test_tprecv_TPEV_SVCFAIL_service");
	int len = 60;
	char *toReturn = ::tpalloc((char*) "X_OCTET", NULL, len);
	strcpy(toReturn, "test_tprecv_TPEV_SVCFAIL_service");
	tpreturn(TPFAIL, 0, toReturn, len, 0);
}

extern void test_no_tpreturn_service(TPSVCINFO *svcinfo) {
	btlogger((char*) "test_no_tpreturn_service");
}
