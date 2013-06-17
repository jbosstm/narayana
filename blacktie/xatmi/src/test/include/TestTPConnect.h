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
#ifndef TestTPConnect_H
#define TestTPConnect_H

#include <cppunit/extensions/HelperMacros.h>
#include <cppunit/TestFixture.h>

#include "BaseServerTest.h"

class TestTPConnect: public BaseServerTest {
	CPPUNIT_TEST_SUITE( TestTPConnect);
	CPPUNIT_TEST( test_tpconnect);
	CPPUNIT_TEST( test_tpconnect_nodata);
	CPPUNIT_TEST( test_tpconnect_double_connect);
	CPPUNIT_TEST( test_tpacall_to_TPCONV_fails);
	CPPUNIT_TEST( test_tpconnect_tpgetrply);CPPUNIT_TEST_SUITE_END();

public:
	void test_tpconnect();
	void test_tpconnect_double_connect();
	void test_tpconnect_nodata();
	void test_tpacall_to_TPCONV_fails();
	void test_tpconnect_tpgetrply();
	virtual void setUp();
	virtual void tearDown();
private:
	int cd;
	int cd2;
	char *sendbuf, *rcvbuf;
	long sendlen, rcvlen;
};

#endif
