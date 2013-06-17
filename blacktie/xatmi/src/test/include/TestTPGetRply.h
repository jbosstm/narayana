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
#ifndef TestTPGetRply_H
#define TestTPGetRply_H

#include <cppunit/extensions/HelperMacros.h>
#include <cppunit/TestFixture.h>

#include "BaseServerTest.h"

class TestTPGetRply: public BaseServerTest {
	CPPUNIT_TEST_SUITE( TestTPGetRply);
	CPPUNIT_TEST( test_tpgetrply);
	CPPUNIT_TEST( test_tpgetrply_with_TPNOBLOCK);
	CPPUNIT_TEST( test_tpgetrply_without_TPNOBLOCK);
	CPPUNIT_TEST( test_tpgetrply_baddesc);
	CPPUNIT_TEST( test_tpgetrply_nullcd);
	CPPUNIT_TEST( test_tpgetrply_nullrcvbuf);
	CPPUNIT_TEST( test_tpgetrply_nullrcvlen);
	CPPUNIT_TEST( test_tpgetrply_with_TPGETANY);
	CPPUNIT_TEST( test_tpgetrply_without_TPGETANY);CPPUNIT_TEST_SUITE_END();
public:
	void test_tpgetrply();
	void test_tpgetrply_without_TPNOBLOCK();
	void test_tpgetrply_with_TPNOBLOCK();
	void test_tpgetrply_baddesc();
	void test_tpgetrply_nullcd();
	void test_tpgetrply_nullrcvbuf();
	void test_tpgetrply_nullrcvlen();
	void test_tpgetrply_without_TPGETANY();
	void test_tpgetrply_with_TPGETANY();
	virtual void setUp();
	virtual void tearDown();

private:
	char *sendbuf, *rcvbuf;
	long sendlen, rcvlen;
	bool testingTPGETANY;
	int cd;
};

#endif
