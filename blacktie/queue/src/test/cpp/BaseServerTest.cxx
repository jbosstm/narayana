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
#include "BaseTest.h"

static int initcalled = 0;
static int donecalled = 0;

extern "C" {
#include "btserver.h"
#include "btservice.h"

EXPORT_SERVICE int dummyserverinit(int argc, char** argv) {
    initcalled = 1;
    return 0;
}

EXPORT_SERVICE void dummyserverdone(void) {
    donecalled = 1;
}

}

#include "xatmi.h"

void BaseServerTest::setUp() {
	// Perform initial start up
	BaseTest::setUp();
	initcalled = donecalled = 0;
	startServer();
	BT_ASSERT(initcalled == 1);
}

void BaseServerTest::startServer() {
#ifdef WIN32
		char* argv[] = {(char*)"server", (char*)"-c", (char*)"win32", (char*)"-i", (char*)"1", (char*)"-s", (char*)"testsui"};
#else
		char* argv[] = {(char*)"server", (char*)"-c", (char*)"linux", (char*)"-i", (char*)"1", (char*)"-s", (char*)"testsui"};
#endif
	int argc = sizeof(argv)/sizeof(char*);

	int result = serverinit(argc, argv);
	// Check that there is no error on server setup
	BT_ASSERT(result != -1);
	BT_ASSERT(tperrno == 0);
}

void BaseServerTest::tearDown() {
	// Stop the server
	serverdone();
	BT_ASSERT(donecalled == 1);

	// Perform additional clean up
	BaseTest::tearDown();
}
