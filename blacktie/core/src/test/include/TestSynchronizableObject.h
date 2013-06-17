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
#ifndef TestSynchronizableObject_H
#define TestSynchronizableObject_H

#include <cppunit/extensions/HelperMacros.h>
#include <cppunit/TestFixture.h>
#include <ace/Task.h>
#include <tao/ORB.h>
#include "SynchronizableObject.h"
#include "btlogger.h"

class Waiter: public ACE_Task_Base {
public:
	Waiter();
	~Waiter();
	int svc() ;
	SynchronizableObject* getLock();
	SynchronizableObject* getLock2();
	bool getNotified();
private:
	SynchronizableObject* object;
	SynchronizableObject* object2;
	bool notified;
};

class TestSynchronizableObject: public CppUnit::TestFixture {
CPPUNIT_TEST_SUITE( TestSynchronizableObject)
	;
		CPPUNIT_TEST( testWaitNotify);
		CPPUNIT_TEST( testNotifyWaitWithTimeout);
	CPPUNIT_TEST_SUITE_END()
	;

public:
	void setUp();
	void tearDown();
	void testWaitNotify();
	void testNotifyWaitWithTimeout();
private:
	Waiter* waiter;
	CORBA::ORB_var orbRef;
};

#endif
