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

#include "TestCodecImpl.h"
#include "Codec.h"
#include "CodecFactory.h"
#include "AtmiBrokerEnv.h"
#include "btlogger.h"
#include "btnbf.h"
#include "xatmi.h"

extern "C" {
#include "btclient.h"
}
#include "malloc.h"

void TestCodecImpl::setUp() {
	// Perform global set up
	TestFixture::setUp();
}

void TestCodecImpl::tearDown() {
	// Perform clean up
	::clientdone(0);
	// Perform global clean up
	TestFixture::tearDown();
}

void TestCodecImpl::testXMLCodec() {
	btlogger("TestCodecImpl::XMLCodec");
	char name[16];
	long id = 1234;
	int rc;

	char* employee = tpalloc((char*)"BT_NBF", (char*)"employee", 0);
	BT_ASSERT(employee != NULL);

	strcpy(name, "test");
	rc = btaddattribute(&employee, (char*)"name", name, strlen(name));	
	BT_ASSERT(rc == 0);

	rc = btaddattribute(&employee, (char*)"id", (char*)&id, sizeof(id));
	BT_ASSERT(rc == 0);

	CodecFactory factory;
	Codec* codec = factory.getCodec((char*)"xml");

	btlogger("BT_NBF encode");
	long size1 = tptypes(employee, NULL, NULL);
	long size2 = size1;
	char* data = codec->encode((char*)"BT_NBF", (char*)"employee", employee, &size1);
	//btlogger((char*)"%s", data);
	btlogger((char*)"size1 = %ld, size2 = %ld", size1, size2);
	BT_ASSERT(size1 == size2);

	tpfree(employee);
	delete[] data;
	factory.release(codec);
}
