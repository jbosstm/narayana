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
#include "TestServerinit.h"
#include "AtmiBrokerServer.h"
#include "btclient.h"

#include <stdlib.h>

#if defined(__cplusplus)
extern "C" {
#endif
extern void test_service(TPSVCINFO *svcinfo);
#if defined(__cplusplus)
}
#endif

void TestServerinit::test_serverinit() {
	btlogger((char*) "test_serverinit");
	int result;
#ifdef WIN32
	char* argv[] = {(char*)"server", (char*)"-c", (char*)"win32", (char*)"-i", (char*)"1", (char*)"-s", (char*)"testsui"};
#else
	char* argv[] = {(char*)"server", (char*)"-c", (char*)"linux", (char*)"-i", (char*)"1", (char*)"-s", (char*)"testsui"};
#endif
	int argc = sizeof(argv)/sizeof(char*);

	result = serverinit(argc, argv);
	BT_ASSERT(result != -1);
	BT_ASSERT(tperrno == 0);

	BT_ASSERT(ptrServer->isAdvertised((char*)"BAR"));
	result = serverdone();
	BT_ASSERT(result != -1);
	BT_ASSERT(tperrno == 0);
}

void TestServerinit::test_config_env() {
	btlogger((char*) "TestServerinit::test_config_env");
	int result;
#ifdef WIN32
	char* argv[] = {(char*)"server", (char*)"-c", (char*)"win32", (char*)"-i", (char*)"1", (char*)"-s", (char*)"testsui"};
#else
	char* argv[] = {(char*)"server", (char*)"-c", (char*)"linux", (char*)"-i", (char*)"1", (char*)"-s", (char*)"testsui"};
#endif
	int argc = sizeof(argv)/sizeof(char*);

	result = serverinit(argc, argv);
	BT_ASSERT(result != -1);
	BT_ASSERT(tperrno == 0);

	result = serverdone();
	BT_ASSERT(result != -1);
	BT_ASSERT(tperrno == 0);

	clientdone(0);

	putenv("BLACKTIE_CONFIGURATION_DIR=nosuch_conf");
	result = serverinit(argc, argv);
	putenv("BLACKTIE_CONFIGURATION_DIR=.");
	BT_ASSERT(result == -1);
}

void TestServerinit::test_config_cmdline() {
	btlogger((char*) "TestServerinit::test_config_cmdline");
	int result;

#ifdef WIN32
		char* argv1[] = {(char*)"server", (char*)"-c", (char*)"win32", (char*)"-i", (char*)"1", (char*)"-s", (char*)"testsui"};
#else
		char* argv1[] = {(char*)"server", (char*)"-c", (char*)"linux", (char*)"-i", (char*)"1", (char*)"-s", (char*)"testsui"};
#endif
	int argc1 = sizeof(argv1)/sizeof(char*);

	result = serverinit(argc1, argv1);
	BT_ASSERT(result != -1);
	BT_ASSERT(tperrno == 0);

	int id = ::tpadvertise((char*) "TestTPAdvertise", test_service);
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(id != -1);

	result = serverdone();
	BT_ASSERT(result != -1);
	BT_ASSERT(tperrno == 0);

	/* invalid command line arguments */
	char* argv2[] = {(char*)"server", (char*)"-i", (char*)"conf"};
	int argc2 = sizeof(argv2)/sizeof(char*);

	result = serverinit(argc2, argv2);
	BT_ASSERT(result == -1);
	serverdone();
}

void TestServerinit::test_requires_id() {
	btlogger((char*) "TestServerinit::test_requires_id");
	/* make the -i paramenter mandatory */
	char* argv3[] = {(char*)"server"};
	int argc3 = sizeof(argv3)/sizeof(char*);

	int result = serverinit(argc3, argv3);
	BT_ASSERT(result == -1);

	result = serverdone();
	BT_ASSERT(result != -1);
	BT_ASSERT(tperrno == 0);
}

void test_service(TPSVCINFO *svcinfo) {
	btlogger((char*) "test_service");
}

CPPUNIT_TEST_SUITE_REGISTRATION( TestServerinit);
