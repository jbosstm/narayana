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

#include "TestDefaultCodecImpl.h"
#include "Codec.h"
#include "CodecFactory.h"
#include "AtmiBrokerEnv.h"
#include "btlogger.h"

#include "malloc.h"

void TestDefaultCodecImpl::setUp() {
	init_ace();
	AtmiBrokerEnv::get_instance();

	// Perform global set up
	TestFixture::setUp();
}

void TestDefaultCodecImpl::tearDown() {
	// Perform clean up
	AtmiBrokerEnv::discard_instance();

	// Perform global clean up
	TestFixture::tearDown();
}

void TestDefaultCodecImpl::test() {
	btlogger("TestDefaultCodecImpl::test");
	CodecFactory factory;
	Codec* codec = factory.getCodec(NULL);
	DEPOSIT* deposit = (DEPOSIT*) malloc(sizeof(DEPOSIT));
	deposit->acct_no = 1234567889;
	deposit->amount = 100;
	deposit->balance = 1.1;
	strcpy(
			deposit->status,
			"1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567");
	deposit->status_len = 127;

	long expectedWireSize = 148;
	long wireSize = -1;
	char* wireBuffer = codec->encode(
			(char*) "X_C_TYPE", (char*) "DEPOSIT", (char*) deposit, &wireSize);
	BT_ASSERT(expectedWireSize == wireSize);

	long expectedMemorySize = sizeof(DEPOSIT);
	long memorySize = wireSize;
	DEPOSIT* memoryBuffer =
			(DEPOSIT*) codec->decode(
					(char*) "X_C_TYPE", (char*) "DEPOSIT", (char*) wireBuffer,
					&memorySize);
	BT_ASSERT(expectedMemorySize == memorySize);

	// CHECK THE CONTENT OF THE CONVERTED BUFFER
	BT_ASSERT(deposit->acct_no == memoryBuffer->acct_no);
	BT_ASSERT(deposit->amount == memoryBuffer->amount);
	BT_ASSERT(deposit->balance == memoryBuffer->balance);
	BT_ASSERT(deposit->acct_no == memoryBuffer->acct_no);
	BT_ASSERT(strcmp(deposit->status, memoryBuffer->status) == 0);
	BT_ASSERT(deposit->status_len == memoryBuffer->status_len);

	delete[] wireBuffer;
	factory.release(codec);
	free(deposit);
	free(memoryBuffer);
}
