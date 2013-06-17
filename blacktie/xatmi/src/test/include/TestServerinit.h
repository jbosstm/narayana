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
#ifndef TestServerinit_H
#define TestServerinit_H

#include <cppunit/extensions/HelperMacros.h>
#include <cppunit/TestFixture.h>
#include "xatmi.h"
#include "btserver.h"
#include "BaseTest.h"

class TestServerinit: public BaseTest {
	CPPUNIT_TEST_SUITE( TestServerinit);
	CPPUNIT_TEST( test_serverinit);
	CPPUNIT_TEST( test_config_cmdline);
	CPPUNIT_TEST( test_requires_id);
	CPPUNIT_TEST( test_config_env);
	CPPUNIT_TEST_SUITE_END();

public:
	void test_serverinit();
	void test_config_cmdline();
	void test_requires_id();
	void test_config_env();
};

#endif
