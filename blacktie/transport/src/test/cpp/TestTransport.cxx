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

#include <string>
#include <sstream>
#include <stdlib.h>

#include "apr_strings.h"

#include "TestAssert.h"
#include "TestTransport.h"
#include "ThreadLocalStorage.h"
#include "btlogger.h"

#include "HttpSessionImpl.h"

void TestTransport::setUp()
{
	//init_ace();

	apr_status_t rc = apr_initialize();
	BT_ASSERT (rc == APR_SUCCESS);

	TestFixture::setUp();
}

void TestTransport::tearDown()
{
	apr_terminate();
	TestFixture::tearDown();
}

static bool compare_strings(const char *msg, char *s1, char *s2) {
	if (s1 == s2 || (s1 != NULL && s2 != NULL && strcmp(s1, s2) == 0))
		return true;

	btlogger_debug("%s: %s != %s\n", msg, s1, s2);

	return false;
}
static bool compare_numbers(const char *msg, long v1, long v2) {
	if (v1 == v2)
		return true;

	btlogger_debug("%s: %ld != %ld\n", msg, v1, v2);

	return false;
}

static bool compare(MESSAGE &m1, MESSAGE &m2) {
	return
		compare_numbers("correlationId", m1.correlationId , m2.correlationId)
		&& compare_numbers("priority", m1.priority , m2.priority)
		&& compare_numbers("rval", m1.rval , m2.rval)
		&& compare_numbers("rcode", m1.rcode , m2.rcode)
		&& compare_numbers("len", m1.len , m2.len)
		&& compare_numbers("flags", m1.flags , m2.flags)
		&& compare_numbers("ttl", m1.ttl , m2.ttl)

		&& compare_strings("replyto", (char*) (m1.replyto), (char*) (m2.replyto))
		&& compare_strings("data", m1.data, m2.data)
		&& compare_strings("control", (char*) m1.control, (char*) m2.control)
		&& compare_strings("xid", (char*) m1.xid, (char*) m2.xid)
		&& compare_strings("type", m1.type, m2.type)
		&& compare_strings("subtype", m1.subtype, m2.subtype)
		&& compare_strings("serviceName", m1.serviceName, m2.serviceName)
		&& compare_strings("messageId", m1.messageId, m2.messageId);
}

static void free_message(MESSAGE &msg) {
	if (msg.replyto != NULL)
		free((char *) msg.replyto);
	if (msg.data!= NULL)
		free(msg.data);
	if (msg.control!= NULL)
		free(msg.control);
	if (msg.xid!= NULL)
		free(msg.xid);
	if (msg.type!= NULL)
		free(msg.type);
	if (msg.subtype!= NULL)
		free(msg.subtype);
	if (msg.serviceName!= NULL)
		free(msg.serviceName);
	if (msg.messageId!= NULL)
		free(msg.messageId);
}

void TestTransport::test_basic()
{
	btlogger_debug("TestTransport::test_basic begin");
	int i;
	int cnt = 2;
	const char *qname = "http://localhost:8080/blacktie-messaging-5.0.5.Final-SNAPSHOT/queues/jms.queue.testQueue";
	
	HttpSessionImpl s1(qname);

	if ((i = s1.status()) != 0) {
		btlogger("Skipping HTTP messaging tests. Reason: service not available %d (" \
			" Did you start the Hornetq REST API)\n", i);
        return;
    }

	s1.startConsumer();

	MESSAGE smsgs[] = {
		{"replyto_Q1", 101101, 99, (char*) "MESSAGE 1", (void*) NULL, (void *) "xid1",
			0, -2L, 6, 802L, 100L, 0,
			(char *) "type1", (char *) "subtype1", true, (char *) "serviceName1", (char *) "messageId1", false},
		{"replyto_Q2", 101101, 99, (char*) "MESSAGE 2", (void*) NULL, (void *) NULL,
			0, -2L, 6, 802L, 100L, 0,
			(char *) "type2", (char *) "subtype2", true, (char *) "serviceName2", (char *) "messageId2", false},
		{"replyto_Q3", 101101, 99, (char*) "MESSAGE 3", (void*) NULL, (void *) NULL,
			0, -2L, 6, 802L, 100L, 0,
			(char *) "type3", (char *) "subtype3", true, (char *) "serviceName3", (char *) "messageId3", false},
	};
	MESSAGE rmsgs[3];

	HttpSessionImpl s2(qname);

	for (i = 0; i < cnt; i++) {
		btlogger_debug("SENDING %s\n", smsgs[i].data);
		(void) s2.send(smsgs[i]);
	}

	for (i = 0; i < cnt; i++) {
		rmsgs[i] = s1.receive(1L);
		btlogger_debug("RECEIVED %s\n", rmsgs[i].data);
	}

	s1.stopConsumer();

	for (i = 0; i < cnt; i++)
		if (rmsgs[i].rcode != -1) {
			bool ok = compare(smsgs[i], rmsgs[i]);
			btlogger_debug("==== %d\n", ok);
			BT_ASSERT(ok);
			free_message(rmsgs[i]);
		}

	btlogger_debug("TestTransport::test_basic pass");
}
