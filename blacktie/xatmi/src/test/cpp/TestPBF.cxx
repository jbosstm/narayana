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
#include "BaseTest.h"

#include "xatmi.h"
#include "malloc.h"

#include "TestPBF.h"

#if defined(__cplusplus)
extern "C" {
#endif
extern void pbf_service(TPSVCINFO *svcinfo);

void TestPBF::setUp() {
	btlogger((char*) "TestPBF::setUp");
	BaseServerTest::setUp();

	// Do local work
	m_allocated = NULL;
	sendbuf = NULL;
	rcvbuf = NULL;
	BT_ASSERT(tperrno == 0);
}
#if defined(__cplusplus)
}
#endif

void TestPBF::tearDown() {
	btlogger((char*) "TestPBF::tearDown");
	// Do local work
	if (m_allocated) {
		::tpfree( m_allocated);
		m_allocated = NULL;
	}

	if (sendbuf) {
		::tpfree( sendbuf);
		sendbuf = NULL;
	}

	if (rcvbuf) {
		::tpfree( rcvbuf);
		rcvbuf = NULL;
	}

	BaseServerTest::tearDown();
}

void TestPBF::test_tpalloc() {
	btlogger((char*) "test_tpalloc");
	ACCT_INFO *aptr;
	aptr = (ACCT_INFO*) tpalloc((char*) "X_COMMON", (char*) "acct_info", 0);

	m_allocated = (char*) aptr;
	BT_ASSERT(m_allocated != NULL);
	BT_ASSERT(tperrno == 0);
	// Won't check length as typtypes does that

	// ASSIGN SOME VALUES
	aptr->acct_no = 12345678;
	strcpy(aptr->name, "1234567890123456789012345678901234567890123456789");
	aptr->balances[0] = 0;
	aptr->balances[1] = 0;
	strcpy(aptr->address, "");

	// CHECK THE ASSIGNATIONS
	BT_ASSERT(aptr->acct_no == 12345678);
	BT_ASSERT(strcmp(aptr->name,
			"1234567890123456789012345678901234567890123456789") == 0);
	BT_ASSERT(strcmp(aptr->address, "") == 0);
	BT_ASSERT(aptr->balances[0] == 0);
	BT_ASSERT(aptr->balances[1] == 0);
}

void TestPBF::test_tpalloc_nonzero() {
	btlogger((char*) "test_tpalloc_nonzero");
	m_allocated = tpalloc((char*) "X_COMMON", (char*) "acct_info", 10);
	BT_ASSERT(m_allocated != NULL);
	BT_ASSERT(tperrno == 0);

	char* type = (char*) malloc(8);
	char* subtype = (char*) malloc(16);
	int toTest = ::tptypes(m_allocated, type, subtype);
	BT_ASSERT(tperrno == 0);

	char* toTestS = (char*) malloc(110);
	sprintf(toTestS, "%d", toTest);
	BT_ASSERT_MESSAGE(toTestS, toTest >= (int) sizeof(ACCT_INFO));
	free (toTestS);
	BT_ASSERT(toTest != 10);
	BT_ASSERT(strncmp(type, "X_COMMON", 8) == 0);
	BT_ASSERT(strcmp(subtype, "acct_info") == 0);
	free(type);
	free(subtype);
}

void TestPBF::test_tpalloc_subtype_required() {
	btlogger((char*) "test_tpalloc_subtype_required");
	m_allocated = tpalloc((char*) "X_COMMON", NULL, 0);
	char* tperrnoS = (char*) malloc(110);
	sprintf(tperrnoS, "%d", tperrno);
	BT_ASSERT_MESSAGE(tperrnoS, tperrno == TPEOS);
	free (tperrnoS);
	BT_ASSERT(m_allocated == NULL);
}

void TestPBF::test_tpalloc_wrong_subtype() {
	btlogger((char*) "test_tpalloc_subtype_required");
	m_allocated = tpalloc((char*) "X_COMMON", (char*) "not_exist", 0);
	BT_ASSERT(tperrno == TPEINVAL);
	BT_ASSERT(m_allocated == NULL);
}

void TestPBF::test_tprealloc() {
	btlogger("test_tprealloc");
	m_allocated = tpalloc((char*) "X_COMMON", (char*) "acct_info", 0);
	BT_ASSERT(m_allocated != NULL);
	// tprealloc for X_COMMON buffer type should be treated as a NOOP
	m_allocated = ::tprealloc(m_allocated, 10);
	char* tperrnoS = (char*) malloc(110);
	sprintf(tperrnoS, "%d", tperrno);
	BT_ASSERT_MESSAGE(tperrnoS, tperrno == 0);
	free (tperrnoS);
}

