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

static product_t *prods;
static char testid[16];
static char *emps[] = {"8000", "8001", "8002", "8003", "8004", "8005", "8006", "8007"};

static void set_test_id(const char *id) {
	btlogger_debug( "TxLog %s:%d", __FUNCTION__, __LINE__);
	(void) strncpy(testid, id, sizeof (testid));
}

static int send_req(test_req_t *req, char **prbuf) {
	long rsz = sizeof (test_req_t);
	long callflags = 0L;
	test_req_t *resp;
	int rv = 0;

	btlogger_debug( "TxLog %s:%d", __FUNCTION__, __LINE__);
	resp = (test_req_t *) tpalloc((char*) "X_C_TYPE", (char*) "test_req", 0);

	btlogger_debug( "TxLog Invoke Service %s %4d: prod=%d op=%c data=%s dbf=%s tx=%d",
		TXTEST_SVC_NAME, req->id, req->prod, req->op, req->data, req->db, req->txtype);

	if (tpcall((char *) TXTEST_SVC_NAME, (char *) req, sizeof (test_req_t), (char **) &resp, &rsz, callflags) == -1) {
		btlogger_debug( "TxLog TP ERROR tperrno: %d", tperrno);
		rv = -1;
	} else if (prbuf && *prbuf) {
		strncpy(*prbuf, resp->data, sizeof (resp->data));
	}

	btlogger_debug( "TxLog %s:%d tpcall res=%d status=%d", __FUNCTION__, __LINE__, rv, resp->status);
	tpfree((char *) resp);
	return rv;
}

static int count_records(const char *msg, char *key, int in_tx, int expect) {
	int cnt = -1;
	btlogger_debug( "TxLog %s:%d msg=%s key=%s in_tx=%d expect=%d", __FUNCTION__, __LINE__, msg, key, in_tx, expect);

	if (in_tx || start_tx(TX_TYPE_BEGIN) == TX_OK) {
		int rv = 0;
		test_req_t *req;
		test_req_t res;
		product_t *p = prods;
		char *rbuf = (char *) (res.data);

		for (p = prods; p->id != -1; p++) {
			int remote = LOCAL_ACCESS;

			btlogger_debug( "TxLog check product %s", p->pname);

			if (p->pname == NULL)
				continue;

			if ((p->loc & remote) == 0)	/* the RM does not support the requested access type */
				remote = p->loc;

			req = get_buf((remote & REMOTE_ACCESS), key, p->dbname, '1', p->id, TX_TYPE_NONE, expect);
			btlogger_debug( "TxLog invoke prod %s (id=%d) remote=%d dbf=%s", p->pname, p->id, remote, p->dbname);
			btlogger_debug( "TxLog xyz_access op=%c data=%s db=%s", req->op, req->data, req->db);

			rv = ((remote & REMOTE_ACCESS) ? send_req(req, &rbuf) : p->access(req, &res));
			btlogger_debug( "TxLog invoked ok");

			if (rv)
				btlogger_warn( "TxLog BAD REQ %d", rv);

			free_buf((remote & REMOTE_ACCESS), req);
			rv = (rv == 0 ? atoi(res.data) : -1);

			btlogger_debug( "TxLog and count is %d", rv);
			if (rv == -1) {
				btlogger_warn( "TxLog Error: Db %d access error", p->id);
			}

			btlogger_debug( "TxLog product %s (id=%d) dbf=%s returned %d records", p->pname, p->id, p->dbname, rv);

			if (rv != cnt && cnt != -1) {
				btlogger_warn( "TxLog All databases should have the same no of records: db %d cnt %d (prev was %d)", p->id, rv, cnt);
				rv = -1;
			}

			if ((cnt = rv) == -1)
				break;
		}

		if (in_tx || end_tx(TX_TYPE_COMMIT) == TX_OK)
			return cnt;
	}

	return cnt;
}

static int check_count(const char *msg, char *key, int in_tx, int expect) {
	int rcnt;
	btlogger_debug( "TxLog %s:%d", __FUNCTION__, __LINE__);

	if ((rcnt = count_records(msg, key, 0, expect)) != expect) {
		btlogger_warn( "TxLog WRONG NUMBER OF RECORDS: %d expected %d", rcnt, expect);
		return -1;
	}

	btlogger( "TxLog %s: RECORD COUNT: %d expected %d", testid, rcnt, expect);
	return 0;
}

