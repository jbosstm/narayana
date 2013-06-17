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
#include "XATMITestSuite.h"
#include "BaseServerTest.h"

#include "xatmi.h"

#include "TestTPFreeService.h"

#if defined(__cplusplus)
extern "C" {
#endif
extern void testtpfreeservice_service(TPSVCINFO *svcinfo);
#if defined(__cplusplus)
}
#endif

void TestTPFreeService::setUp() {
	btlogger((char*) "TestTPFreeService::setUp");
	m_allocated = NULL;
	m_rcvbuf = NULL;

	// Start server
	BaseServerTest::setUp();

	// Do local work
	int toCheck = tpadvertise((char*) "TestTPFree", testtpfreeservice_service);
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(toCheck != -1);
	m_allocated = NULL;

	m_rcvlen = 1;
	m_rcvbuf = (char *) tpalloc((char*) "X_OCTET", NULL, 1);
	BT_ASSERT(m_rcvbuf != NULL);
}

void TestTPFreeService::tearDown() {
	btlogger((char*) "TestTPFreeService::tearDown");
	// Do local work
	::tpfree( m_allocated);
	::tpfree( m_rcvbuf);
	int toCheck = tpunadvertise((char*) "TestTPFree");
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(toCheck != -1);

	// Clean up server
	BaseServerTest::tearDown();
}

void TestTPFreeService::test_tpfree_x_octet() {
	btlogger((char*) "test_tpfree_x_octet");
	m_allocated = tpalloc((char*) "X_OCTET", NULL, 10);
	memset(m_allocated, '\0', 10);
	BT_ASSERT(m_allocated != NULL);

	int toCheck = ::tpcall((char*) "TestTPFree", (char*) m_allocated, 10,
			(char**) &m_rcvbuf, &m_rcvlen, 0);
	BT_ASSERT(toCheck != -1);
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(m_rcvbuf[0] == '1');
}

void TestTPFreeService::test_tpfree_x_common() {
	btlogger((char*) "test_tpfree_x_common");
	DEPOSIT *dptr;
	dptr = (DEPOSIT*) tpalloc((char*) "X_COMMON", (char*) "deposit", 0);
	m_allocated = (char*) dptr;
	BT_ASSERT(m_allocated != NULL);
	BT_ASSERT(tperrno == 0);

	int toCheck = ::tpcall((char*) "TestTPFree", (char*) m_allocated, 0,
			(char**) &m_rcvbuf, &m_rcvlen, 0);
	BT_ASSERT(toCheck != -1);
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(m_rcvbuf[0] == '1');
}

void TestTPFreeService::test_tpfree_x_c_type() {
	btlogger((char*) "test_tpfree_x_c_type");
	ACCT_INFO *aptr;
	aptr = (ACCT_INFO*) tpalloc((char*) "X_C_TYPE", (char*) "acct_info", 0);
	m_allocated = (char*) aptr;
	BT_ASSERT(m_allocated != NULL);
	BT_ASSERT(tperrno == 0);

	int toCheck = ::tpcall((char*) "TestTPFree", (char*) m_allocated, 0,
			(char**) &m_rcvbuf, &m_rcvlen, 0);
	BT_ASSERT(toCheck != -1);
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(m_rcvbuf[0] == '1');
}

void testtpfreeservice_service(TPSVCINFO *svcinfo) {
	btlogger((char*) "testtpfreeservice_service");
	// Allocate a buffer to return
	char *toReturn = tpalloc((char*) "X_OCTET", (char*) "acct_info", 1);

	// Free should be idempotent on the inbound buffer
	::tpfree(svcinfo->data);

	// Get the data from tptypes still
	int toTest = ::tptypes(svcinfo->data, NULL, NULL);

	// Check the values of tptypes (should still have been valid
	if (toTest == -1 || tperrno == TPEINVAL) {
		// False
		toReturn[0] = '0';
	} else {
		// True
		toReturn[0] = '1';
	}

	// Return the data
	tpreturn(TPSUCCESS, 0, toReturn, 1, 0);
}
CPPUNIT_TEST_SUITE_REGISTRATION( TestTPFreeService);
