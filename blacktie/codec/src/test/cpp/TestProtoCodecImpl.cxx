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

#include "TestProtoCodecImpl.h"
#include "Codec.h"
#include "CodecFactory.h"
#include "AtmiBrokerEnv.h"
#include "btlogger.h"

#include "malloc.h"
#include "person.pb.h"

using namespace org::jboss::blacktie::proto;

void TestProtoCodecImpl::setUp() {
	init_ace();
	AtmiBrokerEnv::get_instance();

	// Perform global set up
	TestFixture::setUp();
}

void TestProtoCodecImpl::tearDown() {
	// Perform clean up
	AtmiBrokerEnv::discard_instance();

	// Perform global clean up
	TestFixture::tearDown();
}

void TestProtoCodecImpl::test_encode() {
	btlogger("TestProtoCodecImpl::test_encode");

	GOOGLE_PROTOBUF_VERIFY_VERSION;

	Person person;
	person.set_name("John Doe");
	person.set_id(1234);
	person.set_email("jdoe@example.com");

	// encode to myfile
	fstream output("myfile", ios::out | ios::binary);
	if (!person.SerializeToOstream(&output)) {
		BT_FAIL("searialize to file fail");
	}

	google::protobuf::ShutdownProtobufLibrary();
}

void TestProtoCodecImpl::test_decode() {
	btlogger("TestProtoCodecImpl::test_decode");

	GOOGLE_PROTOBUF_VERIFY_VERSION;

	Person person;
	// decode from myfile
	fstream input("myfile", ios::in | ios::binary);
	if (!person.ParseFromIstream(&input)) {
		BT_FAIL("paser from file fail");
	} else {
		BT_ASSERT(person.name().compare("John Doe") == 0);
		BT_ASSERT(person.id() == 1234);
		BT_ASSERT(person.email().compare("jdoe@example.com") == 0);
	}

	google::protobuf::ShutdownProtobufLibrary();
}
