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

#include "Sleeper.h"
#include "xatmi.h"
#include "btlogger.h"
#include "TestTimeToLive.h"

#if defined(__cplusplus)
extern "C" {
#endif
int ttlCounter = 0;
void test_TTL_service(TPSVCINFO *svcinfo) {
    btlogger((char*) "test_TTL_service");
	char *toReturn = ::tpalloc((char*) "X_OCTET", NULL, 1);

	if (strncmp(svcinfo->data, "counter", 7) != 0) {
		::sleeper(60);
		ttlCounter++;
	}
	btlogger("Counter was %d", ttlCounter);
    if (ttlCounter == 1) {
        (void) strncpy(toReturn, "1", 1);
    } else if (ttlCounter == 2) {
        (void) strncpy(toReturn, "2", 1);
    } else {
        (void) strncpy(toReturn, "0", 1);
    }
	
	tpreturn(TPSUCCESS, 0, toReturn, 1, 0);
}
#if defined(__cplusplus)
}
#endif

void TestTimeToLive::setUp() {
	btlogger((char*) "TestTimeToLive::setUp");
	BaseServerTest::setUp();
	sendbuf = tpalloc((char*) "X_OCTET", NULL, 7);
	rcvbuf = tpalloc((char*) "X_OCTET", NULL, 1);
	rcvlen = 1;
}

void TestTimeToLive::tearDown() {
	btlogger((char*) "TestTimeToLive::tearDown");
	::tpfree( sendbuf);
	::tpfree( rcvbuf);
	BaseServerTest::tearDown();
}

void TestTimeToLive::testTTL() {
	int rc = tpadvertise((char*) "TTL", test_TTL_service);
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(rc != -1);

    int cd = -1;
	btlogger((char*)"send first message");
	(void) strncpy(sendbuf, "1234567", 7);
	cd = callTTL();
	BT_ASSERT(cd == -1);
	BT_ASSERT(tperrno == TPETIME);

	btlogger((char*)"send second message");
	cd = callTTL();
	BT_ASSERT(cd == -1);
	BT_ASSERT(tperrno == TPETIME);

	btlogger((char*)"send third message");
	(void) strncpy(sendbuf, "counter", 7);
	cd = callTTL();	
	//btlogger((char*)"TTL get message counter is %s", rcvbuf);
	BT_ASSERT(strncmp(rcvbuf, "1", 1) == 0);
}

int TestTimeToLive::callTTL() {
	int cd = ::tpcall((char*) "TTL", (char *) sendbuf, 7, (char**)&rcvbuf, &rcvlen, 0);
	return cd;
}

CPPUNIT_TEST_SUITE_REGISTRATION( TestTimeToLive );
