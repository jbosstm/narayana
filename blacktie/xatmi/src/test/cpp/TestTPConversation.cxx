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

#include "BaseServerTest.h"

#include "xatmi.h"

#include "TestTPConversation.h"

#include "btlogger.h"

#include "malloc.h"

int interationCount = 100;

#if defined(__cplusplus)
extern "C" {
#endif
extern void testTPConversation_service(TPSVCINFO *svcinfo);
extern void testTPConversation_short_service(TPSVCINFO *svcinfo);
#if defined(__cplusplus)
}
#endif

void TestTPConversation::setUp() {
	btlogger((char*) "TestTPConversation::setUp");
	sendbuf = NULL;
	rcvbuf = NULL;

	// Setup server
	BaseServerTest::setUp();

	// Do local work
	cd = -1;

	sendlen = 11;
	rcvlen = sendlen;
	BT_ASSERT((sendbuf
			= (char *) tpalloc((char*) "X_OCTET", NULL, sendlen)) != NULL);
	BT_ASSERT((rcvbuf = (char *) tpalloc((char*) "X_OCTET", NULL, rcvlen))
			!= NULL);
	BT_ASSERT(tperrno == 0);
}

void TestTPConversation::tearDown() {
	btlogger((char*) "TestTPConversation::tearDown");
	// Do local work
	::tpfree(sendbuf);
	::tpfree(rcvbuf);
	int toCheck = tpunadvertise((char*) "TestTPConversat");
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(toCheck != -1);

	// Clean up server
	BaseServerTest::tearDown();
}

void TestTPConversation::test_conversation() {

	int toCheck = tpadvertise((char*) "TestTPConversat",
			testTPConversation_service);
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(toCheck != -1);

	btlogger((char*) "test_conversation");
	strcpy(sendbuf, "conversate");
	cd
			= ::tpconnect((char*) "TestTPConversat", sendbuf, sendlen,
					TPRECVONLY);
	long revent = 0;
	char* tperrnoS = (char*) malloc(110);
	sprintf(tperrnoS, "%d", tperrno);
	BT_ASSERT_MESSAGE(tperrnoS, tperrno == 0);
	BT_ASSERT(cd != -1);
	btlogger("Started conversation");
	for (int i = 0; i < interationCount; i++) {
		int result = ::tprecv(cd, &rcvbuf, &rcvlen, 0, &revent);
		sprintf(tperrnoS, "%d", tperrno);
		BT_ASSERT_MESSAGE(tperrnoS, tperrno == TPEEVENT);
		BT_ASSERT(result == -1);
		char* expectedResult = (char*) malloc(sendlen);
		sprintf(expectedResult, "hi%d", i);
		char* errorMessage = (char*) malloc(sendlen * 2 + 1);
		sprintf(errorMessage, "%s/%s", expectedResult, rcvbuf);
		BT_ASSERT_MESSAGE(errorMessage, strcmp(expectedResult, rcvbuf)
				== 0);
		free(expectedResult);
		free(errorMessage);

		sprintf(sendbuf, "yo%d", i);
		//btlogger((char*) "test_conversation:%s:", sendbuf);
		result = ::tpsend(cd, sendbuf, sendlen, TPRECVONLY, &revent);
		sprintf(tperrnoS, "%d", tperrno);
		BT_ASSERT_MESSAGE(tperrnoS, tperrno == 0);
		BT_ASSERT(result != -1);
	}
	btlogger("Conversed");
	int result = ::tprecv(cd, &rcvbuf, &rcvlen, 0, &revent);
	sprintf(tperrnoS, "%d", tperrno);
	BT_ASSERT_MESSAGE(tperrnoS, tperrno == TPEEVENT);
	free(tperrnoS);
	BT_ASSERT(result == -1);
	BT_ASSERT(revent == TPEV_SVCSUCC);

	char* expectedResult = (char*) malloc(sendlen);
	sprintf(expectedResult, "hi%d", interationCount);
	char* errorMessage = (char*) malloc(sendlen * 2 + 1);
	sprintf(errorMessage, "%s/%s", expectedResult, rcvbuf);
	BT_ASSERT_MESSAGE(errorMessage, strcmp(expectedResult, rcvbuf) == 0);
	free(expectedResult);
	free(errorMessage);
}

