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
#include "TestTPACall.h"
#include "BaseServerTest.h"

#include "xatmi.h"
#include "malloc.h"

#if defined(__cplusplus)
extern "C" {
#endif
extern void testtpacall_service(TPSVCINFO *svcinfo);
#if defined(__cplusplus)
}
#endif

void TestTPACall::setUp() {
	btlogger((char*) "TestTPACall::setUp");
	sendbuf = NULL;
	rcvbuf = NULL;

	// Set up server
	BaseServerTest::setUp();

	sendlen = strlen("test_tpacall") + 1;
	BT_ASSERT((sendbuf = (char *) tpalloc((char*) "X_OCTET", NULL, sendlen))
			!= NULL);
	strcpy(sendbuf, "test_tpacall");

	rcvlen = 22;
	BT_ASSERT((rcvbuf = (char *) tpalloc((char*) "X_OCTET", NULL, rcvlen))
			!= NULL);

	BT_ASSERT(tperrno == 0);

	// Set up local
	int toCheck = tpadvertise((char*) "TestTPACall", testtpacall_service);
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(toCheck != -1);

	cd = -1;
}

void TestTPACall::tearDown() {
	btlogger((char*) "TestTPACall::tearDown");

	if (cd != -1) {
		int cancelled = ::tpcancel(cd);
		BT_ASSERT(cancelled != -1);
		BT_ASSERT(tperrno == 0);
	}

	// Clean up local
	if (sendbuf) {
		::tpfree( sendbuf);
	}
	if (rcvbuf) {
		::tpfree( rcvbuf);
	}
	int toCheck = tpunadvertise((char*) "TestTPACall");
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(toCheck != -1);

	// Clean up server
	BaseServerTest::tearDown();
}

void TestTPACall::test_tpacall() {
	btlogger((char*) "test_tpacall");

	int cd = ::tpacall((char*) "TestTPACall", (char *) sendbuf, sendlen,
			TPNOREPLY);

	char* tperrnoS = (char*) malloc(110);
	sprintf(tperrnoS, "%d", tperrno);
	BT_ASSERT_MESSAGE(tperrnoS, tperrno == 0);

	char* cdS = (char*) malloc(110);
	sprintf(cdS, "%d", cd);
	BT_ASSERT_MESSAGE(cdS, cd == 0);
	free(cdS);

	// Make sure that there isn't a reply waiting
	int toTest = ::tpgetrply(&cd, (char **) &rcvbuf, &rcvlen, 0);
	sprintf(tperrnoS, "%d", tperrno);
	BT_ASSERT_MESSAGE(tperrnoS, tperrno == TPEBADDESC);
	free(tperrnoS);
	BT_ASSERT(toTest == -1);
}

void TestTPACall::test_tpconnect_to_non_TPCONV_fails() {
	btlogger((char*) "test_tpconnect_to_non_TPCONV_fails");
	int cd = ::tpconnect((char*) "TestTPACall", sendbuf, sendlen, TPRECVONLY);
	char* tperrnoS = (char*) malloc(110);
	sprintf(tperrnoS, "%d", tperrno);
	BT_ASSERT_MESSAGE(tperrnoS, tperrno == TPENOENT);
	free(tperrnoS);

	char* cdS = (char*) malloc(110);
	sprintf(cdS, "%d", cd);
	BT_ASSERT_MESSAGE(cdS, cd == -1);
	free(cdS);
}

// 9.1.1
void TestTPACall::test_tpacall_x_octet() {
	btlogger((char*) "test_tpacall_x_octet");

	int cd = tpacall((char*) "GREETSVC", sendbuf, 25, TPNOREPLY);
	BT_ASSERT(tperrno == TPENOENT);
	char* cdS = (char*) malloc(110);
	sprintf(cdS, "%d", cd);
	BT_ASSERT_MESSAGE(cdS, cd == -1);
	free(cdS);
}

void TestTPACall::test_tpacall_tprecv() {
	btlogger((char*) "test_tpacall_tprecv");

	cd = ::tpacall((char*) "TestTPACall", (char *) sendbuf, sendlen, 0);
	char* tperrnoS = (char*) malloc(110);
	sprintf(tperrnoS, "%d", tperrno);
	BT_ASSERT_MESSAGE(tperrnoS, tperrno == 0);

	char* cdS = (char*) malloc(110);
	sprintf(cdS, "%d", cd);
	BT_ASSERT_MESSAGE(cdS, cd != -1);
	free(cdS);

	long revent = 0;
	(void) ::tprecv(cd, &rcvbuf, &rcvlen, 0, &revent);
	sprintf(tperrnoS, "%d", tperrno);
	BT_ASSERT_MESSAGE(tperrnoS, tperrno == TPEBADDESC);

	// Drain the response
	(void) ::tpgetrply(&cd, (char **) &rcvbuf, &rcvlen, 0);
	sprintf(tperrnoS, "%d", tperrno);
	BT_ASSERT_MESSAGE(tperrnoS, tperrno == 0);
	BT_ASSERT_MESSAGE(rcvbuf, strcmp(rcvbuf, "testtpacall_service") == 0);

	free(tperrnoS);
	cd = -1;
}

void testtpacall_service(TPSVCINFO *svcinfo) {
	btlogger((char*) "testtpacall_service");
	int len = 20;
	char *toReturn = ::tpalloc((char*) "X_OCTET", NULL, len);
	strcpy(toReturn, "testtpacall_service");
	tpreturn(TPSUCCESS, 0, toReturn, len, 0);
}

CPPUNIT_TEST_SUITE_REGISTRATION( TestTPACall);
