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
#include "TestTPTypes.h"

void TestTPTypes::setUp() {
	btlogger((char*) "TestTPTypes::setUp");
	BaseTest::setUp();
	// Do local work
	m_allocated = NULL;
}

void TestTPTypes::tearDown() {
	btlogger((char*) "TestTPTypes::tearDown");
	// Do local work
	if (m_allocated != NULL) {
		::tpfree( m_allocated);
	}

	BaseTest::tearDown();
}

void TestTPTypes::test_tptypes_x_octet() {
	btlogger((char*) "test_tptypes_x_octet");
	m_allocated = ::tpalloc((char*) "X_OCTET", NULL, 20);
	BT_ASSERT(m_allocated != NULL);

	char* type = (char*) malloc(8);
	char* subtype = (char*) malloc(16);
	int toTest = ::tptypes(m_allocated, type, subtype);
	BT_ASSERT(strcmp(type, "X_OCTET") == 0);
	BT_ASSERT(strcmp(subtype, "") == 0);
	free(type);
	free(subtype);
	BT_ASSERT(tperrno == 0);
	char* toTestS = (char*) malloc(110);
	sprintf(toTestS, "%d", toTest);
	BT_ASSERT_MESSAGE(toTestS, toTest == 20);
	free(toTestS);
}

void TestTPTypes::test_tptypes_x_common() {
	btlogger((char*) "test_tptypes_x_common");
	m_allocated = ::tpalloc((char*) "X_COMMON", (char*) "deposit", 0);
	BT_ASSERT(m_allocated != NULL);

	char* type = (char*) malloc(8);
	char* subtype = (char*) malloc(16);
	int toTest = ::tptypes(m_allocated, type, subtype);
	btlogger("DEPOSITSIZE%d", sizeof(DEPOSIT));
	BT_ASSERT(tperrno == 0);
	char* toTestS = (char*) malloc(110);
	sprintf(toTestS, "%d", toTest);
	BT_ASSERT_MESSAGE(toTestS, toTest >= (int) sizeof(DEPOSIT));
	free(toTestS);
	BT_ASSERT(strncmp(type, "X_COMMON", 8) == 0);
	BT_ASSERT(strcmp(subtype, "deposit") == 0);
	free(type);
	free(subtype);
}

void TestTPTypes::test_tptypes_x_common_bigdata() {
	btlogger((char*) "test_tptypes_x_common_bigdata");
	m_allocated = ::tpalloc((char*) "X_COMMON", (char*) "deposit", 0);
	BT_ASSERT(m_allocated != NULL);

	char* type = (char*) malloc(8);
	char* subtype = (char*) malloc(16);
	int toTest = ::tptypes(m_allocated, type, subtype);
	BT_ASSERT(tperrno == 0);
	char* toTestS = (char*) malloc(110);
	sprintf(toTestS, "%d", toTest);
	BT_ASSERT_MESSAGE(toTestS, toTest >= (int) sizeof(DEPOSIT));
	free(toTestS);
	BT_ASSERT(strncmp(type, "X_COMMON", 8) == 0);
	BT_ASSERT(strcmp(subtype, "deposit") == 0);
	free(type);
	free(subtype);
}

void TestTPTypes::test_tptypes_x_c_type() {
	btlogger((char*) "test_tptypes_x_c_type");
	m_allocated = ::tpalloc((char*) "X_C_TYPE", (char*) "acct_info", 0);
	BT_ASSERT(m_allocated != NULL);

	char* type = (char*) malloc(8);
	char* subtype = (char*) malloc(16);
	int toTest = ::tptypes(m_allocated, type, subtype);
	btlogger("acct_info SIZE%d", sizeof(ACCT_INFO));
	BT_ASSERT(tperrno == 0);
	char* toTestS = (char*) malloc(110);
	sprintf(toTestS, "%d", toTest);
	BT_ASSERT_MESSAGE(toTestS, toTest >= (int) sizeof(ACCT_INFO));
	free(toTestS);
	BT_ASSERT(strncmp(type, "X_C_TYPE", 8) == 0);
	BT_ASSERT(strcmp(subtype, "acct_info") == 0);
	free(type);
	free(subtype);
}

