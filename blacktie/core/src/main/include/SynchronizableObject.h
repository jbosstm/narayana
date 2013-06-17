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
#ifndef SYNCHRONIZABLEOBJECT_H
#define SYNCHRONIZABLEOBJECT_H

#include "atmiBrokerCoreMacro.h"
#include "SynchronizableObject.h"
#include "log4cxx/logger.h"
#include <ace/Thread.h>
#include <ace/Synch.h>

class BLACKTIE_CORE_DLL SynchronizableObject {

public:
	SynchronizableObject();
	~SynchronizableObject();

	/*
	 * This method acquires a lock on the object in order to allow users to perform
	 * execution of code in a thread safe manner.
	 */
	bool lock();

	/**
	 * This code will wait to be notified or for the specified timeout interval.
	 * A timeout of zero (0) indicates wait until notified.
	 *
	 * lock MUST be called before executing this method
	 * unlock MUST be called after executing this method
	 */
	bool wait(long timeout);

	/**
	 * This code will wake up a single thread that is currently in the wait method.
	 *
	 * lock MUST be called before executing this method
	 * unlock MUST be called after executing this method
	 */
	bool notify();

	/**
	 * This code will wake up all threads that are in the wait method.
	 *
	 * lock MUST be called before executing this method
	 * unlock MUST be called after executing this method
	 */
	bool notifyAll();

	/**
	 * This method will release the lock held by the thread on this object
	 */
	bool unlock();
private:
	static log4cxx::LoggerPtr logger;
	ACE_Recursive_Thread_Mutex mutex;
	ACE_Condition<ACE_Recursive_Thread_Mutex> cond;
	int waitingCount;
	int notifiedCount;
};

#endif
