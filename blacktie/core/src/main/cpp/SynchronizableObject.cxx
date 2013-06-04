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

#include "SynchronizableObject.h"

#include "apr_strings.h"

log4cxx::LoggerPtr SynchronizableObject::logger(log4cxx::Logger::getLogger(
		"SynchronizableObject"));

SynchronizableObject::SynchronizableObject() 
	{
	waitingCount = 0;
	notifiedCount = 0;
// NOT SAFE ON FEDORA 15 either
//#ifndef WIN32
	//LOG4CXX_DEBUG(logger, (char*) "SynchronizableObject created: " << this);
//#endif
#ifdef WIN32
	apr_initialize();
#endif
	apr_pool_create(&pool,NULL);

        apr_thread_cond_create(&cond, pool);

	apr_thread_mutex_create(&mutex, APR_THREAD_MUTEX_NESTED, pool);
}

SynchronizableObject::~SynchronizableObject() {
	//BLACKTIE-339 LOG4CXX_DEBUG(logger, (char*) "SynchronizableObject destroyed: " << this);

	apr_thread_cond_destroy(cond);

	apr_thread_mutex_destroy(mutex);

        apr_pool_destroy(pool);
}

bool SynchronizableObject::lock() {
	LOG4CXX_TRACE(logger, (char*) "Acquiring mutex: " << this);
	bool toReturn = (apr_thread_mutex_lock(mutex) == APR_SUCCESS);
	LOG4CXX_TRACE(logger, (char*) "acquired: " << this);
	return toReturn;
}

bool SynchronizableObject::wait(long timeout) {
	LOG4CXX_TRACE(logger, (char*) "Waiting for cond: " << this);
	waitingCount++;
	bool toReturn = false;
	if (timeout > 0) {
		toReturn = apr_thread_cond_timedwait(cond, mutex, apr_time_from_sec(timeout));
	} else {
		LOG4CXX_TRACE(logger, (char*) "Blocking wait: " << this);
		toReturn = apr_thread_cond_wait(cond, mutex);
	}
	waitingCount--;
	if (notifiedCount > 0) {
		notifiedCount--;
	}
	LOG4CXX_TRACE(logger, (char*) "waited: " << this);
	return toReturn;
}

bool SynchronizableObject::notify() {
	LOG4CXX_TRACE(logger, (char*) "Notifying cond: " << this);
	bool toReturn = false;
	if (notifiedCount < waitingCount) {
		toReturn = apr_thread_cond_signal(cond);
		notifiedCount++;
		LOG4CXX_TRACE(logger, (char*) "notified: " << this);
	} else {
		LOG4CXX_TRACE(logger, (char*) "no waiters: " << this);
	}
	return toReturn;
}

bool SynchronizableObject::notifyAll() {
	LOG4CXX_TRACE(logger, (char*) "Notifying All cond: " << this);
	bool toReturn = apr_thread_cond_broadcast(cond);
	LOG4CXX_TRACE(logger, (char*) "All notified: " << this);
	return toReturn;
}

bool SynchronizableObject::unlock() {
	LOG4CXX_TRACE(logger, (char*) "Releasing mutex: " << this);
	bool toReturn = apr_thread_mutex_unlock(mutex);
	LOG4CXX_TRACE(logger, (char*) "released mutex: " << this);
	return toReturn;
}
