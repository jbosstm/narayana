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
#ifndef TESTEXTERNMANAGEDESTINATION
#define TESTEXTERNMANAGEDESTINATION

#include <cppunit/extensions/HelperMacros.h>
#include <cppunit/TestFixture.h>

#include "BaseServerTest.h"

class TestExternManageDestination : public BaseServerTest {
	CPPUNIT_TEST_SUITE( TestExternManageDestination);
#ifndef SunOS
	CPPUNIT_TEST( test_stored_messages);
	CPPUNIT_TEST( test_stored_message_priority);
#ifdef WIN32
	//Disable test_stored_messsage_schedule on windows due to JBTM-2065
#else
    CPPUNIT_TEST( test_stored_message_schedule);
#endif

	CPPUNIT_TEST( test_btenqueue_with_txn_abort);
	CPPUNIT_TEST( test_btenqueue_with_txn_commit);
	CPPUNIT_TEST( test_btdequeue_with_txn_abort);
	CPPUNIT_TEST( test_btdequeue_with_txn_commit);
	CPPUNIT_TEST( test_btenqueue_with_tptypes);
#endif
	CPPUNIT_TEST_SUITE_END();
public:
	void test_stored_messages();
	void test_stored_message_priority();
	void test_stored_message_schedule();
	void test_btenqueue_with_txn_abort();
	void test_btenqueue_with_txn_commit();
	void test_btdequeue_with_txn_abort();
	void test_btdequeue_with_txn_commit();
	void test_btenqueue_with_tptypes();

	virtual void setUp();
	virtual void tearDown();
};

#endif
