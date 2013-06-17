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

#include "TestAtmiBrokerXml.h"
#include "AtmiBrokerEnvXml.h"
#include "AtmiBrokerEnv.h"
#include "ace/OS_NS_stdlib.h"
#include "ace/OS_NS_stdio.h"
#include "btlogger.h"

#include "malloc.h"
#include <string.h>
void TestAtmiBrokerXml::setUp() {
	init_ace();
	// Perform global set up
	TestFixture::setUp();
}

void TestAtmiBrokerXml::tearDown() {
	ACE_OS::putenv("BLACKTIE_CONFIGURATION_DIR=.");
	ACE_OS::putenv("BLACKTIE_CONFIGURATION=");
	// Perform global clean up
	TestFixture::tearDown();
}
void TestAtmiBrokerXml::test_env() {
	btlogger((char*) "RUNNING");
	ACE_OS::putenv("BLACKTIE_CONFIGURATION_DIR=xmltest");
	ACE_OS::putenv("BLACKTIE_CONFIGURATION=xmltest");
	AtmiBrokerEnv* env = AtmiBrokerEnv::get_instance();
	char* value;
	value = env->getenv((char*) "MYLIBTEST");
	BT_ASSERT(strcmp(value, "xmltestfoo.xmltest") == 0);

	value = orbConfig.opt;
	BT_ASSERT(strncmp(value, "-ORBInitRef NameService=corbaloc::", 34)
			== 0);
	BT_ASSERT(strcmp(domain, "fooapp") == 0);
	BT_ASSERT(xarmp != 0);

	BT_ASSERT(servers.size() == 2);
	ServerInfo* server = servers[1];
	BT_ASSERT(server != NULL);
	BT_ASSERT(strcmp(server->serverName, "foo") == 0);
	std::vector<ServiceInfo>* services = &server->serviceVector;
	BT_ASSERT(strcmp((*services)[0].serviceName, "BAR") == 0);
	BT_ASSERT(strcmp((*services)[0].serviceType, "queue") == 0);
	BT_ASSERT((*services)[0].coding_type == NULL);

	BT_ASSERT(strcmp((*services)[1].serviceName, "ECHO") == 0);
	BT_ASSERT(strcmp((*services)[1].serviceType, "topic") == 0);
	BT_ASSERT(strcmp((*services)[1].coding_type, "xml") == 0);

#ifdef WIN32
	BT_ASSERT(strcmp((*services)[0].transportLib, "atmibroker-hybrid.dll") == 0);
	BT_ASSERT(strcmp((*services)[1].transportLib, "atmibroker-hybrid.dll") == 0);
#else
	BT_ASSERT(strcmp((*services)[0].transportLib,
			"libatmibroker-hybrid.so") == 0);
	BT_ASSERT(strcmp((*services)[1].transportLib,
			"libatmibroker-hybrid.so") == 0);
#endif

	BT_ASSERT((*services)[0].poolSize == 5);
	BT_ASSERT(strcmp((*services)[0].function_name, "BAR") == 0);
	BT_ASSERT(strcmp((*services)[0].library_name, "libXMLTESTSERVICE.so") == 0);
	BT_ASSERT((*services)[0].advertised == true);
	BT_ASSERT((*services)[0].conversational == true);
	BT_ASSERT((*services)[0].externally_managed_destination == true);

	char* transport = env->getTransportLibrary((char*) "BAR");
#ifdef WIN32
	BT_ASSERT(strcmp(transport, "atmibroker-hybrid.dll") == 0);
#else
	BT_ASSERT(strcmp(transport, "libatmibroker-hybrid.so") == 0);
#endif

	BT_ASSERT(buffers.size() == 2);

	char* foo = (char*) "foo";
	Buffer* foob = buffers[foo];
	BT_ASSERT(strcmp(foob->name, "foo") == 0);
	BT_ASSERT(foob->wireSize == ((4 * 3) + 8 + (1 * 2 * 10)));
	BT_ASSERT(foob->memSize == sizeof(FOO));
	BT_ASSERT(foob->attributes.size() == 3);
	char* Balance2 = (char*) "Balance2";
	char* Balance = (char*) "Balance";
	char* accountName = (char*) "accountName";
	BT_ASSERT(strcmp(foob->attributes[Balance2]->id, "Balance2") == 0);
	BT_ASSERT(strcmp(foob->attributes[Balance2]->type, "float[]") == 0);
	BT_ASSERT(foob->attributes[Balance2]->length == 3);
	BT_ASSERT(foob->attributes[Balance2]->count == 0);
	BT_ASSERT(strcmp(foob->attributes[accountName]->id, "accountName")
			== 0);
	BT_ASSERT(strcmp(foob->attributes[accountName]->type, "char[][]") == 0);
	BT_ASSERT(foob->attributes[accountName]->length == 10);
	BT_ASSERT(foob->attributes[accountName]->count == 2);
	BT_ASSERT(strcmp(foob->attributes[Balance]->id, "Balance") == 0);
	BT_ASSERT(strcmp(foob->attributes[Balance]->type, "long") == 0);
	BT_ASSERT(foob->attributes[Balance]->count == 0);
	BT_ASSERT(foob->attributes[Balance]->length == 0);

	char* bar = (char*) "bar";
	Buffer* barb = buffers[bar];
	BT_ASSERT(strcmp(barb->name, "bar") == 0);
	BT_ASSERT(barb->wireSize == ((4 * 4) + (2) + (4 * 4) + (2)));
	BT_ASSERT(barb->memSize == sizeof(BAR));
	BT_ASSERT(barb->attributes.size() == 4);
	char* barlance = (char*) "barlance";
	char* barbq = (char*) "barbq";
	char* barlance1 = (char*) "barlance1";
	char* barbq2 = (char*) "barbq2";
	BT_ASSERT(strcmp(barb->attributes[barlance]->id, "barlance") == 0);
	BT_ASSERT(strcmp(barb->attributes[barlance]->type, "int[]") == 0);
	BT_ASSERT(barb->attributes[barlance]->length == 4);
	BT_ASSERT(barb->attributes[barlance]->count == 0);
	BT_ASSERT(strcmp(barb->attributes[barbq]->id, "barbq") == 0);
	BT_ASSERT(strcmp(barb->attributes[barbq]->type, "short") == 0);
	BT_ASSERT(barb->attributes[barbq]->count == 0);
	BT_ASSERT(barb->attributes[barbq]->length == 0);
	BT_ASSERT(strcmp(barb->attributes[barlance1]->id, "barlance1") == 0);
	BT_ASSERT(strcmp(barb->attributes[barlance1]->type, "int[]") == 0);
	BT_ASSERT(barb->attributes[barlance1]->length == 4);
	BT_ASSERT(barb->attributes[barlance1]->count == 0);
	BT_ASSERT(strcmp(barb->attributes[barbq2]->id, "barbq2") == 0);
	BT_ASSERT(strcmp(barb->attributes[barbq2]->type, "short") == 0);
	BT_ASSERT(barb->attributes[barbq2]->count == 0);
	BT_ASSERT(barb->attributes[barbq2]->length == 0);

	Buffers::iterator it;
	for (it = buffers.begin(); it != buffers.end(); ++it) {
		Buffer* buffer = it->second;
		btlogger((char*) "Buffer name: %s", buffer->name);
	}

	BT_ASSERT(cbConfig.port == 12345);
	AtmiBrokerEnv::discard_instance();

}

void TestAtmiBrokerXml::test_define_adminservice() {
	ACE_OS::putenv("BLACKTIE_CONFIGURATION_DIR=wrongtest");

	try {
		AtmiBrokerEnv::get_instance();
		AtmiBrokerEnv::discard_instance();
		BT_FAIL("CAN NOT DEFINE ADMIN SERVICE");
	} catch (std::exception& e) {
		btlogger((char*) "define admin services test ok");
	}
}

void TestAtmiBrokerXml::test_same_service() {
	ACE_OS::putenv("BLACKTIE_CONFIGURATION_DIR=sametest");

	try {
		AtmiBrokerEnv::get_instance();
		AtmiBrokerEnv::discard_instance();
		BT_FAIL("CAN NOT DEFINE SAME SERVICE IN DIFFERENT SERVER");
	} catch (std::exception& e) {
		btlogger((char*) "same services test ok");
	}
}
