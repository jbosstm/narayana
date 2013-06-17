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
#include "TestAssert.h"

#ifndef WIN32
#include "ace/OS_NS_unistd.h"
#endif

#include "ThreadLocalStorage.h"
#include "BaseServerTest.h"
#include "XATMITestSuite.h"

#include "xatmi.h"
#include "tx.h"

#include "TestTxTPCall.h"
#include "Sleeper.h"

#if defined(__cplusplus)
extern "C" {
#endif
/* service routines */
static void tx_fill_buf_rtn(TPSVCINFO *svcinfo) {
	int len = 60;
	char *toReturn = ::tpalloc((char*) "X_OCTET", NULL, len);
	TXINFO txinfo;
	int inTx = ::tx_info(&txinfo);

	strcpy(toReturn, "inTx=");
	strcat(toReturn, inTx ? "true" : "false");

	tpreturn(TPSUCCESS, 0, toReturn, len, 0);
}

void test_tx_tpcall_x_octet_service_tardy(TPSVCINFO *svcinfo) {
	btlogger((char*) "TxLog: service running: test_tx_tpcall_x_octet_service_tardy");
	::sleeper(10);
	tx_fill_buf_rtn(svcinfo);
}

void test_tx_tpcall_x_octet_service_without_tx(TPSVCINFO *svcinfo) {
	btlogger((char*) "TxLog: service running: test_tx_tpcall_x_octet_service_without_tx");
	tx_fill_buf_rtn(svcinfo);
}

void test_tx_tpcall_x_octet_service_with_tx(TPSVCINFO *svcinfo) {
	btlogger((char*) "TxLog: service running: test_tx_tpcall_x_octet_service_with_tx");
	tx_fill_buf_rtn(svcinfo);
}
#if defined(__cplusplus)
}
#endif

/* test setup */
void TestTxTPCall::setUp() {
	btlogger((char*) "TestTxTPCall::setUp");
	BaseServerTest::setUp();

	BT_ASSERT(tx_open() == TX_OK);
	sendlen = strlen("TestTxTPCall") + 1;
	BT_ASSERT((sendbuf = (char *) tpalloc((char*) "X_OCTET", NULL, sendlen)) != NULL);
	(void) strcpy(sendbuf, "TestTxTPCall");
	rcvlen = 60;
	BT_ASSERT((rcvbuf = (char *) tpalloc((char*) "X_OCTET", NULL, rcvlen)) != NULL);
	BT_ASSERT(tperrno == 0);
}

/* test teardown */
void TestTxTPCall::tearDown() {
	btlogger((char*) "TestTxTPCall::tearDown");
	::tpfree(sendbuf);
	::tpfree(rcvbuf);

	// test may have left a txn on the thread which would cause tx_close to fail
	destroySpecific(TSS_KEY);
	int rc = tpunadvertise((char*) "tpcall_x_octet");
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(rc != -1);
	BT_ASSERT(tx_close() == TX_OK);

	// Clean up server
	BaseServerTest::tearDown();
}

/* client routines */
void TestTxTPCall::test_timeout_no_tx() {
	btlogger((char*) "TestTxTPCall: test_timeout_no_tx");
	int rc = tpadvertise((char*) "tpcall_x_octet", test_tx_tpcall_x_octet_service_tardy);
	BT_ASSERT(tperrno == 0 && rc != -1);
	int cd = ::tpcall((char*) "tpcall_x_octet", (char *) sendbuf, sendlen, (char **) &rcvbuf, &rcvlen, (long) 0);
	BT_ASSERT(cd != -1);
	BT_ASSERT(tperrno != TPETIME);
	BT_ASSERT_MESSAGE(rcvbuf, strcmp(rcvbuf, "inTx=false") == 0);
}

