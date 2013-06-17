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

#include "TestTPUnadvertise.h"

#if defined(__cplusplus)
extern "C" {
#endif
extern void testtpunadvertise_service(TPSVCINFO *svcinfo);
#if defined(__cplusplus)
}
#endif

void TestTPUnadvertise::setUp() {
	btlogger((char*) "TestTPUnadvertise::setUp");
	sendbuf = NULL;
	rcvbuf = NULL;

	// Setup server
	BaseServerTest::setUp();
	int toCheck = tpadvertise((char*) "TestTPUnadverti",
			testtpunadvertise_service);
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(toCheck != -1);

	// Do local work
	sendlen = strlen("TestTPUnadvertise") + 1;
	rcvlen = sendlen;
	sendbuf = (char *) tpalloc((char*) "X_OCTET", NULL, sendlen);
	rcvbuf = (char *) tpalloc((char*) "X_OCTET", NULL, rcvlen);
	memset(rcvbuf, '\0', rcvlen);
	(void) strcpy(sendbuf, "TestTPUnadvertise");
}

void TestTPUnadvertise::tearDown() {
	btlogger((char*) "TestTPUnadvertise::tearDown");
	// Do local work
	::tpfree( sendbuf);
	::tpfree( rcvbuf);

	tpunadvertise((char*) "TestTPUnadverti");

	// Clean up server
	BaseServerTest::tearDown();
}

void TestTPUnadvertise::test_tpunadvertise() {
	btlogger((char*) "test_tpunadvertise");
	int id = ::tpunadvertise((char*) "TestTPUnadverti");
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(tperrno != TPEINVAL);
	BT_ASSERT(tperrno != TPENOENT);
	BT_ASSERT(tperrno != TPEPROTO);
	BT_ASSERT(tperrno != TPESYSTEM);
	BT_ASSERT(tperrno != TPEOS);
	BT_ASSERT(id != -1);

	id = ::tpcall((char*) "TestTPUnadverti", (char *) sendbuf, sendlen,
			(char **) &rcvbuf, &rcvlen, (long) 0);
	char* tperrnoS = (char*) malloc(110);
	sprintf(tperrnoS, "%d", tperrno);
	BT_ASSERT_MESSAGE(tperrnoS, tperrno == TPENOENT);
	free(tperrnoS);
	BT_ASSERT(id == -1);
	BT_ASSERT(strcmp(rcvbuf, "testtpunadvertise_service") != 0);
}

void TestTPUnadvertise::test_tpunadvertise_twice() {
	btlogger((char*) "test_tpunadvertise_twice");
	int id = ::tpunadvertise((char*) "TestTPUnadverti");
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(tperrno != TPEINVAL);
	BT_ASSERT(tperrno != TPENOENT);
	BT_ASSERT(tperrno != TPEPROTO);
	BT_ASSERT(tperrno != TPESYSTEM);
	BT_ASSERT(tperrno != TPEOS);
	BT_ASSERT(id != -1);

	id = ::tpunadvertise((char*) "TestTPUnadverti");
	BT_ASSERT(tperrno == TPENOENT);
	BT_ASSERT(id == -1);
}

void TestTPUnadvertise::test_tpunadvertise_null() {
	btlogger((char*) "test_tpunadvertise_null");
	int id = ::tpunadvertise(NULL);
	BT_ASSERT(tperrno == TPEINVAL);
	BT_ASSERT(id == -1);
}

void TestTPUnadvertise::test_tpunadvertise_empty() {
	btlogger((char*) "test_tpunadvertise_empty");
	int id = ::tpunadvertise((char*) "");
	BT_ASSERT(tperrno == TPEINVAL);
	BT_ASSERT(id == -1);
}

// 8.4
void TestTPUnadvertise::test_tpunadvertise_not_advertised() {
	btlogger((char*) "test_tpunadvertise_not_advertised");
	int id = ::tpunadvertise((char*) "NONE");
	BT_ASSERT(tperrno == TPENOENT);
	BT_ASSERT(id == -1);
}

void testtpunadvertise_service(TPSVCINFO *svcinfo) {
	btlogger((char*) "testtpunadvertise_service");
	char * toReturn = new char[26];
	strcpy(toReturn, "testtpunadvertise_service");
	// Changed length from 0L to svcinfo->len
	tpreturn(TPSUCCESS, 0, toReturn, 25, 0);
	delete toReturn;
}

CPPUNIT_TEST_SUITE_REGISTRATION( TestTPUnadvertise);

