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
#ifndef TestTPCall_H
#define TestTPCall_H

#include <cppunit/extensions/HelperMacros.h>
#include <cppunit/TestFixture.h>

#include "BaseServerTest.h"

class TestTPCall: public BaseServerTest {
	CPPUNIT_TEST_SUITE( TestTPCall);
	CPPUNIT_TEST( test_tpcall_unknown_service);
	CPPUNIT_TEST( test_tpcall_null_service);
	CPPUNIT_TEST( test_tpcall_without_TPNOCHANGE);
	CPPUNIT_TEST( test_tpcall_with_TPNOCHANGE);
	CPPUNIT_TEST( test_tpcall_without_TPNOBLOCK);
#ifndef SunOS
	CPPUNIT_TEST( test_tpcall_with_TPNOBLOCK);
#endif
	CPPUNIT_TEST( test_tpcall_without_TPNOTIME);
	CPPUNIT_TEST( test_tpcall_with_TPNOTIME);
// TODO THIS REQUIRES TESTS TO BE ABLE TO STOP THE NAMING SERVICE CPPUNIT_TEST( test_tpcall_systemerr);
	CPPUNIT_TEST( test_tpcall_x_octet);
	// THIS IS REMOVED AS IT IS AGAINST THE SPEC CPPUNIT_TEST( test_tpcall_x_octet_zero);
	CPPUNIT_TEST( test_tpcall_x_common);
	CPPUNIT_TEST( test_tpcall_x_c_type);
	// TODO THIS REQUIRES ME TO WORK OUT WHAT IT WAS ADDED FOR! CPPUNIT_TEST( test_tpcall_x_octet_lessdata);
CPPUNIT_TEST_SUITE_END();

public:
	void test_tpcall_unknown_service();
	void test_tpcall_null_service();
	void test_tpcall_systemerr();
	void test_tpcall_without_TPNOCHANGE();
	void test_tpcall_with_TPNOCHANGE();
	void test_tpcall_without_TPNOBLOCK();
	void test_tpcall_with_TPNOBLOCK();
	void test_tpcall_without_TPNOTIME();
	void test_tpcall_with_TPNOTIME();
	void test_tpcall_x_octet();
	void test_tpcall_x_octet_zero();
	void test_tpcall_x_common();
	void test_tpcall_x_c_type();
	void test_tpcall_x_octet_lessdata();
	virtual void setUp();
	virtual void tearDown();

private:
	char *sendbuf, *rcvbuf;
	long sendlen, rcvlen;
};

#endif
