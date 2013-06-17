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
#ifndef TestTPTypes_H
#define TestTPTypes_H

#include <cppunit/extensions/HelperMacros.h>
#include <cppunit/TestFixture.h>

#include "BaseTest.h"

class TestTPTypes: public BaseTest {
	CPPUNIT_TEST_SUITE( TestTPTypes);
	CPPUNIT_TEST( test_tptypes_x_octet);
	CPPUNIT_TEST( test_tptypes_x_common);
	CPPUNIT_TEST( test_tptypes_x_c_type);
	CPPUNIT_TEST( test_tptypes_x_common_bigdata);
	CPPUNIT_TEST( test_tptypes_x_c_type_bigdata);
	CPPUNIT_TEST( test_tptypes_null_ptr);
	CPPUNIT_TEST( test_tptypes_null_type);
	CPPUNIT_TEST( test_tptypes_null_subtype);
	CPPUNIT_TEST( test_tptypes_max_type);
	CPPUNIT_TEST( test_tptypes_max_subtype);
	//CPPUNIT_TEST( test_tptypes_small_type); // cannot be tested as we can't find how big the memory is
	//CPPUNIT_TEST( test_tptypes_small_subtype); // cannot be tested as we can't find how big the memory is
	CPPUNIT_TEST( test_tptypes_large_type);
	CPPUNIT_TEST( test_tptypes_large_subtype);
	CPPUNIT_TEST( test_tptypes_unallocated);
CPPUNIT_TEST_SUITE_END();
public:
void test_tptypes_x_octet();
void test_tptypes_x_common();
void test_tptypes_x_c_type();
void test_tptypes_x_common_bigdata();
void test_tptypes_x_c_type_bigdata();
void test_tptypes_null_ptr();
void test_tptypes_null_type();
void test_tptypes_null_subtype();
void test_tptypes_max_type();
void test_tptypes_max_subtype();
void test_tptypes_small_type();
void test_tptypes_small_subtype();
void test_tptypes_large_type();
void test_tptypes_large_subtype();
void test_tptypes_unallocated();
virtual void setUp();
virtual void tearDown();
private:
char * m_allocated;
};

#endif
