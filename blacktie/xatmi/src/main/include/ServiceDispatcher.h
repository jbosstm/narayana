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

#ifndef SERVICEDISPATCHER_H_
#define SERVICEDISPATCHER_H_

#include <ace/Task.h>
#include "log4cxx/logger.h"
#include "xatmi.h"
#include "Destination.h"
#include "Connection.h"
#include "Session.h"
#include "AtmiBrokerServer.h"

class AtmiBrokerServer;

class ServiceDispatcher: public ACE_Task_Base {
public:
	ServiceDispatcher(AtmiBrokerServer* server, Destination* destination,
			Connection* connection, const char *serviceName, void(*func)(
					TPSVCINFO *), bool isPause,
			SynchronizableObject* reconnect, bool isConversational);
	~ServiceDispatcher();
	int svc();
	int pause();
	int resume();
	void shutdown();
	long getCounter();
	long getErrorCounter();
	void updateErrorCounter();
	void getResponseTime(unsigned long* min, unsigned long* avg,
			unsigned long* max);
	SynchronizableObject* getReconnect();
private:
	void onMessage(MESSAGE message);
	static log4cxx::LoggerPtr logger;
	AtmiBrokerServer* server;
	Destination* destination;
	Connection* connection;
	char* serviceName;
	void (*func)(TPSVCINFO *);
	Session* session;
	bool isadm;
	bool stop;
	bool isPause;
	bool isConversational;
	long timeout;
	long counter;
	long error_counter;
	unsigned long minResponseTime;
	unsigned long avgResponseTime;
	unsigned long maxResponseTime;
	SynchronizableObject* reconnect;
	SynchronizableObject* pauseLock;
	SynchronizableObject* stopLock;
};

#endif
