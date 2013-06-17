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
#include "TestExternManageDestination.h"
#include "BaseServerTest.h"

#include "btserver.h"

#include "xatmi.h"
#include "tx.h"
#include "btqueue.h"
#include "malloc.h"

#include <stdio.h>
#include <stdlib.h>


//static SynchronizableObject* lock = new SynchronizableObject();

void TestExternManageDestination::setUp() {
	btlogger((char*) "TestExternManageDestination::setUp");

	// Set up server
	BaseServerTest::setUp();
}

void TestExternManageDestination::tearDown() {
	btlogger((char*) "TestExternManageDestination::tearDown");

	// Clean up server
	BaseServerTest::tearDown();
}

static void send_one(int id, int pri, const char *type, const char *subtype) {
	msg_opts_t mopts = {0, 0L};
	char msg[16];
	char* buf;
	long len;
	int cd;

	mopts.priority = pri;
	sprintf(msg, (char*) "%d", id);
	len = strlen(msg) + 1;

	buf = tpalloc((char *) type, (char *) subtype, len);
	BT_ASSERT(buf != NULL);

	(void) strcpy(buf, msg);
	cd = btenqueue((char*) "TestOne", &mopts, buf, len, 0);

	BT_ASSERT(tperrno == 0 && cd == 0);

	btlogger("Sent %d %d", id, pri);

	tpfree(buf);
}

static void recv_one(msg_opts_t *mopts, long len, long flags, int expect, int expected_tperrno,
	const char *type, const char *subtype) {
	int toCheck;
	char* data = (char*) tpalloc((char *) "X_OCTET", (char *) NULL, len);

	BT_ASSERT(data != NULL);
	toCheck = btdequeue((char*) "TestOne", mopts, &data, &len, 0L);

	if (tperrno == 0) {
		char tptype[9];
		char tpsubtype[17];

		BT_ASSERT(toCheck != -1);

		btlogger((char*) "recv_one: tperrno=%d expected_tperrno=%d toCheck=%d data=%d",
			tperrno, expected_tperrno, toCheck, atoi(data));
		BT_ASSERT(tperrno == expected_tperrno);

		if (expect >= 0)
			BT_ASSERT(atoi(data) == expect);

		BT_ASSERT(::tptypes(data, tptype, tpsubtype)  != -1);

		// make sure buffers are null terminated before passing to printf
		*(tptype + 8) = *(tpsubtype + 16) = 0;
		btlogger((char*) "recv_one: type=%s subtype=%s", tptype, tpsubtype);

		if (type != NULL)
			BT_ASSERT(strncmp(type, tptype, 8) == 0);
		if (subtype != NULL)
			BT_ASSERT(strncmp(subtype, tpsubtype, 16) == 0);
		tpfree(data);
	} else {
		btlogger((char*) "recv_one: tperrno=%d expected_tperrno=%d toCheck=%d",
			tperrno, expected_tperrno, toCheck);
		BT_ASSERT(tperrno == expected_tperrno);
	}
}

void TestExternManageDestination::test_stored_messages() {
	int i;
	msg_opts_t mopts = {0, 0L};

	btlogger((char*) "test_stored_messages");
	for (i = 10; i < 20; i++)
		send_one(i, 0, "X_OCTET", NULL);

	// retrieve the messages in two goes:
	for (i = 10; i < 20; i++) {
		btlogger((char*) "test_stored_messages: retrieving 5");

		char* data = (char*) tpalloc((char*) "X_OCTET", NULL, 2);
		long len = 2;
		long flags = 0;
		int toCheck = btdequeue((char*) "TestOne", &mopts, &data, &len, flags);
		BT_ASSERT(tperrno == 0 && toCheck != -1);

		int id = atoi(data);
		btlogger((char*) "qservice expected: %d received: %d", i, id);
		

		if (i % 5 == 0) {
			btlogger((char*) "Restart server");
			serverdone();
			startServer();
		}
	}

	btlogger((char*) "test_stored_message passed");
}

void TestExternManageDestination::test_stored_message_priority() {
	btlogger((char*) "test_stored_message_priority");
	// send messages with out of order ids - the qservice should receive them in order
	int prefix = 20;
	msg_opts_t mopts = {0, 10L};

	send_one(prefix + 8, 1, "X_OCTET", NULL);
	send_one(prefix + 6, 3, "X_OCTET", NULL);
	send_one(prefix + 4, 5, "X_OCTET", NULL);
	send_one(prefix + 2, 7, "X_OCTET", NULL);
	send_one(prefix + 0, 9, "X_OCTET", NULL);
	send_one(prefix + 9, 0, "X_OCTET", NULL);
	send_one(prefix + 7, 2, "X_OCTET", NULL);
	send_one(prefix + 5, 4, "X_OCTET", NULL);
	send_one(prefix + 3, 6, "X_OCTET", NULL);
	send_one(prefix + 1, 8, "X_OCTET", NULL);

	int msgId = prefix;

	// retrieve the messages in two goes:

	bool orderOK = true;
	for (int msgCnt = 0; msgCnt < 10; msgCnt++) {
		char* data = (char*) tpalloc((char*) "X_OCTET", NULL, 2);
		long len = 2;
		long flags = 0;
		int toCheck = btdequeue((char*) "TestOne", &mopts, &data, &len, flags);
		BT_ASSERT(tperrno == 0 && toCheck != -1);
		
		int id = atoi(data);

		btlogger((char*) "qservice iteration: %d expected: %d received: %d", msgCnt, msgId, id);
		orderOK = orderOK & (msgId == id);
		
		msgId += 1;
	}
	BT_ASSERT(orderOK);

	serverdone();

	btlogger((char*) "test_stored_message_priority passed");
}

