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

#include "TestClientInit.h"

#include "xatmi.h"
extern "C" {
#include "btclient.h"
}

#include <stdlib.h>

void TestClientInit::test_clientinit() {
	btlogger((char*) "TestClientInit::test_clientinit");
	BT_ASSERT(tperrno == 0);
	int valToTest = ::clientinit();
	BT_ASSERT(valToTest != -1);
	BT_ASSERT(tperrno == 0);

	valToTest = ::clientdone(0);
	BT_ASSERT(valToTest != -1);
	BT_ASSERT(tperrno == 0);
}

void TestClientInit::test_config_env() {
	btlogger((char*) "TestClientInit::test_config_env");

	BT_ASSERT(tperrno == 0);
	int valToTest = ::clientinit();
	BT_ASSERT(valToTest != -1);
	BT_ASSERT(tperrno == 0);

	valToTest = ::clientdone(0);
	BT_ASSERT(valToTest != -1);
	BT_ASSERT(tperrno == 0);

	/* wrong envionment */
	putenv("BLACKTIE_CONFIGURATION_DIR=nosuch_conf");
	valToTest = ::clientinit();
	putenv("BLACKTIE_CONFIGURATION_DIR=.");
	BT_ASSERT(valToTest == -1);
}

CPPUNIT_TEST_SUITE_REGISTRATION( TestClientInit);
