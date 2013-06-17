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

#include "malloc.h"

#include "TestTPRecv.h"

#if defined(__cplusplus)
extern "C" {
#endif
extern void testtprecv_service(TPSVCINFO *svcinfo);
#if defined(__cplusplus)
}
#endif

void TestTPRecv::setUp() {
	btlogger((char*) "TestTPRecv::setUp");
	sendbuf = NULL;
	rcvbuf = NULL;

	// Setup server
	BaseServerTest::setUp();

	// Do local work
	cd = -1;
	sendlen = strlen("recv") + 1;
	rcvlen = sendlen;
	BT_ASSERT((sendbuf
			= (char *) tpalloc((char*) "X_OCTET", NULL, sendlen)) != NULL);
	BT_ASSERT((rcvbuf = (char *) tpalloc((char*) "X_OCTET", NULL, rcvlen))
			!= NULL);
	strcpy(sendbuf, "recv");
	BT_ASSERT(tperrno == 0);
	int toCheck = tpadvertise((char*) "TestTPRecv", testtprecv_service);
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(toCheck != -1);
}

void TestTPRecv::tearDown() {
	btlogger((char*) "TestTPRecv::tearDown");
	// Do local work
	if (cd != -1) {
		::tpdiscon(cd);
	}
	::tpfree(sendbuf);
	::tpfree(rcvbuf);
	int toCheck = tpunadvertise((char*) "TestTPRecv");
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(toCheck != -1);

	// Clean up server
	BaseServerTest::tearDown();
}

void TestTPRecv::test_tprecv_sendonly() {
	btlogger((char*) "test_tprecv_sendonly");
	cd = ::tpconnect((char*) "TestTPRecv", sendbuf, sendlen, TPSENDONLY);
	long revent = 0;
	int result = ::tprecv(cd, &rcvbuf, &rcvlen, 0, &revent);
	char* tperrnoS = (char*) malloc(110);
	sprintf(tperrnoS, "%d", tperrno);
	BT_ASSERT_MESSAGE(tperrnoS, tperrno == TPEPROTO);
	free(tperrnoS);

	BT_ASSERT(result == -1);
}

void testtprecv_service(TPSVCINFO *svcinfo) {
	btlogger((char*) "testtprecv_service");
}

CPPUNIT_TEST_SUITE_REGISTRATION( TestTPRecv);
