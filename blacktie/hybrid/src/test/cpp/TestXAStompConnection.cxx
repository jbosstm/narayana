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

#include "TestXAStompConnection.h"

#include "btlogger.h"
#include "AtmiBrokerEnv.h"

#include "atmiBrokerTxMacro.h"
#ifdef __cplusplus
extern "C" {
#endif
extern BLACKTIE_TX_DLL int tx_begin(void);
extern BLACKTIE_TX_DLL int tx_close(void);
extern BLACKTIE_TX_DLL int tx_commit(void);
extern BLACKTIE_TX_DLL int tx_open(void);
extern BLACKTIE_TX_DLL int tx_rollback(void);
extern BLACKTIE_TX_DLL char* txx_serialize(long* ttl);
extern BLACKTIE_TX_DLL void txx_stop();
#ifdef __cplusplus
}
#endif

void messagesAvailableCallbackXA(int bar, bool remove) {

}

void TestXAStompConnection::setUp() {
	init_ace();
	btlogger("TestXAStompConnection::setUp");

	serverConnection = NULL;
	clientConnection = NULL;
	serverConnection = new HybridConnectionImpl((char*) "server",
			messagesAvailableCallbackXA);
	clientConnection = new HybridConnectionImpl((char*) "client",
			messagesAvailableCallbackXA);
}

void TestXAStompConnection::tearDown() {
	btlogger("TestXAStompConnection::tearDown");

	serverConnection->cleanupThread();

	if (serverConnection) {
		delete serverConnection;
	}

	txx_stop();

	if (clientConnection) {
		delete clientConnection;
	}
}

void TestXAStompConnection::test() {
	btlogger("TestXAStompConnection::test");

	int msgCount = 2;

	Session* client = clientConnection->getQueueSession();

	tx_open();
	tx_begin();

	// THIS IS THE INITIAL SEND
	for (int i = 0; i < msgCount; i++) {
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
		clientSend.ttl = 120 * 1000;
		clientSend.schedtime = 0;
		clientSend.control = NULL;
		long discardTxTTL = -1;
		clientSend.xid = txx_serialize(&discardTxTTL);
		clientSend.syncRcv = 0;
		BT_ASSERT(client->send((char*) "JAVA_Converse", clientSend));
		free (clientData);
        	free (clientSend.xid);
		btlogger("TestXAStompConnection::test sent message");
	}

	tx_commit();
	tx_close();

	btlogger("TestXAStompConnection::test starting to receive (rollback)");
	tx_open();
	tx_begin();
	btlogger("Iterating");
	for (int i = 0; i < msgCount; i++) {
		MESSAGE message;
		message.replyto = NULL;
		message.correlationId = -1;
		message.data = NULL;
		message.len = 0;
		message.priority = 0;
		message.flags = -1;
		message.control = NULL;
		message.rval = -1;
		message.rcode = -1;
		message.type = NULL;
		message.subtype = NULL;
		message.received = false;
		message.ttl = -1;
		message.schedtime = 0;
		message.serviceName = NULL;
		message.messageId = NULL;
		message.syncRcv = 1;
		long discardTxTTL = -1;
		message.xid = txx_serialize(&discardTxTTL);
		BT_ASSERT(client->send((char*) "JAVA_Converse", message));
		BT_ASSERT(message.received);
		BT_ASSERT(strncmp(message.data, "hello", 5) == 0);
		free(message.data);
		btlogger("TestXAStompConnection::test received message");
	}
	tx_rollback();
	tx_close();


	btlogger("TestXAStompConnection::test starting to receive (commit)");
	tx_open();
	tx_begin();
	btlogger("Iterating");
	for (int i = 0; i < msgCount; i++) {
		MESSAGE message;
		message.replyto = NULL;
		message.correlationId = -1;
		message.data = NULL;
		message.len = 0;
		message.priority = 0;
		message.flags = -1;
		message.control = NULL;
		message.rval = -1;
		message.rcode = -1;
		message.type = NULL;
		message.subtype = NULL;
		message.received = false;
		message.ttl = -1;
		message.schedtime = 0;
		message.serviceName = NULL;
		message.messageId = NULL;
		message.syncRcv = 1;
		long discardTxTTL = -1;
		message.xid = txx_serialize(&discardTxTTL);
		message.control = NULL;
		BT_ASSERT(client->send((char*) "JAVA_Converse", message));
		BT_ASSERT(message.received);
		BT_ASSERT(strncmp(message.data, "hello", 5) == 0);
		free(message.data);
		free(message.xid);
		btlogger("TestXAStompConnection::test received message");
	}
	tx_commit();
	tx_close();

	btlogger("Iterated");
}
