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
#include <cppunit/TestFixture.h>

#include <tao/ORB.h>
#include "TestMultiOrb.h"
#include "AtmiBrokerPoaFac.h"
#include "OrbManagement.h"
#include "AtmiBrokerEnv.h"
#include "Worker.h"
#include "btlogger.h"

void TestMultiOrb::setUp() {
	init_ace();
	// Perform set up
	ACE_OS::putenv("BLACKTIE_CONFIGURATION_DIR=xmltest");

	// Perform global set up
	TestFixture::setUp();
}

void TestMultiOrb::tearDown() {
	// Perform clean up
	ACE_OS::putenv("BLACKTIE_CONFIGURATION_DIR=.");

	// Perform global clean up
	TestFixture::tearDown();
}

void TestMultiOrb::test() {
	try {
		CORBA_CONNECTION* serverConnection = initOrb((char*) "server");
		CORBA_CONNECTION* clientConnection = initOrb((char*) "client");
		shutdownBindings(serverConnection);
		serverConnection = NULL;
		shutdownBindings(clientConnection);
		clientConnection = NULL;
	} catch (CORBA::Exception &e) {
		BT_FAIL("COULDN'T CONNECT TO NAME SERVICE");
	}
}

void TestMultiOrb::test_manyorb() {
	for (int i = 0; i < 10; i++) {
		CORBA_CONNECTION* serverConnection = initOrb((char*) "server");
		AtmiBrokerPoaFac* serverPoaFactory = new AtmiBrokerPoaFac();
		PortableServer::POA_var server_poa = serverPoaFactory->createServerPoa(((CORBA::ORB_ptr) serverConnection->orbRef), "foo", ((PortableServer::POA_ptr) serverConnection->root_poa), ((PortableServer::POAManager_ptr) serverConnection->root_poa_manager));
		CORBA_CONNECTION* clientConnection = initOrb((char*) "client");

		shutdownBindings(serverConnection);
		serverConnection = NULL;
		shutdownBindings(clientConnection);
		clientConnection = NULL;

		delete serverPoaFactory;
	}
}
