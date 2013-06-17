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

#include "TestTPCancel.h"

#if defined(__cplusplus)
extern "C" {
#endif
extern void testtpcancel_service(TPSVCINFO *svcinfo);
#if defined(__cplusplus)
}
#endif

void TestTPCancel::setUp() {
	btlogger((char*) "TestTPCancel::setUp");
	sendbuf = NULL;
	rcvbuf = NULL;

	// Setup server
	BaseServerTest::setUp();

	// Do local work
	sendlen = strlen("cancel") + 1;
	BT_ASSERT((sendbuf = (char *) tpalloc((char*) "X_OCTET", NULL, sendlen)) != NULL);
	BT_ASSERT((rcvbuf = (char *) tpalloc((char*) "X_OCTET", NULL, sendlen)) != NULL);
	strcpy(sendbuf, "cancel");
	BT_ASSERT(tperrno == 0);
	int toCheck = tpadvertise((char*) "TestTPCancel", testtpcancel_service);
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(toCheck != -1);
}

void TestTPCancel::tearDown() {
	btlogger((char*) "TestTPCancel::tearDown");
	// Do local work
	::tpfree(sendbuf);
	::tpfree(rcvbuf);

	// Clean up server
	BaseServerTest::tearDown();
}

void TestTPCancel::test_tpcancel() {
	btlogger((char*) "test_tpcancel");
	int cd = ::tpacall((char*) "TestTPCancel", (char *) sendbuf, sendlen, 0);
	char* tperrnoS = (char*) malloc(110);
	sprintf(tperrnoS, "%d", tperrno);
	BT_ASSERT_MESSAGE(tperrnoS, tperrno == 0);
	free(tperrnoS);
	BT_ASSERT(cd != -1);

	// CANCEL THE REQUEST
	int cancelled = ::tpcancel(cd);
	BT_ASSERT(cancelled != -1);
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(tperrno != TPEBADDESC);
	BT_ASSERT(tperrno != TPETRAN);
	BT_ASSERT(tperrno != TPEPROTO);
	BT_ASSERT(tperrno != TPESYSTEM);
	BT_ASSERT(tperrno != TPEOS);

	// FAIL TO RETRIEVE THE RESPONSE
	int valToTest = ::tpgetrply(&cd, (char **) &rcvbuf, &rcvlen, 0);
	BT_ASSERT(tperrno == TPEBADDESC);
	BT_ASSERT(valToTest == -1);
}

void TestTPCancel::test_tpcancel_noreply() {
	btlogger((char*) "test_tpcancel_noreply");
	int cd = ::tpacall((char*) "TestTPCancel", (char *) sendbuf, sendlen, TPNOREPLY);
	char* tperrnoS = (char*) malloc(110);
	sprintf(tperrnoS, "%d", tperrno);
	BT_ASSERT_MESSAGE(tperrnoS, tperrno == 0);
	free(tperrnoS);
	BT_ASSERT(cd != -1);

	// CANCEL THE REQUEST
	int cancelled = ::tpcancel(cd);
	BT_ASSERT(cancelled == -1);
	BT_ASSERT(tperrno != 0);
	BT_ASSERT(tperrno == TPEBADDESC);
}

// 8.5
void TestTPCancel::test_tpcancel_baddesc() {
	btlogger((char*) "test_tpcancel_baddesc");
	// CANCEL THE REQUEST
	int cancelled = ::tpcancel(2);
	BT_ASSERT(cancelled == -1);
	BT_ASSERT(tperrno == TPEBADDESC);
}

void testtpcancel_service(TPSVCINFO *svcinfo) {
	btlogger((char*) "testtpcancel_service");
	if (!(svcinfo->flags && TPNOREPLY)) {
		int len = 21;
		char *toReturn = ::tpalloc((char*) "X_OCTET", NULL, len);
		strcpy(toReturn, "testtpcancel_service");
		tpreturn(TPSUCCESS, 0, toReturn, len, 0);
	}
}

CPPUNIT_TEST_SUITE_REGISTRATION( TestTPCancel);
