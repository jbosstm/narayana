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
#ifndef TestTPRealloc_H
#define TestTPRealloc_H

#include <cppunit/extensions/HelperMacros.h>
#include <cppunit/TestFixture.h>

#include "BaseTest.h"

class TestTPRealloc: public BaseTest {
	CPPUNIT_TEST_SUITE( TestTPRealloc);
	CPPUNIT_TEST( test_tprealloc_negative_x_octet);
	// THIS DOES NOT WORK AS YOU CANNOT REALLOC A ZERO BUFFER AS IT CANT BE FOUND
	//CPPUNIT_TEST( test_tprealloc_zero_x_octet);
	CPPUNIT_TEST( test_tprealloc_larger_x_octet);
	CPPUNIT_TEST( test_tprealloc_smaller_x_octet);
	CPPUNIT_TEST( test_tprealloc_samesize_x_octet);
	CPPUNIT_TEST( test_tprealloc_multi_x_octet);
	CPPUNIT_TEST( test_tprealloc_negative_x_common);
	CPPUNIT_TEST( test_tprealloc_zero_x_common);
	CPPUNIT_TEST( test_tprealloc_larger_x_common);
	CPPUNIT_TEST( test_tprealloc_smaller_x_common);
	CPPUNIT_TEST( test_tprealloc_samesize_x_common);
	CPPUNIT_TEST( test_tprealloc_multi_x_common);
	CPPUNIT_TEST( test_tprealloc_negative_x_c_type);
	CPPUNIT_TEST( test_tprealloc_zero_x_c_type);
	CPPUNIT_TEST( test_tprealloc_larger_x_c_type);
	CPPUNIT_TEST( test_tprealloc_smaller_x_c_type);
	CPPUNIT_TEST( test_tprealloc_samesize_x_c_type);
	CPPUNIT_TEST( test_tprealloc_multi_x_c_type);
	CPPUNIT_TEST( test_tprealloc_nonbuffer);
	CPPUNIT_TEST( test_tprealloc_null);
CPPUNIT_TEST_SUITE_END();
public:
void test_tprealloc_negative_x_octet();
void test_tprealloc_zero_x_octet();
void test_tprealloc_larger_x_octet();
void test_tprealloc_smaller_x_octet();
void test_tprealloc_samesize_x_octet();
void test_tprealloc_multi_x_octet();
void test_tprealloc_negative_x_common();
void test_tprealloc_zero_x_common();
void test_tprealloc_larger_x_common();
void test_tprealloc_smaller_x_common();
void test_tprealloc_samesize_x_common();
void test_tprealloc_multi_x_common();
void test_tprealloc_negative_x_c_type();
void test_tprealloc_zero_x_c_type();
void test_tprealloc_larger_x_c_type();
void test_tprealloc_smaller_x_c_type();
void test_tprealloc_samesize_x_c_type();
void test_tprealloc_multi_x_c_type();
void test_tprealloc_nonbuffer();
void test_tprealloc_null();
virtual void setUp();
virtual void tearDown();
private:
char* m_allocated;
char* m_nonallocated;
};

#endif
