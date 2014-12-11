/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc., and others contributors as indicated
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
#include "tx/TestTxRMTPCall.h"
#include "TestAssert.h"

#include "BaseServerTest.h"
#include "XATMITestSuite.h"

extern "C" {
#include "tx/request.h"
extern int run_tests(product_t *prod_array);
extern void tx_db_service(TPSVCINFO *svcinfo);
}

void TestTxRMTPCall::setUp() {
	btlogger((char*) "TestTxRMTPCall::setUp");
	BaseServerTest::setUp();

	// Do local work
	BT_ASSERT(tpadvertise((char*) "tpcall_x_octet", tx_db_service)  != -1);
	BT_ASSERT(tperrno == 0);
}

void TestTxRMTPCall::tearDown() {
	btlogger((char*) "TestTxRMTPCall::tearDown");

	// Do local work
	BT_ASSERT(tpunadvertise((char*) "tpcall_x_octet")!= -1);
	BT_ASSERT(tperrno == 0);

	// Clean up server
	BaseServerTest::tearDown();
}

void TestTxRMTPCall::test0() {
	BT_ASSERT(run_tests(products) == 0);
}

CPPUNIT_TEST_SUITE_REGISTRATION( TestTxRMTPCall);
