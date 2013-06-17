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
#include "XATMITestSuite.h"

#include "xatmi.h"

#include "TestSecurity.h"
#include "Sleeper.h"
#include "malloc.h"

#include "ace/OS_NS_stdlib.h"
#include "ace/OS_NS_stdio.h"
#include "ace/OS_NS_string.h"
#include "AtmiBrokerEnv.h"

#if defined(__cplusplus)
extern "C" {
#endif
extern void test_tpcall_SECURE(TPSVCINFO *svcinfo);;
#if defined(__cplusplus)
}
#endif

extern "C" {
#include "btclient.h"
}


void TestSecurity::setUp() {
	btlogger((char*) "TestSecurity::setUp");
	sendbuf = NULL;
	rcvbuf = NULL;
	ACE_OS::putenv("BLACKTIE_CONFIGURATION_DIR=serv");

	BaseTest::setUp();

#ifdef WIN32
		char* argv[] = {(char*)"server", (char*)"-c", (char*)"win32", (char*)"-i", (char*)"1", (char*)"-s", (char*)"secure"};
#else
		char* argv[] = {(char*)"server", (char*)"-c", (char*)"linux", (char*)"-i", (char*)"1", (char*)"-s", (char*)"secure"};
#endif
	int argc = sizeof(argv)/sizeof(char*);

	int result = serverinit(argc, argv);
	// Check that there is no error on server setup
	BT_ASSERT(result != -1);
	BT_ASSERT(tperrno == 0);

	// Do local work
	tpadvertise((char*) "SECURE", test_tpcall_SECURE);
}

void TestSecurity::tearDown() {
	btlogger((char*) "TestSecurity::tearDown");
	// Do local work
	ACE_OS::putenv("BLACKTIE_CONFIGURATION_DIR=.");
	::tpfree( sendbuf);
	::tpfree( rcvbuf);

	tpunadvertise((char*) "SECURE");
	// Clean up server
	// Stop the server
	serverdone();

	// Perform additional clean up
	BaseTest::tearDown();
}


void TestSecurity::test_tpcall_guest() {
	btlogger((char*) "test_tpcall_guest");


	btlogger_warn((char*) "This test hacks the underlying discard_instance so we can load a new configuration for the client");
    ::sleeper(15);
    ::clientdone(0);
    AtmiBrokerEnv::discard_instance();
    AtmiBrokerEnv::discard_instance();

	ACE_OS::putenv("BLACKTIE_CONFIGURATION_DIR=guest");

	sendlen = strlen("test_tpcall_guest") + 1;
	rcvlen = sendlen;
	BT_ASSERT((sendbuf
			= (char *) tpalloc((char*) "X_OCTET", NULL, sendlen)) != NULL);
	BT_ASSERT((rcvbuf = (char *) tpalloc((char*) "X_OCTET", NULL, rcvlen))
			!= NULL);
	(void) strcpy(sendbuf, "test_tpcall_guest");
	char* tperrnoS = (char*) malloc(110);
	sprintf(tperrnoS, "%d", tperrno);
	BT_ASSERT_MESSAGE(tperrnoS, tperrno == 0);
	free(tperrnoS);

	int id = ::tpcall((char*) "SECURE", (char *) sendbuf, sendlen,
			(char **) &rcvbuf, &rcvlen, (long) 0);
    tperrnoS = (char*) malloc(110);
	sprintf(tperrnoS, "%d", tperrno);
	BT_ASSERT_MESSAGE(tperrnoS, tperrno == TPENOENT);
	free(tperrnoS);
	BT_ASSERT(id == -1);
}

void TestSecurity::test_tpcall_dynsub() {
	btlogger((char*) "test_tpcall_dynsub");

	btlogger_warn((char*) "This test hacks the underlying discard_instance so we can load a new configuration for the client");
    ::sleeper(15);
    ::clientdone(0);
    AtmiBrokerEnv::discard_instance();
    AtmiBrokerEnv::discard_instance();

	ACE_OS::putenv("BLACKTIE_CONFIGURATION_DIR=dynsub");

	sendlen = strlen("test_tpcall_dynsub") + 1;
	rcvlen = sendlen;
	BT_ASSERT((sendbuf
			= (char *) tpalloc((char*) "X_OCTET", NULL, sendlen)) != NULL);
	BT_ASSERT((rcvbuf = (char *) tpalloc((char*) "X_OCTET", NULL, rcvlen))
			!= NULL);
	(void) strcpy(sendbuf, "test_tpcall_dynsub");
	char* tperrnoS = (char*) malloc(110);
	sprintf(tperrnoS, "%d", tperrno);
	BT_ASSERT_MESSAGE(tperrnoS, tperrno == 0);
	free(tperrnoS);

	int id = ::tpcall((char*) "SECURE", (char *) sendbuf, sendlen,
			(char **) &rcvbuf, &rcvlen, (long) 0);
    tperrnoS = (char*) malloc(110);
	sprintf(tperrnoS, "%d", tperrno);
	BT_ASSERT_MESSAGE(tperrnoS, tperrno == 0);
	free(tperrnoS);
	BT_ASSERT(id != -1);
	BT_ASSERT_MESSAGE(rcvbuf, strcmp(rcvbuf, "BAR SAYS HELLO") == 0);
}

void test_tpcall_SECURE(TPSVCINFO * svcinfo) {
	char* buffer;
	int sendlen;

	btlogger((char*) "bar called  - svc: %s data %s len: %d flags: %d", svcinfo->name, svcinfo->data, svcinfo->len, svcinfo->flags);

	sendlen = 15;
	buffer = tpalloc((char*) "X_OCTET", NULL, sendlen);
	strcpy(buffer, "BAR SAYS HELLO");

	tpreturn(TPSUCCESS, 0, buffer, sendlen, 0);
}

CPPUNIT_TEST_SUITE_REGISTRATION( TestSecurity );
