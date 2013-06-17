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

#include "TestXMLCodecImpl.h"
#include "Codec.h"
#include "CodecFactory.h"
#include "AtmiBrokerEnv.h"
#include "btlogger.h"

#include "malloc.h"

void TestXMLCodecImpl::setUp() {
	init_ace();
	AtmiBrokerEnv::get_instance();

	// Perform global set up
	TestFixture::setUp();
}

void TestXMLCodecImpl::tearDown() {
	// Perform clean up
	AtmiBrokerEnv::discard_instance();

	// Perform global clean up
	TestFixture::tearDown();
}

void TestXMLCodecImpl::test_x_octet() {
	btlogger("TestXMLCodecImpl::test_x_octet");
	CodecFactory factory;
	Codec* codec = factory.getCodec((char*)"xml");
	char data[16];
	char* buf;
	long length;

	strcpy(data, (char*)"test");
	length = 4;

	buf = codec->encode((char*)"X_OCTET", (char*)"", data, &length);
	BT_ASSERT(buf != NULL);
	btlogger("%s\n", buf);
	//btlogger("length = %ld", length);

	char* debuf= codec->decode((char*)"X_OCTET", (char*)"", buf, &length);
	BT_ASSERT(debuf != NULL);
	//btlogger("length = %ld", length);
	BT_ASSERT(length == 4);
	BT_ASSERT(memcmp(data, debuf, 4) == 0);

	delete[] buf;
	free(debuf);
	factory.release(codec);
}

void TestXMLCodecImpl::test_x_c_type() {
	btlogger("TestXMLCodecImpl::test_x_c_type");
	CodecFactory factory;
	Codec* codec = factory.getCodec((char*)"xml");
	char* buf;
	long length;

	DEPOSIT* deposit = (DEPOSIT*) malloc(sizeof(DEPOSIT));
	deposit->acct_no = 1234567889;
	deposit->amount = 100;
	deposit->balance = 1.1;
	strcpy(
			deposit->status,
			"1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567");
	deposit->status_len = 127;

	length = -1;
	buf = codec->encode((char*)"X_C_TYPE", (char*)"DEPOSIT", (char*)deposit, &length);
	BT_ASSERT(buf != NULL);
	btlogger("%s", buf);
	//btlogger("strlen(buf) = %d, length = %ld\n", strlen(buf), length);

	DEPOSIT* memoryBuffer = (DEPOSIT*)codec->decode((char*)"X_C_TYPE", (char*)"DEPOSIT", buf, &length);

	// CHECK THE CONTENT OF THE CONVERTED BUFFER
	BT_ASSERT(memoryBuffer != NULL);
	BT_ASSERT(deposit->acct_no == memoryBuffer->acct_no);
	BT_ASSERT(deposit->amount == memoryBuffer->amount);
	BT_ASSERT(deposit->balance == memoryBuffer->balance);
	BT_ASSERT(deposit->acct_no == memoryBuffer->acct_no);
	BT_ASSERT(strcmp(deposit->status, memoryBuffer->status) == 0);
	BT_ASSERT(deposit->status_len == memoryBuffer->status_len);

	delete[] buf;
	free(memoryBuffer);
	free(deposit);
	factory.release(codec);
}

void TestXMLCodecImpl::test_x_c_type_2() {
	btlogger("TestXMLCodecImpl::test_x_c_type_2");
	CodecFactory factory;
	Codec* codec = factory.getCodec((char*)"xml");
	char* buf;
	long length;

	EXAMPLE* example = (EXAMPLE*) malloc(sizeof(EXAMPLE));
	example->id = 1001;
	example->test1[0] = 10;
	example->test1[1] = 11;
	example->test2[0] = 2.0;
	example->test2[1] = 2.1;
	example->test3 = 'a';
	example->test4 = 1.4;

	length = -1;
	buf = codec->encode((char*)"X_C_TYPE", (char*)"EXAMPLE", (char*)example, &length);
	BT_ASSERT(buf != NULL);
	btlogger("%s", buf);

	EXAMPLE* memoryBuffer = (EXAMPLE*)codec->decode((char*)"X_C_TYPE", (char*)"EXAMPLE", buf, &length);

	long size = sizeof(EXAMPLE);
	BT_ASSERT(memoryBuffer != NULL);
	BT_ASSERT(size == length);

	BT_ASSERT(example->id == memoryBuffer->id);
	BT_ASSERT(example->test1[0] == memoryBuffer->test1[0]);
	BT_ASSERT(example->test1[1] == memoryBuffer->test1[1]);
	BT_ASSERT(example->test2[0] == memoryBuffer->test2[0]);
	BT_ASSERT(example->test2[1] == memoryBuffer->test2[1]);
	BT_ASSERT(example->test3 == memoryBuffer->test3);
	BT_ASSERT(example->test4 == memoryBuffer->test4);

	/*
	btlogger("example->id = %d, memoryBuffer->id = %d",
			  example->id, memoryBuffer->id);
	btlogger("example->test1[0] = %d, memoryBuffer->test1[0] = %d",
			  example->test1[0], memoryBuffer->test1[0]);
	btlogger("example->test1[1] = %d, memoryBuffer->test1[1] = %d",
			  example->test1[1], memoryBuffer->test1[1]);
	btlogger("example->test2[0] = %f, memoryBuffer->test2[0] = %f",
			  example->test2[0], memoryBuffer->test2[0]);
	btlogger("example->test2[1] = %f, memoryBuffer->test2[1] = %f",
			  example->test2[1], memoryBuffer->test2[1]);
	btlogger("example->test3 = %c, memoryBuffer->test3 = %c",
			  example->test3, memoryBuffer->test3);
	btlogger("example->test4 = %f, memoryBuffer->test4 = %f",
			  example->test4, memoryBuffer->test4);
	*/

	delete[] buf;
	free(memoryBuffer);
	free(example);
	factory.release(codec);
}
