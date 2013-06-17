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

#include "malloc.h"

#include "TestTPAdvertise.h"

#if defined(__cplusplus)
extern "C" {
#endif
extern void testtpadvertise_service(TPSVCINFO *svcinfo);
extern void testtpadvertise_service_2(TPSVCINFO *svcinfo);
#if defined(__cplusplus)
}
#endif

void TestTPAdvertise::setUp() {
	btlogger((char*) "TestTPAdvertise::setUp");
	sendbuf = NULL;
	rcvbuf = NULL;

	// Setup server
	BaseServerTest::setUp();

	// Do local work
	sendlen = 6;
	rcvlen = sendlen;
	sendbuf = (char *) tpalloc((char*) "X_OCTET", NULL, sendlen);
	rcvbuf = (char *) tpalloc((char*) "X_OCTET", NULL, sendlen);
}

void TestTPAdvertise::tearDown() {
	btlogger((char*) "TestTPAdvertise::tearDown");
	// Do local work
	::tpfree(sendbuf);
	::tpfree(rcvbuf);

	::tpunadvertise((char*) "TestTPAdvertise");
	::tpunadvertise((char*) "a12345678901234");
	::tpunadvertise((char*) "abcdefghij0123456789abcdefghij0123456789abcdefghij0123456789abcdefghij0123456789abcdefghij0123456789abcdefghij0123456789abcdefg");
	::tpunadvertise((char*) "underscore_name");

	// Clean up server
	BaseServerTest::tearDown();
}

void TestTPAdvertise::test_tpadvertise_new_service() {
	btlogger((char*) "test_tpadvertise_new_service");
	int id = ::tpadvertise((char*) "TestTPAdvertise", testtpadvertise_service);
	BT_ASSERT(tperrno!= TPEINVAL);
	BT_ASSERT(tperrno!= TPELIMIT);
	BT_ASSERT(tperrno!= TPEMATCH);
	BT_ASSERT(tperrno!= TPEPROTO);
	BT_ASSERT(tperrno!= TPESYSTEM);
	BT_ASSERT(tperrno!= TPEOS);
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(id != -1);

	(void) strcpy(sendbuf, "test0");
	id = ::tpcall((char*) "TestTPAdvertise", (char *) sendbuf, sendlen, (char **) &rcvbuf, &rcvlen, (long) 0);
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(id != -1);
	BT_ASSERT(strcmp(rcvbuf, "testtpadvertise_servicetest0") == 0);
}

void TestTPAdvertise::test_tpadvertise_underscore_name() {
	btlogger((char*) "test_tpadvertise_underscore_name");
	int id = ::tpadvertise((char*) "underscore_name", testtpadvertise_service);
	BT_ASSERT(tperrno!= TPEINVAL);
	BT_ASSERT(tperrno!= TPELIMIT);
	BT_ASSERT(tperrno!= TPEMATCH);
	BT_ASSERT(tperrno!= TPEPROTO);
	BT_ASSERT(tperrno!= TPESYSTEM);
	BT_ASSERT(tperrno!= TPEOS);
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(id != -1);

	(void) strcpy(sendbuf, "test0");
	id = ::tpcall((char*) "underscore_name", (char *) sendbuf, sendlen, (char **) &rcvbuf, &rcvlen, (long) 0);
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(id != -1);
	BT_ASSERT(strcmp(rcvbuf, "testtpadvertise_servicetest0") == 0);
}

void TestTPAdvertise::test_tpadvertise_null_name_null() {
	btlogger((char*) "test_tpadvertise_null_name_null");
	int id = ::tpadvertise(NULL, testtpadvertise_service);
	BT_ASSERT(tperrno== TPEINVAL);
	BT_ASSERT(id == -1);
}

void TestTPAdvertise::test_tpadvertise_idempotent() {
	btlogger((char*) "test_tpadvertise_idempotent");
	int id = ::tpadvertise((char*) "TestTPAdvertise", testtpadvertise_service);
	BT_ASSERT(tperrno == 0);
	char* idS = (char*) malloc(110);
	sprintf(idS, "%d", id);
	BT_ASSERT_MESSAGE(idS, id == 1);
	free(idS);
	id = ::tpadvertise((char*) "TestTPAdvertise", testtpadvertise_service);
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(id == 1);
}

