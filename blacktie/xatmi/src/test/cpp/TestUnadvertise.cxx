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
#include "TestUnadvertise.h"

void TestUnadvertise::setUp() {
	btlogger((char*) "TestUnadvertise::setUp");
	BaseAdminTest::setUp();
}

void TestUnadvertise::tearDown() {
	btlogger((char*) "TestUnadvertise::tearDown");
	BaseAdminTest::tearDown();
}

void TestUnadvertise::testAdminService() {
	int cd;

	// should not unadvertise ADMIN service by itself
	cd = callADMIN((char*)"unadvertise,.testsui1", '0', 0, NULL);
	BT_ASSERT(cd == 0);
}

void TestUnadvertise::testUnknowService() {
	int   cd;

	cd = callADMIN((char*)"unadvertise,UNKNOW,", '0', 0, NULL);
	BT_ASSERT(cd == 0);
}

void TestUnadvertise::testUnadvertise() {
	int   cd;

	btlogger((char*) "tpcall BAR before unadvertise");
	cd = callBAR(0);
	BT_ASSERT(cd == 0);
	
	cd = callADMIN((char*)"unadvertise,BAR,", '1', 0, NULL);
	BT_ASSERT(cd == 0);

	btlogger((char*) "tpcall BAR after unadvertise");
	cd = callBAR(TPENOENT);
	BT_ASSERT(cd != 0);
}

CPPUNIT_TEST_SUITE_REGISTRATION( TestUnadvertise );