void TestPBF::test_tptypes() {
	btlogger((char*) "test_tptypes");
	m_allocated = tpalloc((char*) "X_COMMON", (char*) "acct_info", 0);
	BT_ASSERT(m_allocated != NULL);

	char* type = (char*) malloc(8);
	char* subtype = (char*) malloc(16);
	int toTest = ::tptypes(m_allocated, type, subtype);
	BT_ASSERT(tperrno == 0);

	char* toTestS = (char*) malloc(110);
	sprintf(toTestS, "%d", tperrno);
	BT_ASSERT_MESSAGE(toTestS, toTest >= (int) sizeof(ACCT_INFO));
	free (toTestS);
	BT_ASSERT(strncmp(type, "X_COMMON", 8) == 0);
	BT_ASSERT(strcmp(subtype, "acct_info") == 0);
	free(type);
	free(subtype);
}

void TestPBF::test_tpfree() {
	btlogger((char*) "test_tpfree");
	ACCT_INFO *aptr;
	aptr = (ACCT_INFO*) tpalloc((char*) "X_COMMON", (char*) "acct_info", 0);
	m_allocated = (char*) aptr;
	BT_ASSERT(m_allocated != NULL);
	BT_ASSERT(tperrno == 0);

	::tpfree( m_allocated);
	m_allocated = NULL;
	BT_ASSERT(tperrno == 0);
}

void TestPBF::test_tpcall() {
	btlogger((char*) "test_tpcall");
	tpadvertise((char*) "PBF", pbf_service);
	char* tperrnoS = (char*) malloc(110);
	sprintf(tperrnoS, "%d", tperrno);
	BT_ASSERT_MESSAGE(tperrnoS, tperrno == 0);
	free (tperrnoS);

	ACCT_INFO *aptr;
	aptr = (ACCT_INFO*) tpalloc((char*) "X_COMMON", (char*) "acct_info", 0);
	long rcvlen = 60;

	BT_ASSERT((rcvbuf = (char *) tpalloc((char*) "X_OCTET", NULL, rcvlen))
			!= NULL);
	sendbuf = (char*) aptr;
	aptr->acct_no = 12345678;
	strcpy(aptr->name, "1234567890123456789012345678901234567890123456789");
	strcpy(
			aptr->address,
			"123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789");
	aptr->foo[0] = 1.1F;
	aptr->foo[1] = 2.2F;

	aptr->balances[0] = 1.1;
	aptr->balances[1] = 2.2;


	BT_ASSERT(aptr->acct_no == 12345678);
	BT_ASSERT(strcmp(aptr->name,
			"1234567890123456789012345678901234567890123456789") == 0);
	BT_ASSERT(strcmp(
							aptr->address,
							"123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789")
							== 0);
	BT_ASSERT(aptr->foo[0] == 1.1F && aptr->foo[1] == 2.2F);
	BT_ASSERT(aptr->balances[0] == 1.1 && aptr->balances[1] == 2.2);

	int id = ::tpcall((char*) "PBF", (char*) aptr, 0, (char**) &rcvbuf,
			&rcvlen, TPNOCHANGE);
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(tpurcode == 23);
	BT_ASSERT(id != -1);
	BT_ASSERT_MESSAGE(rcvbuf, strcmp(rcvbuf, "fail") != 0);
	BT_ASSERT_MESSAGE(rcvbuf, strcmp(rcvbuf, "pbf_service") == 0);
}

void pbf_service(TPSVCINFO *svcinfo) {
	btlogger((char*) "pbf_service");
	bool ok = false;
	ACCT_INFO *aptr = (ACCT_INFO*) svcinfo->data;
	bool acctEq = aptr->acct_no == 12345678;
	bool nameEq = strcmp(aptr->name,
			"1234567890123456789012345678901234567890123456789") == 0;
	bool
			addressEq =
					strcmp(
							aptr->address,
							"123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789")
							== 0;
	bool fooEq = aptr->foo[0] == 1.1F && aptr->foo[1] == 2.2F;
	bool balsEq = aptr->balances[0] == 1.1 && aptr->balances[1] == 2.2;
	btlogger((char*) "pbf_service tests: %s", aptr->address);
	btlogger((char*) "pbf_service fooEq: %d %d", aptr->foo[0], aptr->foo[1]);
	btlogger((char*) "pbf_service balsEq: %d %d", aptr->balances[0], aptr->balances[1]);
	if (acctEq && nameEq && addressEq && fooEq && balsEq) {
		ok = true;
	}
	int len = 60;
	char *toReturn = ::tpalloc((char*) "X_OCTET", NULL, len);
	if (ok) {
		strcpy(toReturn, "pbf_service");
	} else {
		strcpy(toReturn, "fail");
	}
	tpreturn(TPSUCCESS, 23, toReturn, len, 0);
}

CPPUNIT_TEST_SUITE_REGISTRATION( TestPBF);
