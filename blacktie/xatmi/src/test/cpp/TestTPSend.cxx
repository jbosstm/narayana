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

#include "BaseServerTest.h"

#include "xatmi.h"

#include "TestTPSend.h"

#if defined(__cplusplus)
extern "C" {
#endif
extern void testtpsend_service(TPSVCINFO *svcinfo);
extern void testtpsend_tpsendonly_service(TPSVCINFO *svcinfo);
#if defined(__cplusplus)
}
#endif

void TestTPSend::setUp() {
	btlogger((char*) "TestTPSend::setUp");
	sendbuf = NULL;
	rcvbuf = NULL;
	rcvlen = -1;

	// Setup server
	BaseServerTest::setUp();

	// Do local work
	cd = -1;

	sendlen = strlen("tpsend") + 1;
	BT_ASSERT((sendbuf = (char *) tpalloc((char*) "X_OCTET", NULL, sendlen))
			!= NULL);
	BT_ASSERT((rcvbuf = (char *) tpalloc((char*) "X_OCTET", NULL, sendlen))
			!= NULL);
	strcpy(sendbuf, "tpsend");
	BT_ASSERT(tperrno == 0);
}

void TestTPSend::tearDown() {
	btlogger((char*) "TestTPSend::tearDown");
	// Do local work
	if (cd != -1) {
		::tpdiscon( cd);
	}
	::tpfree( sendbuf);
	::tpfree( rcvbuf);
	int toCheck = tpunadvertise((char*) "TestTPSend");
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(toCheck != -1);

	// Clean up server
	BaseServerTest::tearDown();
}

void TestTPSend::test_tpsend_recvonly() {
	btlogger((char*) "test_tpsend_recvonly");

	int toCheck = tpadvertise((char*) "TestTPSend", testtpsend_service);
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(toCheck != -1);

	cd = ::tpconnect((char*) "TestTPSend", sendbuf, sendlen, TPRECVONLY);
	long event = 0;
	int result = ::tpsend(cd, sendbuf, sendlen, 0, &event);
	BT_ASSERT((event == TPEV_SVCERR) || (tperrno == TPEPROTO));
	BT_ASSERT(result == -1);
}

void TestTPSend::test_tpsend_tpsendonly() {
	btlogger((char*) "test_tpsend_tpsendonly");
	int toCheck = tpadvertise((char*) "TestTPSend",
			testtpsend_tpsendonly_service);
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(toCheck != -1);

	cd = ::tpconnect((char*) "TestTPSend", sendbuf, sendlen, TPRECVONLY);

	long revent = 0;
	int result = ::tprecv(cd, &rcvbuf, &rcvlen, 0, &revent);
	BT_ASSERT(revent & TPEV_SENDONLY);
	BT_ASSERT(tperrno == TPEEVENT);
	BT_ASSERT(result == -1);

	result = ::tprecv(cd, &rcvbuf, &rcvlen, 0, &revent);
	BT_ASSERT(tperrno == TPEPROTO);
	BT_ASSERT(result == -1);

	long event = 0;
	result = ::tpsend(cd, sendbuf, sendlen, 0, &event);
	BT_ASSERT(event == 0);
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(result != -1);
}

void testtpsend_service(TPSVCINFO *svcinfo) {
	btlogger((char*) "testtpsend_service");
}

void testtpsend_tpsendonly_service(TPSVCINFO *svcinfo) {
	btlogger((char*) "testtpsend_tpsendonly_service");

	long event = 0;
	int result = ::tpsend(svcinfo->cd, svcinfo->data, svcinfo->len, TPRECVONLY,
			&event);
	btlogger((char*) "result=%d", result);

	long revent = 0;
	long rcvlen = svcinfo->len;
	char* rcvbuf = (char *) tpalloc((char*) "X_OCTET", NULL, svcinfo->len);
	result = ::tprecv(svcinfo->cd, &rcvbuf, &rcvlen, 0, &revent);
	btlogger((char*) "result=%d", result);
}
CPPUNIT_TEST_SUITE_REGISTRATION( TestTPSend);

