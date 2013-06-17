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

product_t products[] = {
	{0, "null db", "null", ANY_ACCESS, null_access, null_xaflags},
#ifdef ORACLE
	{1, "ora - blacktie", "blacktie", ANY_ACCESS, ora_access, ora_xaflags},
	{2, "ora - bt", "bt", ANY_ACCESS, ora_access, ora_xaflags},
#endif
	{-1, 0, 0, 0, 0},
};

/* helper methods for controling transactions */
int is_begin(enum TX_TYPE txtype) {
	btlogger_debug( "TxLog %s:%d", __FUNCTION__, __LINE__);
	return (txtype & TX_TYPE_BEGIN);
}
int is_commit(enum TX_TYPE txtype) {
	btlogger_debug( "TxLog %s:%d", __FUNCTION__, __LINE__);
	return (txtype & TX_TYPE_COMMIT);
}
int is_abort(enum TX_TYPE txtype) {
	btlogger_debug( "TxLog %s:%d", __FUNCTION__, __LINE__);
	return (txtype & TX_TYPE_ABORT);
}
int start_tx(enum TX_TYPE txtype) {
	int rv = TX_OK;

	btlogger_debug( "TxLog %s:%d", __FUNCTION__, __LINE__);
	if (is_begin(txtype)) {
		btlogger_debug( "TxLog - Starting Transaction");
		rv = tx_begin();
	}

	if (rv != TX_OK)
		btlogger_warn( "TxLog TX ERROR %d starting transaction", rv);

	return rv;
}
int end_tx(enum TX_TYPE txtype) {
	int rv = TX_OK;
	btlogger_debug( "%s:%d", __FUNCTION__, __LINE__);

	if (is_commit(txtype)) {
		btlogger_debug( "TxLog - Commiting transaction");
		rv = tx_commit();
	} else if (is_abort(txtype)) {
		btlogger_debug( "TxLog - Rolling back transaction");
		rv = tx_rollback();
	}

	if (rv != TX_OK)
		btlogger_warn( "TxLog TX finish error %d", rv);

	return rv;
}

int is_tx_in_state(enum TX_TYPE txtype) {
	int txs;
	int ts;
/*	btlogger_debug( "TxLog %s:%d %d", __FUNCTION__, __LINE__, txtype);*/
	
	ts = (txtype == TX_TYPE_NONE ? -1 : TX_ACTIVE);
	txs = get_tx_status();

	btlogger_debug("TxLog validating tx status actual %d vrs desired %d", txs, ts);

	return (txs == ts);
}

int get_tx_status()
{
	TXINFO txinfo;
	int rv = tx_info(&txinfo);

	if (rv < 0) {
		btlogger_warn("TxLog is_tx_in_state tx_info error: %d", rv);
		return rv;
	}

	btlogger_debug("TxLog tx status %d", txinfo.transaction_state);

	return (txinfo.transaction_state);
}

static int reqid = 0;
static void _init_req(test_req_t *req, int prodid, const char *dbfile, const char *data, char op, enum TX_TYPE txtype, int expect) {
	btlogger_debug( "TxLog %s:%d", __FUNCTION__, __LINE__);
	req->prod = prodid;
	req->txtype = txtype;
	req->expect = expect;
	req->id = ++reqid;
	req->op = op;
	req->status = 0;
	req->data[0] = 0;
	req->db[0] = 0;

	if (data)
		(void) strncpy(req->data, data, sizeof(req->data) - 1);
	if (dbfile)
		(void) strncpy(req->db, dbfile, sizeof(req->db) - 1);
}

test_req_t * get_buf(int remote, const char *data, const char *dbfile, char op, int prod, enum TX_TYPE txtype, int expect) {
	test_req_t *req;
	btlogger_debug( "TxLog %s:%d", __FUNCTION__, __LINE__);

	if (remote)
		req = (test_req_t *) tpalloc((char*) "X_C_TYPE", (char*) "test_req", 0);
	else
		req = (test_req_t *) malloc(sizeof (test_req_t));

	if (req != NULL) {
		int foo = sizeof (test_req_t);
		(void *) memset(req, 0, foo);
		_init_req(req, prod, dbfile, data, op, txtype, expect);
	} else {
		(void) fatal("out of memory (for alloc)");
	}

	return req;
}

void free_buf(int remote, test_req_t *req) {
	btlogger_debug( "TxLog %s:%d", __FUNCTION__, __LINE__);
	if (remote)
		tpfree((char *) req);
	else
		free(req);
}

int fail(const char *reason, int ret)
{
	btlogger_debug( "TxLog %s:%d", __FUNCTION__, __LINE__);
	btlogger_warn( "TxLog %s: %d\n", reason, ret);
	return ret;
}

int fatal(const char *msg)
{
	btlogger_debug( "TxLog %s:%d: %s", __FUNCTION__, __LINE__, msg);
	return -1;
}

long null_xaflags()
{
	btlogger_debug( "TxLog %s:%d", __FUNCTION__, __LINE__);
	return 0L;
}

int null_access(test_req_t *req, test_req_t *resp)
{
	btlogger_debug( "TxLog %s:%d", __FUNCTION__, __LINE__);
	resp->status = 0;
	(void) btlogger_snprintf(resp->data, sizeof(resp->data), "%d", req->expect);

	btlogger_debug( "TxLog null_access: prod id=%d (%s) op=%c res=%s", req->prod, req->db, req->op, resp->data);

	return 0;
}
