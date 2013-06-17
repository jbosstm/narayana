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
#ifndef TestPBF_H
#define TestPBF_H

#include <cppunit/extensions/HelperMacros.h>
#include <cppunit/TestFixture.h>

#include "BaseServerTest.h"

struct acct_info_t {
	long acct_no; // 8
	char name[50]; // 50
	char address[100]; // 100
	float foo[2]; // 4 * 2
	double balances[2]; // 8 * 2
};
typedef struct acct_info_t ACCT_INFO;

class TestPBF: public BaseServerTest {
	CPPUNIT_TEST_SUITE( TestPBF);
	CPPUNIT_TEST( test_tpalloc);
	CPPUNIT_TEST( test_tpalloc_nonzero);
	CPPUNIT_TEST( test_tpalloc_subtype_required);
	CPPUNIT_TEST( test_tprealloc);
	CPPUNIT_TEST( test_tptypes);
	CPPUNIT_TEST( test_tpfree);
	CPPUNIT_TEST( test_tpcall);CPPUNIT_TEST_SUITE_END();
public:
	void test_tpalloc();
	void test_tpalloc_nonzero();
	void test_tpalloc_subtype_required();
	void test_tpalloc_wrong_subtype();
	void test_tprealloc();
	void test_tptypes();
	void test_tpfree();
	void test_tpcall();

	virtual void setUp();
	virtual void tearDown();
private:
	char *m_allocated, *sendbuf, *rcvbuf;
};

#endif
