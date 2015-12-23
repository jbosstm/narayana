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
#include "TestAdmin.h"
#include "malloc.h"
#include "Sleeper.h"

void TestAdmin::setUp() {
	btlogger((char*) "TestAdmin::setUp");
	BaseAdminTest::setUp();
}

void TestAdmin::tearDown() {
	btlogger((char*) "TestAdmin::tearDown");
	BaseAdminTest::tearDown();
}

char* TestAdmin::getBARCounter(char* command) {
	btlogger((char*) "TestAdmin::getBARCounter");
	char* n = NULL;
	int cd;

	cd = callADMIN(command, '1', 0, &n);	
	BT_ASSERT(cd == 0);

	return n;
}

void TestAdmin::testStatus() {
	btlogger((char*) "TestAdmin::testStatus");
	int cd;

	cd = callADMIN((char*)"status", '1', 0, NULL);
	BT_ASSERT(cd == 0);
	cd = callADMIN((char*)"status,BAR,", '1', 0, NULL);
	BT_ASSERT(cd == 0);
}

void TestAdmin::testVersion() {
	btlogger((char*) "TestAdmin::testVersion");
	int cd;
	char* ver = NULL;

	cd = callADMIN((char*)"version", '1', 0, &ver);
	BT_ASSERT(cd == 0);
	btlogger((char*)"version is %s", ver);
	BT_ASSERT(strcmp(ver, "5.2.11.Final") == 0);
	free(ver);
}

void TestAdmin::testMessageCounter() {
	btlogger((char*) "TestAdmin::testMessageCounter");
	char* barCounter = getBARCounter((char*)"counter,BAR,");
	BT_ASSERT(strncmp(barCounter, "0", 1) == 0);
	free (barCounter);
	BT_ASSERT(callBAR(0) == 0);
	barCounter = getBARCounter((char*)"counter,BAR,");
	BT_ASSERT(strncmp(barCounter, "1", 1) == 0);
	free (barCounter);
}

void TestAdmin::testErrorCounter() {
	btlogger((char*) "TestAdmin::testErrorCounter");
	char* barCounter = getBARCounter((char*)"error_counter,BAR,");
	BT_ASSERT(strncmp(barCounter, "0", 1) == 0);
	free (barCounter);
	BT_ASSERT(callBAR(TPESVCFAIL, (char*)"error_counter_test") == -1);
	barCounter = getBARCounter((char*)"error_counter,BAR,");
	btlogger("error counter = %s", barCounter);
	BT_ASSERT(strncmp(barCounter, "1", 1) == 0);
	free (barCounter);
}

void TestAdmin::testServerdone() {
	btlogger((char*) "TestAdmin::testServerdone");
	int cd;

	cd = callADMIN((char*)"serverdone", '1', 0, NULL);
	BT_ASSERT(cd == 0);
}

void TestAdmin::testServerPauseAndResume() {
	btlogger((char*) "TestAdmin::testServerPauseAndResume");
	int cd;

	BT_ASSERT(callBAR(0) == 0);
	btlogger((char*)"call BAR OK");

	cd = callADMIN((char*)"pause", '1', 0, NULL);
	BT_ASSERT(cd == 0);
	btlogger((char*)"pause server OK");

	btlogger((char*)"unadvertise on pause server should OK");
	cd = callADMIN((char*)"unadvertise,BAR,", '1', 0, NULL);
	BT_ASSERT(cd == 0);

	btlogger((char*)"advertise on pause server should OK, but service is still pause");
	cd = callADMIN((char*)"advertise,BAR,", '1', 0, NULL);
	BT_ASSERT(cd == 0);

	btlogger((char*)"call BAR should time out after 50 seconds");
	BT_ASSERT(callBAR(TPETIME) == -1);

	cd = callADMIN((char*)"resume", '1', 0, NULL);
	BT_ASSERT(cd == 0);
	btlogger((char*)"resume server OK");

	::sleeper(3);
	btlogger((char*)"call BAR should OK");
	BT_ASSERT(callBAR(0) == 0);
	btlogger((char*)"call BAR OK");
}
CPPUNIT_TEST_SUITE_REGISTRATION( TestAdmin );

