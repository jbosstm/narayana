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
#include "XATMITestSuite.h"

#include "xatmi.h"
#include "malloc.h"

#include "TestTPAlloc.h"

void TestTPAlloc::setUp() {
	btlogger((char*) "TestTPAlloc::setUp");
	m_allocated = NULL;

	BaseTest::setUp();
	// Do local work
	BT_ASSERT(tperrno == 0);
}

void TestTPAlloc::tearDown() {
	btlogger((char*) "TestTPAlloc::tearDown");
	if (m_allocated) {
		::tpfree( m_allocated);
		m_allocated = NULL;
	}
	// Do local work
	BaseTest::tearDown();
}

void TestTPAlloc::test_tpalloc_zero() {
	btlogger((char*) "test_tpalloc_zero");
	m_allocated = tpalloc((char*) "X_OCTET", NULL, 0);
	BT_ASSERT(m_allocated == NULL);
	BT_ASSERT(tperrno == TPEINVAL);
}

void TestTPAlloc::test_tpalloc_negative() {
	btlogger((char*) "test_tpalloc_negative");
	m_allocated = tpalloc((char*) "X_OCTET", NULL, -1);
	BT_ASSERT(m_allocated == NULL);
	BT_ASSERT(tperrno == TPEINVAL);
}

void TestTPAlloc::test_tpalloc_x_octet_subtype_ignored() {
	btlogger((char*) "test_tpalloc_x_octet_subtype_ignored");
	m_allocated = tpalloc((char*) "X_OCTET", (char*) "fail", 25);
	BT_ASSERT(m_allocated != NULL);
	BT_ASSERT(tperrno == 0);
}

// 9.1.1
void TestTPAlloc::test_tpalloc_x_octet() {
	btlogger((char*) "test_tpalloc_x_octet");
	m_allocated = tpalloc((char*) "X_OCTET", NULL, 28);
	BT_ASSERT(m_allocated != NULL);
	BT_ASSERT(tperrno == 0);

	char type[8];
	char subtype[16];
	int toTest = ::tptypes(m_allocated, type, subtype);
	BT_ASSERT(tperrno == 0);
	char* toTestS = (char*) malloc(110);
	sprintf(toTestS, "%d", toTest);
	BT_ASSERT_MESSAGE(toTestS, toTest == 28);
	free(toTestS);
	BT_ASSERT(strncmp(type, "X_OCTET", 8) == 0);
	BT_ASSERT(strcmp(subtype, "") == 0);

}

// 9.1.2
void TestTPAlloc::test_tpalloc_x_common() {
	btlogger((char*) "test_tpalloc_x_common");
	DEPOSIT *dptr;
	dptr = (DEPOSIT*) tpalloc((char*) "X_COMMON", (char*) "deposit", 0);
	m_allocated = (char*) dptr;
	BT_ASSERT(m_allocated != NULL);
	BT_ASSERT(tperrno == 0);
	// Won't check lenght as tptypes does this

	// ASSIGN SOME VALUES
	dptr->acct_no = 12345678;
	dptr->amount = 50;
	dptr->balance = 0;
	dptr->status[100] = 'c';
	dptr->status_len = 0;

	// CHECK THE ASSIGNATIONS
	BT_ASSERT(dptr->acct_no == 12345678);
	BT_ASSERT(dptr->amount == 50);
	BT_ASSERT(dptr->balance == 0);
	BT_ASSERT(dptr->status[100] == 'c');
	BT_ASSERT(dptr->status_len == 0);
}

void TestTPAlloc::test_tpalloc_x_common_bigsubtype() {
	btlogger((char*) "test_tpalloc_x_common_bigsubtype");
	DEPOSIT *dptr;
	dptr
			= (DEPOSIT*) tpalloc((char*) "X_COMMON",
					(char*) "abcdefghijklmnop", 0);
	m_allocated = (char*) dptr;
	BT_ASSERT(m_allocated != NULL);
	BT_ASSERT(tperrno == 0);

	char* type = (char*) malloc(10);
	char* subtype = (char*) malloc(20);
	memset(subtype, '\0', 20);
	tptypes(m_allocated, type, subtype);
	BT_ASSERT(strncmp(type, "X_COMMON", 8) == 0);
	BT_ASSERT(strncmp(subtype, "abcdefghijklmnopq", 17) != 0);
	BT_ASSERT(strncmp(subtype, "abcdefghijklmnop", 16) == 0);
	free(type);
	free(subtype);
}