static int db_op(const char *msg, const char *data, char op, int txtype,
				 char **prbuf, int remote, int migrating, int expect) {
	btlogger_debug( "TxLog %s:%d", __FUNCTION__, __LINE__);
	if (msg)
		btlogger( "TxLog %s: %s %s", testid, ((remote | REMOTE_ACCESS) ? "REMOTE" : "LOCAL"), msg);

	if (start_tx(txtype) == TX_OK) {
		int rv = 0;
		test_req_t *req;
		test_req_t res;
		product_t *p = prods;

		for (p = prods; p->id != -1; p++) {
#if 0
			if (migrating && (p->xaflags() & TMNOMIGRATE)) {
				/* the RM does not support tx migration (see XA spec for explanation */
				btlogger_warn( "TxLog Info: RM %d does not support tx migration (switching from remote)", p->id);
				remote = !remote;
			}
#endif

			if ((p->loc & remote) == 0)	/* the RM does not support the requested access type */
				remote = p->loc;

			req = get_buf((remote & REMOTE_ACCESS), data, p->dbname, op, p->id, txtype, expect);
			btlogger_debug( "TxLog invoke prod %s (id=%d) remote=%d dbf=%s", p->pname, p->id, remote, p->dbname);
			btlogger_debug( "TxLog xyz_access op=%c data=%s db=%s", req->op, req->data, req->db);

			rv = ((remote & REMOTE_ACCESS) ? send_req(req, prbuf) : p->access(req, &res));

			if (rv)
				btlogger_warn( "TxLog BAD REQ %d", rv);

			free_buf((remote & REMOTE_ACCESS), req);
		}

		if (end_tx(txtype) == TX_OK)
			return rv;
	}

	return -1;
}

/**
 * ensure that all the target databases start off in the same state
 */
static int setup() {
	int rv;
	btlogger_debug( "TxLog %s:%d", __FUNCTION__, __LINE__);

	/* start off with no records */
	if ((rv = db_op("DELETE AT SETUP", emps[0], '3', TX_TYPE_BEGIN_COMMIT, 0, LOCAL_ACCESS, 0, -1)) != 0)
		return rv;

	if ((rv = check_count("COUNT RECORDS", emps[0], 0, 0)))
		return rv;

	return 0;
}

/**
 * remove all the records added by the test
 */
static int teardown(int *cnt)
{
	int rv;
	btlogger_debug( "TxLog %s:%d", __FUNCTION__, __LINE__);

	/* delete records starting from emps[0], */
	if ((rv = db_op("DELETE", emps[0], '3', TX_TYPE_BEGIN_COMMIT, 0, LOCAL_ACCESS, 0, -1)))
		return rv;

	*cnt = 0;
	if ((rv = check_count("COUNT RECORDS", emps[0], 0, *cnt)))
		return rv;

	return 0;
}

#ifdef TEST0
static int test0(int *cnt)
{
	int rv;

	btlogger_debug( "TxLog %s:%d", __FUNCTION__, __LINE__);

	set_test_id("Test 0");

#if 1
	/* ask the remote service to insert a record */
	if ((rv = db_op("INSERT 1", emps[5], '0', TX_TYPE_BEGIN, 0, REMOTE_ACCESS, 0, -1)))
		return rv;

	/* ask the remote service to insert another record in the same transaction */
	if ((rv = db_op("INSERT 2", emps[6], '0', TX_TYPE_NONE, 0, REMOTE_ACCESS, 0, -1)))
		return rv;

	/* insert a record and end the already running transaction */
	if ((rv = db_op("INSERT", emps[7], '0', TX_TYPE_COMMIT, 0, LOCAL_ACCESS, 1, -1)))
		return rv;

	*cnt += 3;
#else
	if ((rv = db_op("INSERT 1", emps[5], '0', TX_TYPE_BEGIN_COMMIT, 0, LOCAL_ACCESS, 0, -1)))
		return rv;
	*cnt += 1;
#endif
	/* make sure the record count increases by 3 */
	if ((rv = check_count("COUNT RECORDS", emps[0], 0, *cnt)))
		return -1;

	return 0;
}
#endif

