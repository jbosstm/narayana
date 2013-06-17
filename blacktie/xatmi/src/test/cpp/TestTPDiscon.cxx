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
#include "Sleeper.h"

#include "xatmi.h"

#include "malloc.h"

#include "TestTPDiscon.h"

#if defined(__cplusplus)
extern "C" {
#endif
extern void testtpdiscon_service(TPSVCINFO *svcinfo);
#if defined(__cplusplus)
}
#endif

void TestTPDiscon::setUp() {
	btlogger((char*) "TestTPDiscon::setUp");
	sendbuf = NULL;

	// Setup server
	BaseServerTest::setUp();

	// Do local work
	sendlen = strlen("discon") + 1;
	BT_ASSERT((sendbuf = (char *) tpalloc((char*) "X_OCTET", NULL, sendlen)) != NULL);
	strcpy(sendbuf, "discon");
	BT_ASSERT(tperrno == 0);

	int toCheck = tpadvertise((char*) "TestTPDiscon", testtpdiscon_service);
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(toCheck != -1);

	cd = ::tpconnect((char*) "TestTPDiscon", sendbuf, sendlen, TPSENDONLY);
	BT_ASSERT(cd != -1);
	char* tperrnoS = (char*) malloc(110);
	sprintf(tperrnoS, "%d", tperrno);
	BT_ASSERT_MESSAGE(tperrnoS, tperrno == 0);
	free(tperrnoS);
}

void TestTPDiscon::tearDown() {
	btlogger((char*) "TestTPDiscon::tearDown");
	// Do local work
	::tpfree(sendbuf);
	if (cd != -1) {
		::tpdiscon(cd);
	}
	int toCheck = tpunadvertise((char*) "TestTPDiscon");
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(toCheck != -1);

	// Clean up server
	BaseServerTest::tearDown();
}

void TestTPDiscon::test_tpdiscon() {
	btlogger((char*) "test_tpdiscon");
	::tpdiscon(cd);
	BT_ASSERT(tperrno == 0);
	cd = -1;
}

void TestTPDiscon::test_tpdiscon_baddescr() {
	btlogger((char*) "test_tpdiscon_baddescr");
	::tpdiscon(cd + 1);
	BT_ASSERT(tperrno == TPEBADDESC);
}

void TestTPDiscon::test_tpdiscon_negdescr() {
	btlogger((char*) "test_tpdiscon_negdescr");
	::tpdiscon(-1);
	BT_ASSERT(tperrno == TPEBADDESC);
}

void testtpdiscon_service(TPSVCINFO *svcinfo) {
	btlogger((char*) "testtpdiscon_service");
	::sleeper(2);
}

CPPUNIT_TEST_SUITE_REGISTRATION( TestTPDiscon);
