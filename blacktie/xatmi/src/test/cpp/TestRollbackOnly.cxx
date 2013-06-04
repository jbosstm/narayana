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

#include "ThreadLocalStorage.h"
#include "BaseServerTest.h"
#include "XATMITestSuite.h"

#include "xatmi.h"
#include "tx.h"

#include "malloc.h"

#include "TestRollbackOnly.h"

#if defined(__cplusplus)
extern "C" {
#endif
extern void test_tpcall_TPETIME_service(TPSVCINFO *svcinfo);
extern void test_tpcall_TPEOTYPE_service(TPSVCINFO *svcinfo);
extern void test_tpcall_TPESVCFAIL_service(TPSVCINFO *svcinfo);
extern void test_tprecv_TPEV_DISCONIMM_service(TPSVCINFO *svcinfo);
extern void test_tprecv_TPEV_SVCFAIL_service(TPSVCINFO *svcinfo);
extern void test_no_tpreturn_service(TPSVCINFO *svcinfo);
#if defined(__cplusplus)
}
#endif

void TestRollbackOnly::setUp() {
	btlogger((char*) "TestRollbackOnly::setUp");
	BaseServerTest::setUp();

	// previous tests may have left a txn on the thread
	destroySpecific(TSS_KEY);

	sendlen = strlen("TestRbkOnly") + 1;
	BT_ASSERT((sendbuf
			= (char *) tpalloc((char*) "X_OCTET", NULL, sendlen)) != NULL);
	(void) strcpy(sendbuf, "TestRbkOnly");
	rcvlen = 60;
	BT_ASSERT((rcvbuf = (char *) tpalloc((char*) "X_OCTET", NULL, rcvlen))
			!= NULL);
	BT_ASSERT(tperrno == 0);
}

void TestRollbackOnly::tearDown() {
	btlogger((char*) "TestRollbackOnly::tearDown");
	destroySpecific(TSS_KEY);
	BT_ASSERT(tx_close() == TX_OK);

	::tpfree( sendbuf);
	::tpfree( rcvbuf);

	// Clean up server
	BaseServerTest::tearDown();
}

void TestRollbackOnly::test_tpcall_TPETIME() {
	btlogger((char*) "test_tpcall_TPETIME");
	int rc = tpadvertise((char*) "TestRbkOnly", test_tpcall_TPETIME_service);
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(rc != -1);

	BT_ASSERT(tx_open() == TX_OK);
	// the TPETIME service sleeps for 10 so set the txn time to something smaller
	BT_ASSERT(tx_set_transaction_timeout(8l) == TX_OK);
	BT_ASSERT(tx_begin() == TX_OK);

	(void) ::tpcall((char*) "TestRbkOnly", (char *) sendbuf, sendlen,
			(char **) &rcvbuf, &rcvlen, (long) 0);
	BT_ASSERT(tperrno == TPETIME);

	TXINFO txinfo;
	int inTx = ::tx_info(&txinfo);
	btlogger((char*) "inTx=%d tx status=%ld", inTx, txinfo.transaction_state);
	BT_ASSERT(txinfo.transaction_state == TX_ROLLBACK_ONLY ||
		txinfo.transaction_state == TX_TIMEOUT_ROLLBACK_ONLY);
	char* commitS = (char*) malloc(110);
	int commit = tx_commit();
	sprintf(commitS, "%d", commit);
	BT_ASSERT_MESSAGE(commitS, commit == TX_ROLLBACK);
	free(commitS);
}

void TestRollbackOnly::test_tpcall_TPEOTYPE() {
	btlogger((char*) "test_tpcall_TPEOTYPE");
	int rc = tpadvertise((char*) "TestRbkOnly", test_tpcall_TPEOTYPE_service);
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(rc != -1);

	BT_ASSERT(tx_open() == TX_OK);
	BT_ASSERT(tx_begin() == TX_OK);

	(void) ::tpcall((char*) "TestRbkOnly", (char *) sendbuf, sendlen,
			(char **) &rcvbuf, &rcvlen, TPNOCHANGE);
	BT_ASSERT(tperrno == TPEOTYPE);

	TXINFO txinfo;
	int inTx = ::tx_info(&txinfo);
	btlogger((char*) "inTx=%d", inTx);
	BT_ASSERT(txinfo.transaction_state == TX_ROLLBACK_ONLY);
	BT_ASSERT(tx_commit() == TX_ROLLBACK);
}

