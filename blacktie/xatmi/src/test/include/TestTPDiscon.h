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
#ifndef TestTPDiscon_H
#define TestTPDiscon_H

#include <cppunit/extensions/HelperMacros.h>
#include <cppunit/TestFixture.h>

#include "BaseServerTest.h"

class TestTPDiscon: public BaseServerTest {
	CPPUNIT_TEST_SUITE( TestTPDiscon);
	CPPUNIT_TEST( test_tpdiscon);
	CPPUNIT_TEST( test_tpdiscon_baddescr);
	CPPUNIT_TEST( test_tpdiscon_negdescr);
	CPPUNIT_TEST_SUITE_END();
public:
void test_tpdiscon();
void test_tpdiscon_baddescr();
void test_tpdiscon_negdescr();
virtual void setUp();
virtual void tearDown();
private:
int cd;
char *sendbuf;
long sendlen;
};

#endif