void TestTPConversation::test_short_conversation() {

	int toCheck = tpadvertise((char*) "TestTPConversat",
			testTPConversation_short_service);
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(toCheck != -1);

	btlogger((char*) "test_short_conversation");
	cd = ::tpconnect((char*) "TestTPConversat", NULL, 0, TPRECVONLY);
	char* tperrnoS = (char*) malloc(110);
	sprintf(tperrnoS, "%d", tperrno);
	BT_ASSERT_MESSAGE(tperrnoS, tperrno == 0);

	char* cdS = (char*) malloc(110);
	sprintf(cdS, "%d", cd);
	BT_ASSERT_MESSAGE(cdS, cd != -1);
	free(cdS);

	long revent = 0;
	int result = ::tprecv(cd, &rcvbuf, &rcvlen, 0, &revent);
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(result != -1);
	BT_ASSERT(strcmp("hi0", rcvbuf) == 0);

	result = ::tprecv(cd, &rcvbuf, &rcvlen, 0, &revent);
	sprintf(tperrnoS, "%d", tperrno);
	BT_ASSERT_MESSAGE(tperrnoS, tperrno == TPEEVENT);
	free(tperrnoS);
	BT_ASSERT(result == -1);
	BT_ASSERT(revent == TPEV_SVCSUCC);
	BT_ASSERT(strcmp("hi1", rcvbuf) == 0);
}

void testTPConversation_service(TPSVCINFO *svcinfo) {

	btlogger((char*) "testTPConversation_service");
	bool fail = false;
	char *sendbuf = ::tpalloc((char*) "X_OCTET", NULL, svcinfo->len);
	char *rcvbuf = ::tpalloc((char*) "X_OCTET", NULL, svcinfo->len);

	char* expectedResult = (char*) malloc(svcinfo->len + 1);
	strcpy(expectedResult, "conversate");
	char* errorMessage = (char*) malloc(svcinfo->len * 2 + 2);
	sprintf(errorMessage, "%s/%s", expectedResult, svcinfo->data);
	if (strncmp(expectedResult, svcinfo->data, 10) != 0) {
		if (svcinfo->data != NULL) {
			btlogger("Got invalid data %s", svcinfo->data);
		} else {
			btlogger("GOT A NULL");
		}
		fail = true;
	} else {
		long revent = 0;
		btlogger("Chatting");
		for (int i = 0; i < interationCount; i++) {
			sprintf(sendbuf, "hi%d", i);
			//btlogger((char*) "testTPConversation_service:%s:", sendbuf);
			int result = ::tpsend(svcinfo->cd, sendbuf, svcinfo->len,
					TPRECVONLY, &revent);
			if (result != -1) {
				result = ::tprecv(svcinfo->cd, &rcvbuf, &svcinfo->len, 0,
						&revent);
				if (result == -1 && revent == TPEV_SENDONLY) {
					char* expectedResult = (char*) malloc(svcinfo->len);
					sprintf(expectedResult, "yo%d", i);
					char* errorMessage = (char*) malloc(svcinfo->len * 2 + 1);
					sprintf(errorMessage, "%s/%s", expectedResult, rcvbuf);
					if (strcmp(expectedResult, rcvbuf) != 0) {
						free(expectedResult);
						free(errorMessage);
						fail = true;
						break;
					}
					free(expectedResult);
					free(errorMessage);
				} else {
					fail = true;
					break;
				}
			} else {
				fail = true;
				break;
			}
		}
		btlogger("Chatted");
	}

	if (fail) {
		tpreturn(TPESVCFAIL, 0, sendbuf, 0, 0);
	} else {
		sprintf(sendbuf, "hi%d", interationCount);
		tpreturn(TPSUCCESS, 0, sendbuf, svcinfo->len, 0);
	}

	::tpfree(rcvbuf);
	free(expectedResult);
	free(errorMessage);
}

void testTPConversation_short_service(TPSVCINFO *svcinfo) {
	btlogger((char*) "testTPConversation_short_service");
	long sendlen = 4;
	long revent = 0;
	char *sendbuf = ::tpalloc((char*) "X_OCTET", NULL, sendlen);
	strcpy(sendbuf, "hi0");
	::tpsend(svcinfo->cd, sendbuf, sendlen, 0, &revent);
	strcpy(sendbuf, "hi1");
	tpreturn(TPSUCCESS, 0, sendbuf, sendlen, 0);
}

CPPUNIT_TEST_SUITE_REGISTRATION( TestTPConversation);