// 9.1.3
void TestTPAlloc::test_tpalloc_x_c_type() {
	btlogger((char*) "test_tpalloc_x_c_type");
	ACCT_INFO *aptr;
	aptr = (ACCT_INFO*) tpalloc((char*) "X_C_TYPE", (char*) "acct_info", 0);
	m_allocated = (char*) aptr;
	BT_ASSERT(m_allocated != NULL);
	BT_ASSERT(tperrno == 0);
	// Won't check length as typtypes does that

	// ASSIGN SOME VALUES
	aptr->acct_no = 12345678;
	strcpy(aptr->name, "12345678901234567890123456789012345678901234567890");
	aptr->balances[0] = 0;
	aptr->balances[1] = 0;

	// CHECK THE ASSIGNATIONS
	BT_ASSERT(aptr->acct_no == 12345678);
	BT_ASSERT(strcmp(aptr->name,
			"12345678901234567890123456789012345678901234567890") == 0);
	BT_ASSERT(aptr->address == NULL || strcmp(aptr->address, "") == 0);
	BT_ASSERT(aptr->balances[0] == 0);
	BT_ASSERT(aptr->balances[1] == 0);
}

void TestTPAlloc::test_tpalloc_unknowntype() {
	btlogger((char*) "test_tpalloc_unknowntype");
	m_allocated = tpalloc((char*) "TOM", NULL, 10);
	BT_ASSERT(tperrno == TPENOENT);
	BT_ASSERT(m_allocated == NULL);
}

void TestTPAlloc::test_tpalloc_x_common_subtype_required() {
	btlogger((char*) "test_tpalloc_x_common_subtype_required");
	m_allocated = tpalloc((char*) "X_COMMON", NULL, 0);
	char* tperrnoS = (char*) malloc(110);
	sprintf(tperrnoS, "%d", tperrno);
	BT_ASSERT_MESSAGE(tperrnoS, tperrno == TPEOS);
	free(tperrnoS);
	BT_ASSERT(m_allocated == NULL);
}

void TestTPAlloc::test_tpalloc_x_c_type_subtype_required() {
	btlogger((char*) "test_tpalloc_x_c_type_subtype_required");
	m_allocated = tpalloc((char*) "X_C_TYPE", NULL, 0);
	char* tperrnoS = (char*) malloc(110);
	sprintf(tperrnoS, "%d", tperrno);
	BT_ASSERT_MESSAGE(tperrnoS, tperrno == TPEOS);
	free(tperrnoS);
	BT_ASSERT(m_allocated == NULL);
}

void TestTPAlloc::test_tpalloc_x_common_unknown_subtype() {
	btlogger((char*) "test_tpalloc_x_common_unknown_subtype");
	m_allocated = tpalloc((char*) "X_COMMON", (char*) "UNKNOWN", 0);
	char* tperrnoS = (char*) malloc(110);
	sprintf(tperrnoS, "%d", tperrno);
	BT_ASSERT_MESSAGE(tperrnoS, tperrno == TPEOS);
	free(tperrnoS);
	BT_ASSERT(m_allocated == NULL);
}

void TestTPAlloc::test_tpalloc_x_c_type_unknown_subtype() {
	btlogger((char*) "test_tpalloc_x_c_type_unknown_subtype");
	m_allocated = tpalloc((char*) "X_C_TYPE", (char*) "UNKNOWN", 0);
	char* tperrnoS = (char*) malloc(110);
	sprintf(tperrnoS, "%d", tperrno);
	BT_ASSERT_MESSAGE(tperrnoS, tperrno == TPEOS);
	free(tperrnoS);
	BT_ASSERT(m_allocated == NULL);
}

CPPUNIT_TEST_SUITE_REGISTRATION( TestTPAlloc);
