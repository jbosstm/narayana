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


#ifndef AtmiBroker_CLIENT_H_
#define AtmiBroker_CLIENT_H_


#include "atmiBrokerXatmiMacro.h"

#include <vector>

#include "Connection.h"
#include "Session.h"
#include "ConnectionManager.h"
#include "SynchronizableObject.h"
#include "AtmiBrokerEnv.h"

class AtmiBrokerClient {
public:
	AtmiBrokerClient(AtmiBrokerSignalHandler& handler);
	virtual ~AtmiBrokerClient();
	Session* createSession(bool isConv, int& id, char* serviceName);
	BLACKTIE_XATMI_DLL Session* getQueueSession();
	Session* getSession(int id);
	void closeSession(int id);
	void disconnectSessions();

protected:
	Connection* currentConnection;
	//std::map<std::string, Connection*> clientConnectionMap;
	ConnectionManager clientConnectionManager;
	SynchronizableObject* lock;
	AtmiBrokerSignalHandler& signalHandler;
};

// CLIENT
extern BLACKTIE_XATMI_DLL AtmiBrokerClient* ptrAtmiBrokerClient;

// Required for extensions to handle sending buffers
extern BLACKTIE_XATMI_DLL int bufferSize(char* data, int suggestedSize);
extern BLACKTIE_XATMI_DLL int send(Session* session, const char* replyTo, char* idata, long ilen,
		int correlationId, long flags, long rval, MESSAGE& message, long rcode,
		int priority, long timeToLive, bool queue, char* queueName);
extern BLACKTIE_XATMI_DLL int convertMessage(MESSAGE &message, int len, char** odata, long* olen,
		long flags);


#endif