void TestRollbackOnly::test_tpcall_TPESVCFAIL() {
	btlogger((char*) "test_tpcall_TPESVCFAIL");
	int rc = tpadvertise((char*) "TestRbkOnly", test_tpcall_TPESVCFAIL_service);
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(rc != -1);

	BT_ASSERT(tx_open() == TX_OK);
	BT_ASSERT(tx_begin() == TX_OK);

	(void) ::tpcall((char*) "TestRbkOnly", (char *) sendbuf, sendlen,
			(char **) &rcvbuf, &rcvlen, (long) 0);
	BT_ASSERT_MESSAGE(rcvbuf, strcmp(rcvbuf,
			"test_tpcall_TPESVCFAIL_service") == 0);
	BT_ASSERT(tperrno == TPESVCFAIL);

	TXINFO txinfo;
	int inTx = ::tx_info(&txinfo);
	btlogger((char*) "inTx=%d", inTx);
	BT_ASSERT(txinfo.transaction_state == TX_ROLLBACK_ONLY);
	BT_ASSERT(tx_commit() == TX_ROLLBACK);
}

void TestRollbackOnly::test_tprecv_TPEV_DISCONIMM() {
	btlogger((char*) "test_tprecv_TPEV_DISCONIMM");
	int rc = tpadvertise((char*) "TestRbkOnly2",
			test_tprecv_TPEV_DISCONIMM_service);
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(rc != -1);

	BT_ASSERT(tx_open() == TX_OK);
	BT_ASSERT(tx_begin() == TX_OK);

	int cd = ::tpconnect((char*) "TestRbkOnly2", (char *) sendbuf, sendlen,
			TPSENDONLY);
	::tpdiscon(cd);
	BT_ASSERT(tperrno == 0);

	TXINFO txinfo;
	int inTx = ::tx_info(&txinfo);
	btlogger((char*) "inTx=%d", inTx);
	BT_ASSERT(txinfo.transaction_state == TX_ROLLBACK_ONLY);
	BT_ASSERT(tx_commit() == TX_ROLLBACK);
}

void TestRollbackOnly::test_tprecv_TPEV_SVCFAIL() {
	btlogger((char*) "test_tprecv_TPEV_SVCFAIL");
	int rc = tpadvertise((char*) "TestRbkOnly2",
			test_tprecv_TPEV_SVCFAIL_service);
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(rc != -1);

	BT_ASSERT(tx_open() == TX_OK);
	BT_ASSERT(tx_begin() == TX_OK);

	int cd = ::tpconnect((char*) "TestRbkOnly2", (char *) sendbuf, sendlen,
			TPRECVONLY);
	long revent = 0;
	int status = ::tprecv(cd, (char **) &rcvbuf, &rcvlen, (long) 0, &revent);
	BT_ASSERT_MESSAGE(rcvbuf, strcmp(rcvbuf,
			"test_tprecv_TPEV_SVCFAIL_service") == 0);
	BT_ASSERT(status == -1);
	BT_ASSERT(revent == TPEV_SVCFAIL);
	BT_ASSERT(tperrno == TPEEVENT);

	TXINFO txinfo;
	int inTx = ::tx_info(&txinfo);
	btlogger((char*) "inTx=%d", inTx);
	BT_ASSERT(txinfo.transaction_state == TX_ROLLBACK_ONLY);
	BT_ASSERT(tx_commit() == TX_ROLLBACK);
}

void TestRollbackOnly::test_no_tpreturn() {
	btlogger((char*) "test_no_tpreturn");
	int rc = tpadvertise((char*) "TestRbkOnly", test_no_tpreturn_service);
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(rc != -1);

	BT_ASSERT(tx_open() == TX_OK);
	BT_ASSERT(tx_begin() == TX_OK);

	(void) ::tpcall((char*) "TestRbkOnly", (char *) sendbuf, sendlen,
			(char **) &rcvbuf, &rcvlen, (long) 0);
	BT_ASSERT(tperrno == TPESVCERR);

	TXINFO txinfo;
	int inTx = ::tx_info(&txinfo);
	btlogger((char*) "inTx=%d", inTx);
	BT_ASSERT(txinfo.transaction_state == TX_ROLLBACK_ONLY);
	BT_ASSERT(tx_commit() == TX_ROLLBACK);
}


CPPUNIT_TEST_SUITE_REGISTRATION( TestRollbackOnly);