void TestExternManageDestination::test_btenqueue_with_txn_abort() {
	int i;
	msg_opts_t mopts = {0, 500L};

	btlogger((char*) "test_btenqueue_with_txn_abort");
	BT_ASSERT(tx_open() == TX_OK);
	BT_ASSERT(tx_begin() == TX_OK);

	// enqueue messages within a transaction but then abort it
	for (i = 40; i < 45; i++)
		send_one(i, 0, "X_OCTET", NULL);

	BT_ASSERT(tx_rollback() == TX_OK);

	// since the txn aborted the queue will be empty and btdequeue should fail with TPETIME
	btlogger((char*) "testing that btdequeue returns TPETIME");
	recv_one(&mopts, 2L, 0L, i, TPETIME, "X_OCTET", NULL);

	BT_ASSERT(tx_close() == TX_OK);

	btlogger((char*) "test_btenqueue_with_txn_abort passed");
}

void TestExternManageDestination::test_btenqueue_with_txn_commit() {
	int i;
	msg_opts_t mopts = {0, 0L};

	btlogger((char*) "test_btenqueue_with_txn_commit");
	BT_ASSERT(tx_open() == TX_OK);
	BT_ASSERT(tx_begin() == TX_OK);

	// enqueue messages within a transaction and then commit it
	for (i = 45; i < 50; i++)
		send_one(i, 0, "X_OCTET", NULL);

	BT_ASSERT(tx_commit() == TX_OK);

	// since the txn commited btdequeue should retrieve them all
	for (i = 45; i < 50; i++)
		recv_one(&mopts, 2L, 0L, i, 0, "X_OCTET", NULL);

	BT_ASSERT(tx_close() == TX_OK);

	btlogger((char*) "test_btenqueue_with_txn_commit passed");
}

void TestExternManageDestination::test_btdequeue_with_txn_abort() {
	msg_opts_t mopts = {0, 0L};
	int i;

	btlogger((char*) "test_btdequeue_with_txn_abort");
	BT_ASSERT(tx_open() == TX_OK);

	// enqueue messages
	for (i = 50; i < 55; i++)
		send_one(i, 0, "X_OCTET", NULL);

	// dequeue messages within a transaction and then abort it
	BT_ASSERT(tx_begin() == TX_OK);
	for (i = 50; i < 55; i++)
		recv_one(&mopts, 2L, 0L, i, 0, "X_OCTET", NULL);

	BT_ASSERT(tx_rollback() == TX_OK);

	// since the txn was abort the queue will still contains the messages
	for (i = 50; i < 55; i++)
		recv_one(&mopts, 2L, 0L, i, 0, "X_OCTET", NULL);

	BT_ASSERT(tx_close() == TX_OK);

	btlogger((char*) "test_btdequeue_with_txn_abort passed");
}

void TestExternManageDestination::test_btdequeue_with_txn_commit() {
	msg_opts_t mopts = {0, 0L};
	int i;

	btlogger((char*) "test_btdequeue_with_txn_commit");
	BT_ASSERT(tx_open() == TX_OK);

	// enqueue messages
	for (i = 55; i < 60; i++)
		send_one(i, 0, "X_OCTET", NULL);

	// and dequeue them within a transaction and then commit it
	BT_ASSERT(tx_begin() == TX_OK);
	for (i = 55; i < 60; i++)
		recv_one(&mopts, 2L, 0L, i, 0, "X_OCTET", NULL);

	BT_ASSERT(tx_commit() == TX_OK);

	// test that all the messages were dequeued
	btlogger((char*) "testing that btdequeue returns TPETIME");
	mopts.ttl = 500L;
	recv_one(&mopts, 2L, 0L, i, TPETIME, "X_OCTET", NULL);

	BT_ASSERT(tx_close() == TX_OK);

	btlogger((char*) "test_btdequeue_with_txn_commit passed");
}

void TestExternManageDestination::test_btenqueue_with_tptypes() {
	int id = 0;
	msg_opts_t mopts = {0, 0L};

	btlogger((char*) "test_btenqueue_with_tptypes");

	send_one(id, 0, "X_OCTET", NULL);
	recv_one(&mopts, 2L, 0L, id, 0, "X_OCTET", NULL);

	send_one(id, 0, "X_COMMON", "deposit");
	recv_one(&mopts, 2L, 0L, id, 0, "X_COMMON", "deposit");

	send_one(id, 0, "X_C_TYPE", "acct_info");
	recv_one(&mopts, 2L, 0L, id, 0, "X_C_TYPE", "acct_info");

	btlogger((char*) "test_btenqueue_with_txn_commit passed");
}
