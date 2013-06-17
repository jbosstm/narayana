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
#ifndef TEST_BLACKTIE_XML_H
#define TEST_BLACKTIE_XML_H

#include <cppunit/extensions/HelperMacros.h>
#include <cppunit/TestFixture.h>

struct foo_t {
	float Balance2[3];
	long Balance;
	char accountName[2][10];
};
typedef struct foo_t FOO;

struct bar_t {
	int barlance[4];
	short barbq;
	int barlance1[4];
	short barbq2;
};
typedef struct bar_t BAR;

class TestAtmiBrokerXml: public CppUnit::TestFixture {
	CPPUNIT_TEST_SUITE( TestAtmiBrokerXml);
	CPPUNIT_TEST( test_env);
	CPPUNIT_TEST( test_define_adminservice);
	CPPUNIT_TEST( test_same_service);
	CPPUNIT_TEST_SUITE_END();

public:
	virtual void setUp();
	virtual void tearDown();
	void test_env();
	void test_define_adminservice();
	void test_same_service();
private:
	char* env;
};

#endif