/**
 * start a transaction
 * perform a remote insert on each target db
 * perform another remote insert on each target db
 * perform a local insert on each target db
 * commit the transaction
 *
 * Note for databases that do not support tx migration (meaning that performing a
 * remote insert followed by a local insert is not supported) the local operation
 * is switched to a remote one (which ensures that all the updates are done in the
 * same thread).
 */
static int test1(int *cnt)
{
	int rv;
	btlogger_debug( "TxLog ENTER %s:%d", __FUNCTION__, __LINE__);

	set_test_id("Test 1");
	/* ask the remote service to insert a record */
	if ((rv = db_op("INSERT 1", emps[5], '0', TX_TYPE_BEGIN, 0, REMOTE_ACCESS, 0, -1))) {
		btlogger_warn( "TxLog LEAVE %s:%d res=%d", __FUNCTION__, __LINE__, rv);
		return rv;
	}

	/* ask the remote service to insert another record in the same transaction */
	if ((rv = db_op("INSERT 2", emps[6], '0', TX_TYPE_NONE, 0, REMOTE_ACCESS, 0, -1))) {
		btlogger_warn( "TxLog LEAVE %s:%d res=%d", __FUNCTION__, __LINE__, rv);
		return rv;
	}
	/* insert a record and end the already running transaction */
	if ((rv = db_op("INSERT", emps[7], '0', TX_TYPE_COMMIT, 0, LOCAL_ACCESS, 1, -1))) {
		btlogger_warn( "TxLog LEAVE %s:%d res=%d", __FUNCTION__, __LINE__, rv);
		return rv;
	}

	*cnt += 3;

	/* make sure the record count increases by 3 */
	if ((rv = check_count("COUNT RECORDS", emps[0], 0, *cnt))) {
		btlogger_warn( "TxLog LEAVE %s:%d res=%d", __FUNCTION__, __LINE__, rv);
		return -1;
	}

	btlogger_debug( "TxLog LEAVE %s:%d res=%d", __FUNCTION__, __LINE__, rv);
	return 0;
}

/**
 * start a transaction
 * perform a remote insert on each target db
 * perform another remote insert on each target db
 * perform a third remote insert on each target db
 * commit the transaction
 */
static int test2(int *cnt)
{
	int rv;
	btlogger_debug( "TxLog %s:%d", __FUNCTION__, __LINE__);

	set_test_id("Test 2");
	/* ask the remote service to insert a record */
	if ((rv = db_op("INSERT 1", emps[0], '0', TX_TYPE_BEGIN, 0, REMOTE_ACCESS, 0, -1)))
		return rv;

	/* ask the remote service to insert another record in the same transaction */
	if ((rv = db_op("INSERT 2", emps[1], '0', TX_TYPE_NONE, 0, REMOTE_ACCESS, 0, -1)))
		return rv;

	/* insert a record and end the already running transaction */
	if ((rv = db_op("INSERT 3", emps[2], '0', TX_TYPE_COMMIT, 0, REMOTE_ACCESS, 0, -1)))
		return rv;

	*cnt += 3;
	/* make sure the record count increases by 3 */
	if ((rv = check_count("COUNT RECORDS", emps[0], 0, *cnt)))
		return -1;

	return 0;
}

/**
 * start a transaction
 * insert a record into each db
 * commit the transaction
 *
 * repeat the test but rollback the transaction instead of commit
 */
static int test3(int *cnt)
{
	int rv;
	btlogger_debug( "TxLog IN %s:%d", __FUNCTION__, __LINE__);

	set_test_id("Test 3");
	/* ask the remote service to insert a record */
	if ((rv = db_op("INSERT 1", emps[3], '0', TX_TYPE_BEGIN_COMMIT, 0, REMOTE_ACCESS, 0, -1)))
		return rv;

	*cnt += 1;
	/* delete records starting from emps[0] but abort it */
	if ((rv = db_op("INSERT WITH ABORT", emps[4], '1', TX_TYPE_BEGIN_ABORT, 0, REMOTE_ACCESS, 0, -1)))
		return rv;

	if ((rv = check_count("COUNT RECORDS", emps[0], 0, *cnt)))
		return rv;

	btlogger_debug( "TxLog OUT %s:%d success", __FUNCTION__, __LINE__);
	return 0;
}

