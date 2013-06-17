/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc., and others contributors as indicated
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
#ifndef TESTTPCAllTX_H
#define TESTTPCALLTX_H

#include <cppunit/extensions/HelperMacros.h>
#include <cppunit/TestFixture.h>

#include "BaseServerTest.h"

class TestTxTPCall: public BaseServerTest {
	CPPUNIT_TEST_SUITE( TestTxTPCall);
	CPPUNIT_TEST(test_timeout_with_tx);
	CPPUNIT_TEST(test_tpcall_without_tx);
	CPPUNIT_TEST(test_tpcall_with_tx);
	CPPUNIT_TEST(test_tpcancel_with_tx);
	CPPUNIT_TEST(test_timeout_no_tx);
	CPPUNIT_TEST_SUITE_END();

public:
	void test_tpcall_without_tx();
	void test_tpcall_with_tx();
	void test_tpcancel_with_tx();
	void test_timeout_no_tx();
	void test_timeout_with_tx();
	virtual void setUp();
	virtual void tearDown();

private:
	char *sendbuf, *rcvbuf;
	long sendlen, rcvlen;
};

#endif