void TestTPAdvertise::test_tpadvertise_null_name_empty() {
	btlogger((char*) "test_tpadvertise_null_name_empty");
	int id = ::tpadvertise((char*) "", testtpadvertise_service);
	BT_ASSERT(tperrno== TPEINVAL);
	BT_ASSERT(id == -1);
}

void TestTPAdvertise::test_tpadvertise_different_method() {
	btlogger((char*) "test_tpadvertise_different_method");
	int id = ::tpadvertise((char*) "TestTPAdvertise", testtpadvertise_service);
	BT_ASSERT(tperrno == 0);
	char* idS = (char*) malloc(110);
	sprintf(idS, "%d", id);
	BT_ASSERT_MESSAGE(idS, id == 1);
	free(idS);
	id = ::tpadvertise((char*) "TestTPAdvertise", testtpadvertise_service_2);
	BT_ASSERT(tperrno== TPEMATCH);
	BT_ASSERT(id == -1);
}

void TestTPAdvertise::test_tpadvertise_length_128() {
	btlogger((char*) "test_tpadvertise_length_128");
	int id = ::tpadvertise((char*) "abcdefghij0123456789abcdefghij0123456789abcdefghij0123456789abcdefghij0123456789abcdefghij0123456789abcdefghij0123456789abcdefg", testtpadvertise_service);
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(id != -1);
	id = ::tpadvertise((char*) "abcdefghij0123456789abcdefghij0123456789abcdefghij0123456789abcdefghij0123456789abcdefghij0123456789abcdefghij0123456789abcdefg", testtpadvertise_service_2);
	BT_ASSERT(tperrno== TPEMATCH);
	BT_ASSERT(id == -1);
}

void TestTPAdvertise::test_tpadvertise_readvertise() {
	btlogger((char*) "test_tpadvertise_readvertise");
	int id = ::tpadvertise((char*) "TestTPAdvertise", testtpadvertise_service);
	BT_ASSERT(id != -1);
	BT_ASSERT(tperrno == 0);
	id = ::tpunadvertise((char*) "TestTPAdvertise");
	BT_ASSERT(id != -1);
	BT_ASSERT(tperrno == 0);

	(void) strcpy(sendbuf, "test1");
	id = ::tpcall((char*) "TestTPAdvertise", (char *) sendbuf, sendlen, (char **) &rcvbuf, &rcvlen, (long) 0);
	BT_ASSERT(tperrno== TPENOENT);
	BT_ASSERT(id == -1);
	BT_ASSERT(strcmp(rcvbuf, "testtpadvertise_servicetest1") != 0);

	id = ::tpadvertise((char*) "TestTPAdvertise", testtpadvertise_service);
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(id != -1);

	(void) strcpy(sendbuf, "test2");
	id = ::tpcall((char*) "TestTPAdvertise", (char *) sendbuf, sendlen, (char **) &rcvbuf, &rcvlen, (long) 0);
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(id != -1);
	BT_ASSERT(strcmp(rcvbuf, "testtpadvertise_servicetest2") == 0);
}

void testtpadvertise_service(TPSVCINFO *svcinfo) {
	btlogger((char*) "testtpadvertise_service");
	char * toReturn = ::tpalloc((char*) "X_OCTET", NULL, 35);
	sprintf(toReturn, "testtpadvertise_service%s", svcinfo->data);
	// Changed length from 0L to svcinfo->len
	tpreturn(TPSUCCESS, 0, toReturn, 35, 0);
}

void testtpadvertise_service_2(TPSVCINFO *svcinfo) {
	btlogger((char*) "testtpadvertise_service_2");
	char * toReturn = ::tpalloc((char*) "X_OCTET", NULL, 25);
	strcpy(toReturn, "testtpadvertise_service_2");
	// Changed length from 0L to svcinfo->len
	tpreturn(TPSUCCESS, 0, toReturn, 25, 0);
}

CPPUNIT_TEST_SUITE_REGISTRATION( TestTPAdvertise);
