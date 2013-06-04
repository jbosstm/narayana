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

#include "TestAssert.h"
#include "testTxAvoid.h"
#include "TxManager.h"
#include "btlogger.h"

#include "AtmiBrokerEnv.h"

#include <stdlib.h>

void initEnv() {	
#ifdef WIN32
	putenv("BLACKTIE_CONFIGURATION=win32");
#else
	putenv("BLACKTIE_CONFIGURATION=linux");
#endif
	AtmiBrokerEnv::get_instance();
}

void destroyEnv(){
	putenv((char*) "BLACKTIE_CONFIGURATION=");
	AtmiBrokerEnv::discard_instance();
}

void doOne() {
try {
		XARecoveryLog log("test_recovery_log");

		XID gid = {1L, 1L, 0L};
		XID xid = XAResourceManager::gen_xid(200, 0L, gid);
		XID xid2 = XAResourceManager::gen_xid(202, 0L, gid);
		XID xid3 = XAResourceManager::gen_xid(203, 0L, gid);
		int cnt = 0;
		char* ior = (char *) "IOR:1";

		// add a record
		BT_ASSERT_EQUAL(log.add_rec(xid, ior), 0);

		// delete it by XID
		BT_ASSERT_EQUAL(log.del_rec(xid), 0);

		// add it back again record
		BT_ASSERT_EQUAL(log.add_rec(xid, ior), 0);

		// find it by xid
		char *iorv = log.find_ior(xid);
		BT_ASSERT(iorv != NULL && strcmp(ior, iorv) == 0);

		// add two more records
		BT_ASSERT_EQUAL(log.add_rec(xid2, (char *) "IOR:2"), 0);
		BT_ASSERT_EQUAL(log.add_rec(xid3, (char *) "IOR:3"), 0);

		// use an iterator to check that there are 3 records
		for (rrec_t* rr = log.find_next(0); rr; rr = log.find_next(rr)) {
			cnt += 1;
			btlogger( (char*) "test_recovery_log deleting %s", (char *) (rr + 1));
			log.del_rec(rr->xid);
		}

		BT_ASSERT(cnt >= 3);
	} catch (RMException e) {
		std::string s = "Error creating recovery log: ";
		s += e.what();
		BT_FAIL(s.c_str());
	}
}

void doTwo() {
	btlogger( (char*) "TestTransactions::test_basic begin");
	atmibroker::tx::TxManager *txm = atmibroker::tx::TxManager::get_instance();
	BT_ASSERT_EQUAL(TX_OK, txm->open());
	BT_ASSERT_EQUAL(TX_OK, txm->begin());
	BT_ASSERT_EQUAL(TX_OK, txm->suspend(123, NULL));
	BT_ASSERT_EQUAL(true, txm->isCdTransactional(123));
	BT_ASSERT_EQUAL(TX_OK, txm->resume(123));
	BT_ASSERT_EQUAL(TX_OK, txm->commit());
	BT_ASSERT_EQUAL(TX_OK, txm->close());
	atmibroker::tx::TxManager::discard_instance();
	btlogger( (char*) "TestTransactions::test_basic pass");
}

 
void doThree(long delay) {
(void) apr_sleep(apr_time_from_sec(delay));
}

void doFour() {
BT_ASSERT_EQUAL(TX_OK, txx_rollback_only());
}

static int fn1(char *a, int i, long l) { return 0; }
static int fn2(XID *x, int i, long l) { return 0; }
static int fn3(XID *, long l1, int i, long l2) { return 0; }
static int fn4(int *ip1, int *ip2, int i, long l) { return 0; }


static struct xa_switch_t real_resource = { "DummyRM", 0L, 0, fn1, fn1, /* open and close */
	fn2, fn2, fn2, fn2, fn2, /*start, end, rollback, prepare, commit */
	fn3, /* recover */
	fn2, /* forget */
	fn4 /* complete */
};
// manufacture a dummy RM transaction id
static XID xid = {
		1L, /* long formatID */
		0L, /* long gtrid_length */
		0L, /* long bqual_length */
		{0} /* char data[XIDDATASIZE]; */
};


void doSix(long delay) {
	(void) apr_sleep(apr_time_from_sec(delay));
}

int count_log_records() {
	XARecoveryLog log;
	int cnt = 0;

	for (rrec_t* rr = log.find_next(0); rr; rr = log.find_next(rr))
		cnt += 1;

	return cnt;
}

int clear_log() {
	XARecoveryLog log;
	rrec_t* rr;
	int nrecs = 0;

	while ((rr = log.find_next(0))) {
		nrecs += 1;
		log.del_rec(rr->xid);
	}

	return nrecs;
}

bool isOTS() {
	return atmibroker::tx::TxManager::get_instance()->isOTS();
}

bool deactivate_objects(long rmid, bool deactivate) {
	XAResourceManager* rm = atmibroker::tx::TxManager::get_instance()->find_rm(rmid);

	if (rm != NULL) {
		rm->deactivate_objects(deactivate);
		return true;
	}

	return false;
}
