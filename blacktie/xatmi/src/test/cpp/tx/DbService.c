/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc., and others contributors as indicated
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
#include "tx/request.h"

#ifdef TX_RC
#include "testrm.h"

static void inject_fault(int xa_method, int type)
{
	btlogger( "TxLog inject_fault check type=0x%x", type);
	if (type & TX_TYPE_HALT) {
		fault_t fault1 = {0, 202, O_XA_COMMIT, XA_OK, F_HALT, (void*) 0};

		btlogger( "TxLog inject_fault adding halt");
		(void) dummy_rm_add_fault(&fault1);
	}
}
#else
#define inject_fault(xa_method, type)
#endif

#ifdef UNITTEST
void tx_db_service(TPSVCINFO *svcinfo)
#else
void BAR(TPSVCINFO * svcinfo)
#endif
{
	test_req_t *req = (test_req_t *) svcinfo->data;
	test_req_t *resp = (test_req_t *) tpalloc((char*) "X_C_TYPE", (char*) "test_req", 0);
	product_t *p = products;

	btlogger_debug( "TxLog %s service %s running", __FUNCTION__, TXTEST_SVC_NAME);
	resp->status = -1;

	inject_fault(O_XA_COMMIT, req->txtype);

	for (p = products; p->id != -1; p++) {
		if (req->prod == p->id) {
			int rv;
			strncpy(req->db, p->dbname, sizeof(req->db));
			btlogger_debug("TxLog Service %s %4d: prod=%8s (id=%d) op=%c tx=0x%x data=%s", TXTEST_SVC_NAME,
					req->id, p->pname, p->id, req->op, req->txtype, req->data);
			rv = p->access(req, resp);
			btlogger_debug("TxLog Service %s %4d: resp->status=%d rv=%d", TXTEST_SVC_NAME, req->id, resp->status, rv);

			break;
		}
	}

	tpreturn(TPSUCCESS, resp->status, (char *) resp, sizeof (test_req_t), 0);
}
