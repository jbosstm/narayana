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

#include "xatmi.h"
#include "btlogger.h"
#include "BaseAdminTest.h"

#include <stdlib.h>

void BaseAdminTest::setUp() {
	BaseServerTest::setUp();
}

void BaseAdminTest::tearDown() {
	BaseServerTest::tearDown();
}

int BaseAdminTest::callADMIN(char* command, char expect, int r, char** n) {
	btlogger("Command was %s", command);
	long  sendlen = strlen(command) + 1;
	char* sendbuf = tpalloc((char*) "X_OCTET", NULL, sendlen);
	strcpy(sendbuf, command);

	char* recvbuf = tpalloc((char*) "X_OCTET", NULL, 1);
	long  recvlen = 1;

	int cd = ::tpcall((char*) ".testsui1", (char *) sendbuf, sendlen, (char**)&recvbuf, &recvlen, TPNOTRAN);
	BT_ASSERT(r != -1);
	BT_ASSERT(recvbuf[0] == expect);
	BT_ASSERT(r == tperrno);

	if(strncmp(command, "counter", 7) == 0 ||
	   strncmp(command, "error_counter", 13) == 0 ||
	   strncmp(command, "version", 7) == 0) {
		*n = (char*) malloc(recvlen);
		memset(*n, 0, recvlen);
		memcpy(*n, &recvbuf[1], recvlen -1);
	} else if(strncmp(command, "status", 6) == 0) {
		btlogger((char*) "len is %d, service status: %s", recvlen, &recvbuf[1]);
	}

	tpfree(sendbuf);
	tpfree(recvbuf);

	return cd;
}

int BaseAdminTest::callBAR(int r, char* buf) {
	if(buf == NULL) {
		buf = (char*) "test";
	}
	long  sendlen = strlen(buf) + 1;
	char* sendbuf = tpalloc((char*) "X_OCTET", NULL, sendlen);
	strcpy(sendbuf, buf);
	char* recvbuf = tpalloc((char*) "X_OCTET", NULL, 1);
	long  recvlen = 1;


	int cd = ::tpcall((char*) "BAR", (char *) sendbuf, sendlen, (char**)&recvbuf, &recvlen, 0);
	if (r == 0) {
		char* tperrnoS = (char*) malloc(110);
		sprintf(tperrnoS, "%d", tperrno);
		BT_ASSERT_MESSAGE(tperrnoS, cd != -1);
		free(tperrnoS);
	} else {
		char* tperrnoS = (char*) malloc(110);
		sprintf(tperrnoS, "%d", tperrno);
		BT_ASSERT_MESSAGE(tperrnoS, cd == -1);
		free(tperrnoS);
	}
	btlogger((char*) "r = %d, tperrno = %d", r, tperrno);
	BT_ASSERT(r == tperrno);
	if(tperrno == 0) {
		BT_ASSERT(recvbuf[0] == '1');
	}

	tpfree(sendbuf);
	tpfree(recvbuf);

	return cd;
}