/**
 * start tx, do a remote insert on all dbs commit the tx
 * start tx, do a remote update on all dbs commit the tx
 * start tx, do a remote delete on all dbs abort the tx
 * start tx, do a local delete on all dbs abort the tx
 */
static int test4(int *cnt)
{
	int rv;
	btlogger_debug( "TxLog %s:%d", __FUNCTION__, __LINE__);

	set_test_id("Test 4");
	/* ask the remote service to insert a record */
	if ((rv = db_op("INSERT 1", emps[4], '0', TX_TYPE_BEGIN_COMMIT, 0, REMOTE_ACCESS, 0, -1)))
		return rv;

	*cnt += 1;
	/* modify one of the records */
	if ((rv = db_op("UPDATE", emps[4], '2', TX_TYPE_BEGIN_COMMIT, 0, REMOTE_ACCESS, 0, -1)))
		return rv;

	/* remote delete records starting from emps[0] with abort*/
	if ((rv = db_op("DELETE WITH ABORT", emps[0], '3', TX_TYPE_BEGIN_ABORT, 0, REMOTE_ACCESS, 0, -1)))
		return rv;

	/* local delete records starting from emps[0] with abort*/
	if ((rv = db_op("DELETE WITH ABORT", emps[0], '3', TX_TYPE_BEGIN_ABORT, 0, LOCAL_ACCESS, 0, -1)))
		return rv;

	if ((rv = check_count("COUNT RECORDS", emps[0], 0, *cnt)))
		return rv;

	return 0;
}

#if defined(TX_RC)   // test recovery
/* cause the program to halt during phase 2 of the transaction 2PC protocol */
static int testrc(int *cnt)
{
	int rv;
	btlogger_debug( "TxLog %s:%d", __FUNCTION__, __LINE__);

	set_test_id("Test 5");
	/* ask the remote service to insert a record but to halt during commit */
	if ((rv = tx_set_commit_return(TX_COMMIT_COMPLETED))) /* report heuristics */
		btlogger_warn( "tx_set_commit_return error: %d" , rv);

	if ((rv = db_op("INSERT 1", emps[4], '0', TX_TYPE_BEGIN_COMMIT_HALT, 0, REMOTE_ACCESS, 0, -1))) {
		btlogger_warn( "tpcall error %d" , rv);
		return rv;
	}

	/*
	 * Unfortunately there is no way of knowing the outcome of commiting the transaction.
	 * TODO check the circumstances under which heuristics are generated
	 * I also need to figure out why killing the server a number of times corrupts the
	 * Berkeley DB (which makes recovery kind of useless)
	 */
	btlogger("tx status: %d (should be -1 meaning no transaction)", get_tx_status());
	(void) tx_set_commit_return(TX_COMMIT_DECISION_LOGGED); /* return when the commit decision is logged */

	return 0;
}
#endif

int run_tests(product_t *prod_array)
{
	int rv, i, cnt = 0;
	struct test {
		const char *name;
		int (*test)(int *);
	} tests[] = {
#if defined(TX_RC)   // test recovery
		{"testrc", testrc},
#elif defined(TX_MCALLS)   // tx extends over multiple tpacalls
		{"test1", test1},
		{"test2", test2},
#elif defined(TX_SCALL)  // tx is active for a single tpacall
		{"test3", test3},
		{"test4", test4},
#else
		{"test1", test1},
		{"test2", test2},
		{"test3", test3},
		{"test4", test4},
#endif
		{0, 0}
	};
	btlogger_debug( "TxLog %s:%d", __FUNCTION__, __LINE__);

	prods = prod_array;

	if (tx_open() != TX_OK)
		return fatal("TxLog ERROR - Could not open RMs");

	if ((rv = setup()))
		return rv;

	for (i = 0; tests[i].test != 0; i++) {
		if ((rv = tests[i].test(&cnt))) {
			btlogger_warn( (char*) "TxLog %s FAILED", tests[i].name);
			return rv;
		}
		btlogger( (char*) "TxLog %s PASSED", tests[i].name);
	}

	btlogger( (char*) "TxLog Tests complete");
	if ((rv = teardown(&cnt)))
		return rv;

	if (tx_close() != TX_OK) {
		btlogger_warn( (char*) "TxLog ERROR - Could not close transaction: ");
		return fatal("ERROR - Could not close RMs");
	}

	return 0;
}