void TestTPTypes::test_tptypes_x_c_type_bigdata() {
	btlogger((char*) "test_tptypes_x_c_type_bigdata");
	m_allocated = ::tpalloc((char*) "X_C_TYPE", (char*) "acct_info", 0);
	BT_ASSERT(m_allocated != NULL);

	char* type = (char*) malloc(8);
	char* subtype = (char*) malloc(16);
	int toTest = ::tptypes(m_allocated, type, subtype);
	BT_ASSERT(tperrno == 0);
	char* toTestS = (char*) malloc(110);
	sprintf(toTestS, "%d", toTest);
	BT_ASSERT_MESSAGE(toTestS, toTest >= (int) sizeof(ACCT_INFO));
	free(toTestS);
	BT_ASSERT(strncmp(type, "X_C_TYPE", 8) == 0);
	BT_ASSERT(strcmp(subtype, "acct_info") == 0);
	free(type);
	free(subtype);
}

// 8.2
void TestTPTypes::test_tptypes_unallocated() {
	btlogger((char*) "test_tptypes_unallocated");
	char* type = (char*) malloc(8);
	char* subtype = (char*) malloc(16);
	int toTest = ::tptypes((char*) "test", type, subtype);
	BT_ASSERT(tperrno == TPEINVAL);
	BT_ASSERT(toTest == -1);
	free(type);
	free(subtype);
}

void TestTPTypes::test_tptypes_null_ptr() {
	btlogger((char*) "test_tptypes_null_ptr");
	char* type = (char*) malloc(8);
	char* subtype = (char*) malloc(16);
	int toTest = ::tptypes(NULL, type, subtype);
	BT_ASSERT(tperrno == TPEINVAL);
	BT_ASSERT(toTest == -1);
	free(type);
	free(subtype);
}

void TestTPTypes::test_tptypes_null_type() {
	btlogger((char*) "test_tptypes_null_type");
	m_allocated = ::tpalloc((char*) "X_COMMON", (char*) "deposit", 0);
	BT_ASSERT(m_allocated != NULL);
	BT_ASSERT(tperrno == 0);

	char* subtype = (char*) malloc(16);
	int toTest = ::tptypes(m_allocated, NULL, subtype);
	BT_ASSERT(tperrno == 0);
	char* toTestS = (char*) malloc(110);
	sprintf(toTestS, "%d", toTest);
	BT_ASSERT_MESSAGE(toTestS, toTest >= (int) sizeof(DEPOSIT));
	free(toTestS);
	BT_ASSERT(strcmp(subtype, "deposit") == 0);
	free(subtype);
}

void TestTPTypes::test_tptypes_null_subtype() {
	btlogger((char*) "test_tptypes_null_subtype");
	m_allocated = ::tpalloc((char*) "X_COMMON", (char*) "deposit", 0);
	BT_ASSERT(m_allocated != NULL);

	char* type = (char*) malloc(8);
	int toTest = ::tptypes(m_allocated, type, NULL);
	BT_ASSERT(tperrno == 0);
	char* toTestS = (char*) malloc(110);
	sprintf(toTestS, "%d", toTest);
	BT_ASSERT_MESSAGE(toTestS, toTest >= (int) sizeof(DEPOSIT));
	free(toTestS);
	BT_ASSERT(strncmp(type, "X_COMMON", 8) == 0);
	free(type);
}

void TestTPTypes::test_tptypes_max_type() {
	btlogger((char*) "test_tptypes_max_type");
	m_allocated = ::tpalloc((char*) "X_COMMON", (char*) "deposit", 0);
	BT_ASSERT(m_allocated != NULL);

	char* type = (char*) malloc(8);
	int toTest = ::tptypes(m_allocated, type, NULL);
	BT_ASSERT(tperrno == 0);
	char* toTestS = (char*) malloc(110);
	sprintf(toTestS, "%d", toTest);
	BT_ASSERT_MESSAGE(toTestS, toTest >= (int) sizeof(DEPOSIT));
	free(toTestS);
	BT_ASSERT(strncmp(type, "X_COMMON", 8) == 0);
	free(type);
}

