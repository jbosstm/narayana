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
#include "TestAssert.h"

#include "TestSynchronizableObject.h"

#include "btlogger.h"
#include "ace/OS_NS_unistd.h"

Waiter::Waiter() {
	object = new SynchronizableObject();
	object2 = new SynchronizableObject();
	notified = false;
}

Waiter::~Waiter() {
	delete object;
	delete object2;
}

SynchronizableObject* Waiter::getLock() {
	return object;
}

SynchronizableObject* Waiter::getLock2() {
	return object2;
}

bool Waiter::getNotified() {
	return notified;
}

int Waiter::svc(void){
	object2->lock();
	object->lock();
	btlogger("waiting");
	object->wait(10);
	btlogger("waited");
	notified = true;
	object->unlock();
	btlogger("svc done");
	object2->unlock();
	return 0;
}

void TestSynchronizableObject::setUp() {
	int argc = 0;
	init_ace();
	orbRef = CORBA::ORB_init(argc, NULL, "null");
	waiter = new Waiter();
	if (waiter->activate(THR_NEW_LWP| THR_JOINABLE, 1, 0, ACE_DEFAULT_THREAD_PRIORITY, -1, 0, 0, 0, 0, 0, 0) != 0) {
		delete (waiter);
		waiter = NULL;
		BT_FAIL("COULD NOT CREATE WAITER");
	}
}
void TestSynchronizableObject::tearDown() {
	if (waiter) {
		waiter->wait();
		delete waiter;
		waiter = NULL;
	}
	if (!CORBA::is_nil(orbRef))
		orbRef->shutdown(1);
	if (!CORBA::is_nil(orbRef))
		orbRef->destroy();

	orbRef = NULL;
}

void TestSynchronizableObject::testWaitNotify() {

	ACE_OS::sleep(1);
	SynchronizableObject* lock = waiter->getLock();
	SynchronizableObject* lock2 = waiter->getLock2();
	lock->lock();
	lock->notify();
	lock->unlock();
	btlogger("done");
	lock2->lock();
	bool notified = waiter->getNotified();
	btlogger("got notified");
	lock2->unlock();
	btlogger("main done");
	BT_ASSERT_MESSAGE("Was not notified", notified == true);
}

void TestSynchronizableObject::testNotifyWaitWithTimeout() {
	ACE_OS::sleep(1);
	SynchronizableObject* lock = waiter->getLock();
	lock->lock();
	lock->notify();
	lock->unlock();
	lock->lock();
	btlogger("waiting for 3 seconds");
	lock->wait(3);
	btlogger("waited");
	lock->unlock();
	btlogger("main done");
}
