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

#include "TestStompConnection.h"

#include "btlogger.h"
#include "AtmiBrokerEnv.h"

#include <stdlib.h>

void messagesAvailableCallback(int bar, bool remove) {

}

void TestStompConnection::setUp() {
	btlogger("TestStompConnection::setUp");

	serverConnection = NULL;
	clientConnection = NULL;
	serverConnection = new HybridConnectionImpl((char*) "server",
			messagesAvailableCallback);
	clientConnection = new HybridConnectionImpl((char*) "client",
			messagesAvailableCallback);
}

void TestStompConnection::tearDown() {
	btlogger("TestStompConnection::tearDown");
	if (serverConnection) {
		delete serverConnection;
	}
	if (clientConnection) {
		delete clientConnection;
	}
}

void TestStompConnection::testLibStomp() {
	btlogger("TestStompConnection::testLibStomp");

	Destination* destination = serverConnection->createDestination(
			(char*) "JAVA_Converse", false, (char*) "queue");

	// THIS IS THE INITIAL EXCHANCE
	btlogger("Iterating (takes about 16 seconds on toms machine)");
	for (int i = 0; i < 50; i++) {
		Session* client = clientConnection->createSession(false,
				(char*) "JAVA_Converse");
		MESSAGE clientSend;
		char* clientData = (char*) malloc(6);
		memset(clientData, '\0', 6);
		strcpy(clientData, "hello");
		clientSend.data = clientData;
		clientSend.correlationId = 0;
		clientSend.flags = 0;
		clientSend.priority = 0;
		clientSend.len = 5;
		clientSend.rval = 0;
		clientSend.rcode = 0;
		clientSend.replyto = client->getReplyTo();
		clientSend.type = (char*) "X_OCTET";
		clientSend.subtype = (char*) "";
		clientSend.ttl = 20 * 1000;
		clientSend.schedtime = 0;
		clientSend.control = NULL;
		clientSend.xid = NULL;
		clientSend.syncRcv = 0;
		client->send(clientSend);
		MESSAGE serviceReceived = destination->receive(0);
		BT_ASSERT(serviceReceived.received == true);
		BT_ASSERT(strcmp(clientSend.type, serviceReceived.type) == 0);
		BT_ASSERT(clientSend.len == serviceReceived.len);
		destination->ack(serviceReceived);
		free(clientData);
		free(serviceReceived.data);
		//		free(serviceReceived.type); - STOMP ALLOCATED - MUST NOT FREE
		//		free(serviceReceived.subtype); - STOMP ALLOCATED - MUST NOT FREE
		clientConnection->closeSession(client->getId());
	}

	btlogger("Iterated");
	serverConnection->destroyDestination(destination);
}

void TestStompConnection::test() {
	btlogger("TestStompConnection::test");

	Destination* destination = serverConnection->createDestination(
			(char*) "JAVA_Converse", false, (char*) "queue");
	Session* client = clientConnection->createSession(false,
			(char*) "JAVA_Converse");

	// THIS IS THE INITIAL EXCHANCE
	const char* clientAddress = NULL;
	{
		MESSAGE clientSend;
		char* clientData = (char*) malloc(6);
		memset(clientData, '\0', 6);
		strcpy(clientData, "hello");
		clientSend.data = clientData;
		clientSend.correlationId = 0;
		clientSend.flags = 0;
		clientSend.len = 5;
		clientSend.priority = 0;
		clientSend.rval = 0;
		clientSend.rcode = 0;
		clientSend.replyto = client->getReplyTo();
		clientSend.type = (char*) "X_OCTET";
		clientSend.subtype = (char*) "";
		clientSend.ttl = 10 * 1000;
		clientSend.schedtime = 0;
		clientSend.control = NULL;
		clientSend.xid = NULL;
		clientSend.syncRcv = 0;
		client->send(clientSend);
		MESSAGE serviceReceived = destination->receive(0);
		BT_ASSERT(serviceReceived.received == true);
		BT_ASSERT(strcmp(clientSend.type, serviceReceived.type) == 0);
		BT_ASSERT(clientSend.len == serviceReceived.len);
		destination->ack(serviceReceived);
		free(clientData);
		free(serviceReceived.data);
		//		free(serviceReceived.type); - STOMP ALLOCATED - MUST NOT FREE
		//		free(serviceReceived.subtype); - STOMP ALLOCATED - MUST NOT FREE
		clientAddress = serviceReceived.replyto;
	}

	Session* service = serverConnection->createSession(false, 1, clientAddress);
	btlogger("Iterating");
	int iterations = 100;
	for (int i = 0; i < iterations; i++) {
		MESSAGE serviceSend;
		char* serviceData = (char*) malloc(4);
		memset(serviceData, '\0', 4);
		strcpy(serviceData, "bye");
                serviceSend.schedtime = -1;
		serviceSend.data = serviceData;
		serviceSend.correlationId = 0;
		serviceSend.flags = 0;
		serviceSend.len = 3;
		serviceSend.priority = 0;
		serviceSend.rval = 0;
		serviceSend.rcode = 0;
		serviceSend.replyto = service->getReplyTo();
		serviceSend.type = (char*) "X_OCTET";
		serviceSend.subtype = (char*) "";
		serviceSend.control = NULL;
		serviceSend.schedtime = 0;
		serviceSend.xid = NULL;
		serviceSend.syncRcv = 0;
		serviceSend.serviceName = NULL;
		serviceSend.messageId = NULL;
		service->send(serviceSend);
		MESSAGE clientReceived = client->receive(0);
		BT_ASSERT(clientReceived.received == true);
		BT_ASSERT(strcmp(serviceSend.type, clientReceived.type) == 0);
		BT_ASSERT(serviceSend.len == clientReceived.len);
		free(serviceData);
		free(clientReceived.data);
		free((char*) clientReceived.replyto);
		free(clientReceived.type);
		free(clientReceived.subtype);

		MESSAGE clientSend;
		char* clientData = (char*) malloc(5);
		memset(clientData, '\0', 5);
		strcpy(clientData, "byte");
		clientSend.data = clientData;
		clientSend.correlationId = 0;
		clientSend.flags = 0;
		clientSend.rval = 0;
		clientSend.rcode = 0;
		clientSend.len = 4;
		clientSend.priority = 0;
		clientSend.ttl = 20 * 1000;
		clientSend.schedtime = 0;
		clientSend.replyto = client->getReplyTo();
		clientSend.type = (char*) "X_OCTET";
		clientSend.subtype = (char*) "";
		clientSend.xid = NULL;
		clientSend.control = NULL;
		clientSend.syncRcv = 0;
		clientSend.serviceName = NULL;
		clientSend.messageId = NULL;
		client->send(clientSend);
		MESSAGE serviceReceived = service->receive(0);
		BT_ASSERT(serviceReceived.received);
		BT_ASSERT(strcmp(clientSend.type, serviceReceived.type) == 0);
		BT_ASSERT(clientSend.len == serviceReceived.len);
		free(clientData);
		free(serviceReceived.data);
		free((char*) serviceReceived.replyto);
		free(serviceReceived.type);
		free(serviceReceived.subtype);
	}
	btlogger("Iterated");
	serverConnection->destroyDestination(destination);
}

CPPUNIT_TEST_SUITE_REGISTRATION( TestStompConnection);
