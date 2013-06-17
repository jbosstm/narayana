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

#include "TestTPConnect.h"

#include "malloc.h"

#if defined(__cplusplus)
extern "C" {
#endif
extern void testtpconnect_service(TPSVCINFO *svcinfo);
#if defined(__cplusplus)
}
#endif

void TestTPConnect::setUp() {
	btlogger((char*) "TestTPConnect::setUp");
	sendbuf = NULL;
	rcvbuf = NULL;

	// Setup server
	BaseServerTest::setUp();

	// Do local work
	cd = -1;
	cd2 = -1;
	int toCheck = tpadvertise((char*) "TestTPConnect", testtpconnect_service);
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(toCheck != -1);

	sendlen = strlen("connect") + 1;
	rcvlen = 0;
	BT_ASSERT((sendbuf = (char *) tpalloc((char*) "X_OCTET", NULL, sendlen))
			!= NULL);
	BT_ASSERT((rcvbuf = (char *) tpalloc((char*) "X_OCTET", NULL, sendlen))
			!= NULL);
	strcpy(sendbuf, "connect");
	BT_ASSERT(tperrno == 0);
}

void TestTPConnect::tearDown() {
	btlogger((char*) "TestTPConnect::tearDown");

	// Do local work
	if (cd != -1) {
		::tpdiscon( cd);
	}

	if (cd2 != -1) {
		::tpdiscon( cd2);
	}
	::tpfree( sendbuf);
	::tpfree( rcvbuf);
	sendbuf = NULL;
	rcvbuf = NULL;
	int toCheck = tpunadvertise((char*) "TestTPConnect");
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(toCheck != -1);

	// Clean up server
	BaseServerTest::tearDown();
}

void TestTPConnect::test_tpconnect() {
	btlogger((char*) "test_tpconnect");
	cd = ::tpconnect((char*) "TestTPConnect", sendbuf, sendlen, TPRECVONLY);
	char* tperrnoS = (char*) malloc(110);
	sprintf(tperrnoS, "%d", tperrno);
	BT_ASSERT_MESSAGE(tperrnoS, tperrno == 0);
	free(tperrnoS);

	char* cdS = (char*) malloc(110);
	sprintf(cdS, "%d", cd);
	BT_ASSERT_MESSAGE(cdS, cd != -1);
	free(cdS);
}

void TestTPConnect::test_tpacall_to_TPCONV_fails() {
	btlogger((char*) "test_tpacall_to_TPCONV_fails");

	int cd = ::tpacall((char*) "TestTPConnect", (char *) sendbuf, sendlen, 0);
	btlogger((char*) "test_tpacall_to_TPCONV_fails %d %d", tperrno, cd);
	char* tperrnoS = (char*) malloc(110);
	sprintf(tperrnoS, "%d", tperrno);
	BT_ASSERT_MESSAGE(tperrnoS, tperrno == TPENOENT);
	free(tperrnoS);
	
	char* cdS = (char*) malloc(110);
	sprintf(cdS, "%d", cd);
	BT_ASSERT_MESSAGE(cdS, cd == -1);
	free(cdS);
}

void TestTPConnect::test_tpconnect_double_connect() {
	btlogger((char*) "test_tpconnect_double_connect");
	cd = ::tpconnect((char*) "TestTPConnect", sendbuf, sendlen, TPRECVONLY);
	cd2 = ::tpconnect((char*) "TestTPConnect", sendbuf, sendlen, TPRECVONLY);
	char* tperrnoS = (char*) malloc(110);
	sprintf(tperrnoS, "%d", tperrno);
	BT_ASSERT_MESSAGE(tperrnoS, tperrno == 0);
	free(tperrnoS);

	char* cdS = (char*) malloc(110);
	sprintf(cdS, "%d", cd);
	BT_ASSERT_MESSAGE(cdS, cd != -1);
	free(cdS);
	BT_ASSERT(cd2 != -1);
	BT_ASSERT(cd != cd2);
}

void TestTPConnect::test_tpconnect_nodata() {
	btlogger((char*) "test_tpconnect_nodata");
	cd = ::tpconnect((char*) "TestTPConnect", NULL, 0, TPRECVONLY);
	char* tperrnoS = (char*) malloc(110);
	sprintf(tperrnoS, "%d", tperrno);
	BT_ASSERT_MESSAGE(tperrnoS, tperrno == 0);
	free(tperrnoS);

	char* cdS = (char*) malloc(110);
	sprintf(cdS, "%d", cd);
	BT_ASSERT_MESSAGE(cdS, cd != -1);
	free(cdS);
}

void TestTPConnect::test_tpconnect_tpgetrply() {
	btlogger((char*) "test_tpconnect_tpgetrply");
	cd = ::tpconnect((char*) "TestTPConnect", sendbuf, sendlen, TPRECVONLY);
	char* tperrnoS = (char*) malloc(110);
	sprintf(tperrnoS, "%d", tperrno);
	BT_ASSERT_MESSAGE(tperrnoS, tperrno == 0);

	char* cdS = (char*) malloc(110);
	sprintf(cdS, "%d", cd);
	BT_ASSERT_MESSAGE(cdS, cd != -1);
	free(cdS);

	(void) ::tpgetrply(&cd, (char **) &rcvbuf, &rcvlen, 0);
	sprintf(tperrnoS, "%d", tperrno);
	BT_ASSERT_MESSAGE(tperrnoS, tperrno == TPEBADDESC);

	// Clean the pending message
	long revent = 0;
	(void) ::tprecv(cd, &rcvbuf, &rcvlen, 0, &revent);
	sprintf(tperrnoS, "%d", tperrno);
	BT_ASSERT_MESSAGE(tperrnoS, tperrno == TPEEVENT);
	BT_ASSERT(revent & TPEV_SVCSUCC);

	free(tperrnoS);
}

void testtpconnect_service(TPSVCINFO *svcinfo) {
	btlogger((char*) "testtpconnect_service");
	tpreturn(TPSUCCESS, 0, NULL, 0, 0);
}

CPPUNIT_TEST_SUITE_REGISTRATION( TestTPConnect);