void TestTPTypes::test_tptypes_max_subtype() {
	btlogger((char*) "test_tptypes_max_subtype");
	m_allocated = ::tpalloc((char*) "X_COMMON", (char*) "abcdefghijklmnop", 0);
	BT_ASSERT(m_allocated != NULL);

	char* subtype = (char*) malloc(16);
	int toTest = ::tptypes(m_allocated, NULL, subtype);
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(strncmp(subtype, "abcdefghijklmnop", 16) == 0);
	free(subtype);
	char* toTestS = (char*) malloc(110);
	sprintf(toTestS, "%d", toTest);
	BT_ASSERT_MESSAGE(toTestS, toTest >= 10);
	free(toTestS);
}

void TestTPTypes::test_tptypes_small_type() { // cannot be tested as we can't find how big the memory is
	btlogger((char*) "test_tptypes_small_type");
	m_allocated = ::tpalloc((char*) "X_COMMON", (char*) "deposit", 0);
	BT_ASSERT(m_allocated != NULL);

	char* type = (char*) malloc(7);
	int toTest = ::tptypes(m_allocated, type, NULL);
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(toTest == (int) sizeof(DEPOSIT));
	BT_ASSERT(strncmp(type, "X_COMMON", 8) == 0);
	BT_ASSERT(strncmp(type, "X_COMMO", 7) == 0);
	free(type);
}

void TestTPTypes::test_tptypes_small_subtype() { // cannot be tested as we can't find how big the memory is
	btlogger((char*) "test_tptypes_small_subtype");
	m_allocated = ::tpalloc((char*) "X_COMMON", (char*) "abcdefghijklmnop", 0);
	BT_ASSERT(m_allocated != NULL);

	char* subtype = (char*) malloc(15);
	int toTest = ::tptypes(m_allocated, NULL, subtype);
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(strncmp(subtype, "abcdefghijklmnop", 16) != 0);
	BT_ASSERT(strncmp(subtype, "abcdefghijklmno", 15) == 0);
	BT_ASSERT(toTest == 10);
	free(subtype);
}

void TestTPTypes::test_tptypes_large_type() {
	btlogger((char*) "test_tptypes_large_type");

	m_allocated = ::tpalloc((char*) "X_COMMON", (char*) "deposit", 0);
	BT_ASSERT(m_allocated != NULL);

	char* type = (char*) malloc(9);
	int toTest = ::tptypes(m_allocated, type, NULL);
	BT_ASSERT(tperrno == 0);
	char* toTestS = (char*) malloc(110);
	sprintf(toTestS, "%d", toTest);
	BT_ASSERT_MESSAGE(toTestS, toTest >= (int) sizeof(DEPOSIT));
	free(toTestS);
	BT_ASSERT(strncmp(type, "X_COMMON", 8) == 0);
	free(type);
}

void TestTPTypes::test_tptypes_large_subtype() {
	btlogger((char*) "test_tptypes_large_subtype");
	m_allocated = ::tpalloc((char*) "X_COMMON", (char*) "abcdefghijklmnop", 0);
	BT_ASSERT(m_allocated != NULL);

	char* subtype = (char*) malloc(17);
	int toTest = ::tptypes(m_allocated, NULL, subtype);
	BT_ASSERT(tperrno == 0);
	char* toTestS = (char*) malloc(110);
	sprintf(toTestS, "%d", toTest);
	BT_ASSERT_MESSAGE(toTestS, toTest >= 10);
	free(toTestS);
	BT_ASSERT(strncmp(subtype, "abcdefghijklmnop", 16) == 0);
	free(subtype);
}

CPPUNIT_TEST_SUITE_REGISTRATION( TestTPTypes);

