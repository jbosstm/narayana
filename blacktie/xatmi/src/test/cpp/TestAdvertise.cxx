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
#include "TestAdvertise.h"

void TestAdvertise::setUp() {
	btlogger((char*) "TestAdvertise::setUp");
	BaseAdminTest::setUp();
}

void TestAdvertise::tearDown() {
	btlogger((char*) "TestAdvertise::tearDown");
	BaseAdminTest::tearDown();
}

void TestAdvertise::testService() {
	int cd;

	cd = callADMIN((char*)"advertise,BAR,", '1', 0, NULL);
	BT_ASSERT(cd == 0);

	cd = callADMIN((char*)"advertise,.testsui1,", '0', 0, NULL);
	BT_ASSERT(cd == 0);
}

void TestAdvertise::testUnknowService() {
	int   cd;

	cd = callADMIN((char*)"advertise,UNKNOW,", '0', 0, NULL);
	BT_ASSERT(cd == 0);
}

void TestAdvertise::testAdvertise() {
	int   cd;

	btlogger((char*) "unadvertise BAR");
	cd = callADMIN((char*)"unadvertise,BAR,", '1', 0, NULL);
	BT_ASSERT(cd == 0);

	btlogger((char*) "tpcall BAR after unadvertise");
	cd = callBAR(TPENOENT);
	BT_ASSERT(cd != 0);

	btlogger((char*) "advertise BAR again");
	cd = callADMIN((char*)"advertise,BAR,", '1', 0, NULL);
	BT_ASSERT(cd == 0);

	btlogger((char*) "tpcall BAR after advertise");
	cd = callBAR(0);
	BT_ASSERT(cd == 0);
}

CPPUNIT_TEST_SUITE_REGISTRATION( TestAdvertise );
