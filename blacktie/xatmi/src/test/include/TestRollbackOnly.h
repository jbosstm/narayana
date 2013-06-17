/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc., and others contributors as indicated
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
#ifndef TestRollbackOnly_H
#define TestRollbackOnly_H

#include <cppunit/extensions/HelperMacros.h>
#include <cppunit/TestFixture.h>

#include "BaseServerTest.h"

class TestRollbackOnly: public BaseServerTest {
	CPPUNIT_TEST_SUITE( TestRollbackOnly);
	CPPUNIT_TEST( test_tpcall_TPETIME);
	CPPUNIT_TEST( test_tpcall_TPEOTYPE);
	CPPUNIT_TEST( test_tpcall_TPESVCFAIL);
	CPPUNIT_TEST( test_tprecv_TPEV_DISCONIMM);
	CPPUNIT_TEST( test_tprecv_TPEV_SVCFAIL);
	CPPUNIT_TEST( test_no_tpreturn);
	// TODO CPPUNIT_TEST( test_tpcall_TPESVCERR_openconnections);
	// TODO CPPUNIT_TEST( test_tprecv_TPEV_SVCERR_openconnections);
CPPUNIT_TEST_SUITE_END();

public:
	void test_tpcall_TPETIME();
	void test_tpcall_TPEOTYPE();
	void test_tpcall_TPESVCFAIL();
	void test_tprecv_TPEV_DISCONIMM();
	void test_tprecv_TPEV_SVCFAIL();
	void test_no_tpreturn();
	// TODO void test_tpcall_TPESVCERR_openconnections();
	// TODO void test_tprecv_TPEV_SVCERR_openconnections();
	virtual void setUp();
	virtual void tearDown();

private:
	char *sendbuf, *rcvbuf;
	long sendlen, rcvlen;
};

#endif
