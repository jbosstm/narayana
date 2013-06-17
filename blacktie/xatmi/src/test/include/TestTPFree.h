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
#ifndef TestTPFree_H
#define TestTPFree_H

#include <cppunit/extensions/HelperMacros.h>
#include <cppunit/TestFixture.h>

#include "BaseTest.h"

class TestTPFree: public BaseTest {
	CPPUNIT_TEST_SUITE( TestTPFree);
	CPPUNIT_TEST( test_tpfree_alloc_x_octet);
	CPPUNIT_TEST( test_tpfree_realloc_x_octet);	
	CPPUNIT_TEST( test_tpfree_alloc_x_common);
	CPPUNIT_TEST( test_tpfree_realloc_x_common);
	CPPUNIT_TEST( test_tpfree_alloc_x_c_type);
	CPPUNIT_TEST( test_tpfree_realloc_x_c_type);
	CPPUNIT_TEST( test_tpfree_nonbuffer);
	// Once tpfree returns, ptr should not be passed as an argument to any XATMI routine
	/*
	CPPUNIT_TEST( test_tpfree_free_free_x_octet);
	CPPUNIT_TEST( test_tpfree_free_free_x_common);
	CPPUNIT_TEST( test_tpfree_free_free_x_c_type);
	*/
CPPUNIT_TEST_SUITE_END();
public:
void test_tpfree_alloc_x_octet();
void test_tpfree_realloc_x_octet();
void test_tpfree_free_free_x_octet();
void test_tpfree_alloc_x_common();
void test_tpfree_realloc_x_common();
void test_tpfree_free_free_x_common();
void test_tpfree_alloc_x_c_type();
void test_tpfree_realloc_x_c_type();
void test_tpfree_free_free_x_c_type();
void test_tpfree_nonbuffer();
virtual void setUp();
virtual void tearDown();
private:
char* m_allocated;
};

#endif
