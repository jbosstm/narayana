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
#ifndef _TESTTRANSACTIONS_H
#define _TESTTRANSACTIONS_H

#include <cppunit/extensions/HelperMacros.h>
#include "cppunit/TestFixture.h"
#include <string.h>

class TestTransactions: public CppUnit::TestFixture {
	CPPUNIT_TEST_SUITE(TestTransactions);
	CPPUNIT_TEST(test_rclog);           
	CPPUNIT_TEST(test_basic);
	CPPUNIT_TEST(test_transactions);
	CPPUNIT_TEST(test_protocol);
	CPPUNIT_TEST(test_info);
	CPPUNIT_TEST(test_timeout1);
	CPPUNIT_TEST(test_timeout2);
	CPPUNIT_TEST(test_rollback);
	CPPUNIT_TEST(test_hhazard);
	CPPUNIT_TEST(test_RM);
	CPPUNIT_TEST(test_RM_recovery_scan);
	CPPUNIT_TEST(test_tx_set);
	CPPUNIT_TEST(test_recovery); 
	CPPUNIT_TEST_SUITE_END();

public:
	void setUp();
	void tearDown();

	void test_rclog();
	void test_basic();
	void test_transactions();
	void test_protocol();
	void test_info();
	void test_RM();
	void test_RM_recovery_scan();
	void test_timeout1();
	void test_timeout2();
	void test_rollback();
	void test_hhazard();
	void test_register_resource();
	void test_tx_set();
	void test_recovery();
	void test_wait_for_recovery();
};

#endif
