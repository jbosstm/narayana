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

#include "TestTopic.h"

#if defined(__cplusplus)
extern "C" {
#endif
extern void test_topic_service(TPSVCINFO *svcinfo);
#if defined(__cplusplus)
}
#endif

void TestTopic::setUp() {
	btlogger((char*) "TestTopic::setUp");

	// Setup server
	BaseServerTest::setUp();
	int toCheck = tpadvertise((char*) "TestTopic", test_topic_service);
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(toCheck != -1);

	// Do local work
	sendlen = strlen("TestTopic") + 1;
	BT_ASSERT((sendbuf = (char *) tpalloc((char*) "X_OCTET", NULL, sendlen)) != NULL);
	(void) strcpy(sendbuf, "TestTopic");
	BT_ASSERT(tperrno == 0);
}

void TestTopic::tearDown() {
	btlogger((char*) "TestTopic::tearDown");
	// Do local work
	::tpfree(sendbuf);

	int toCheck = tpunadvertise((char*) "TestTopic");
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(toCheck != -1);

	// Clean up server
	BaseServerTest::tearDown();
}

void TestTopic::test_tpacall_topic() {
	btlogger((char*) "test_tpacall_topic");

	int id = ::tpacall((char*) "TestTopic", (char *) sendbuf, sendlen, TPNOREPLY);

	BT_ASSERT(id == 0);
	BT_ASSERT(tperrno == 0);
}

void TestTopic::test_tpacall_topic_without_noreply() {
	btlogger((char*) "test_tpacall_topic_without_noreply");

	int id = ::tpacall((char*) "TestTopic", (char *) sendbuf, sendlen, 0);
	BT_ASSERT(id == -1);
	BT_ASSERT(tperrno == TPEINVAL);
}

void test_topic_service(TPSVCINFO *svcinfo) {
	btlogger((char*) "test_topic_service");
}

CPPUNIT_TEST_SUITE_REGISTRATION( TestTopic);
