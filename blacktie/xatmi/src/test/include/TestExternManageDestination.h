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
	CPPUNIT_TEST( test_tpcall_with_service);
	CPPUNIT_TEST( test_tpcall_without_service);
#endif
	CPPUNIT_TEST_SUITE_END();
public:
	void test_tpcall_with_service();
	void test_tpcall_without_service();
	virtual void setUp();
	virtual void tearDown();
};

#endif
