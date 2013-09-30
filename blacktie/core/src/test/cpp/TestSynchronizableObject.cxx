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
#include "apr.h"
#include "TestAssert.h"

#include "TestSynchronizableObject.h"

#include "btlogger.h"

#include <apr_thread_proc.h>

static void* APR_THREAD_FUNC activateWaiter(apr_thread_t *thd, void* data)
{
    Waiter* waiter = (Waiter*) data;

    int ret = waiter->svc();

    apr_thread_exit(thd,APR_SUCCESS);

    return NULL;
}


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

        apr_initialize();

	waiter = new Waiter();

        apr_pool_t* mp;

        apr_pool_create(&mp, NULL);

        apr_thread_t* thd;
        if(apr_thread_create(&thd, NULL, activateWaiter, (void*)waiter, mp) != APR_SUCCESS) {
                        delete waiter;
                        btlogger( "Could not start thread pool");
        }


}
void TestSynchronizableObject::tearDown() {
	if (waiter) {
		delete waiter;
		waiter = NULL;
	}
}

void TestSynchronizableObject::testWaitNotify() {

	apr_sleep(apr_time_from_sec(1));
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
	apr_sleep(apr_time_from_sec(1));
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