void TestTxTPCall::test_timeout_with_tx() {
	btlogger((char*) "TestTxTPCall: test_timeout_with_tx");
	int rv1 = tpadvertise((char*) "tpcall_x_octet", test_tx_tpcall_x_octet_service_tardy);
	BT_ASSERT(tperrno == 0 && rv1 != -1);
	// the service will sleep for 10 seconds so set the timeout to be less that 10
	int rv2 = tx_set_transaction_timeout(6);
	BT_ASSERT(rv2 == TX_OK);
	int rv4 = tx_begin();
	BT_ASSERT(rv4 == TX_OK);
	int rv3 = ::tpcall((char*) "tpcall_x_octet", (char *) sendbuf, sendlen, (char **) &rcvbuf, &rcvlen, (long) 0);
	btlogger((char*) "TxLog: test_timeout_with_tx tpcall=%d tperrno=%d", rv3, tperrno);
	BT_ASSERT(rv3 == -1);
	BT_ASSERT(tperrno == TPETIME);
	// the transaction should have been marked as rollback only
	char* commitS = (char*) malloc(110);
	int commit = tx_commit();
	sprintf(commitS, "%d", commit);
	BT_ASSERT_MESSAGE(commitS, commit == TX_ROLLBACK || commit == TX_FAIL); // BLACKTIE-323, as the transaction is gc'd on the server we may get an OBJECT_NOT_EXIST
	free(commitS);
}

void TestTxTPCall::test_tpcall_without_tx() {
	btlogger((char*) "TestTxTPCall: test_tpcall_without_tx");
	int rc = tpadvertise((char*) "tpcall_x_octet", test_tx_tpcall_x_octet_service_without_tx);
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(rc != -1);

	int id = ::tpcall((char*) "tpcall_x_octet", (char *) sendbuf, sendlen, (char **) &rcvbuf, &rcvlen, (long) 0);
	BT_ASSERT(id != -1);
	BT_ASSERT_MESSAGE(rcvbuf, strcmp(rcvbuf, "inTx=false") == 0);
	// make sure there is no active transaction
	BT_ASSERT(tx_commit() != TX_OK);
	btlogger_debug((char*) "TxLog: test_tpcall_without_tx: passed");
}

void TestTxTPCall::test_tpcall_with_tx() {
	btlogger((char*) "TestTxTPCall: test_tpcall_with_tx");
	int rc = tpadvertise((char*) "tpcall_x_octet", test_tx_tpcall_x_octet_service_with_tx);
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(rc != -1);

	// start a transaction
	btlogger_debug((char*) "TxLog: test_tpcall_with_tx: tx_open");
	BT_ASSERT(tx_begin() == TX_OK);
	btlogger_debug((char*) "TxLog: test_tpcall_with_tx: tpcall");
	(void) ::tpcall((char*) "tpcall_x_octet", (char *) sendbuf, sendlen, (char **) &rcvbuf, &rcvlen, (long) 0);
	btlogger_debug((char*) "TxLog: test_tpcall_with_tx: tx_commit");
	// make sure there is still an active transaction - ie starting a new one should fail
/*	BT_ASSERT(tx_begin() != TX_OK);*/
	BT_ASSERT(tx_commit() == TX_OK);
	BT_ASSERT_MESSAGE(rcvbuf, strcmp(rcvbuf, "inTx=true") == 0);
}

void TestTxTPCall::test_tpcancel_with_tx() {
	btlogger((char*) "TestTxTPCall: test_tpcancel_with_tx");
	int rc = tpadvertise((char*) "tpcall_x_octet", test_tx_tpcall_x_octet_service_with_tx);
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(rc != -1);

	// start a transaction
	btlogger_debug((char*) "TxLog: test_tpcancel_with_tx: tx_open");
	BT_ASSERT(tx_begin() == TX_OK);
	btlogger_debug((char*) "TxLog: test_tpcancel_with_tx: tpcall");
	int cd = ::tpacall((char*) "tpcall_x_octet", (char *) sendbuf, sendlen, (long) 0);
	BT_ASSERT(cd != -1);
	BT_ASSERT(tperrno == 0);
	// cancel should fail with TPETRAN since the outstanding call is transactional
	btlogger_debug((char*) "TxLog: test_tpcancel_with_tx: tpcancel %d", cd);
	int cancelled = ::tpcancel(cd);
	BT_ASSERT(cancelled == -1);
	BT_ASSERT(tperrno == TPETRAN);
	// a tpgetrply should succeed since the tpcancel request will have failed
	int res = ::tpgetrply(&cd, (char **) &rcvbuf, &rcvlen, 0);
	BT_ASSERT(res != -1);
	BT_ASSERT(tperrno == 0);
	btlogger_debug((char*) "TxLog: test_tpcancel_with_tx: tx_commit");
	// commit should succeed since the failed tpcancel does not affect the callers tx
	BT_ASSERT(tx_commit() == TX_OK);
	BT_ASSERT_MESSAGE(rcvbuf, strcmp(rcvbuf, "inTx=true") == 0);
}

CPPUNIT_TEST_SUITE_REGISTRATION( TestTxTPCall);
