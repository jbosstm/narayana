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
#include "BaseTest.h"

#include "xatmi.h"
#include "btnbf.h"
#include "TestNBFCall.h"

#if defined(__cplusplus)
extern "C" {
#endif

extern void nbf_service(TPSVCINFO *svcinfo);

#if defined(__cplusplus)
}
#endif

void TestNBFCall::setUp() {
	btlogger((char*) "TestNBFCall:setUp");
	BaseServerTest::setUp();

	BT_ASSERT(tperrno == 0);
}

void TestNBFCall::tearDown() {
	btlogger((char*) "TestNBFCall::tearDown");
	BaseServerTest::tearDown();
}

void TestNBFCall::test_tpcall() {
	btlogger((char*)"test_tpcall");
	tpadvertise((char*) "PBF", nbf_service);

	char* buf = tpalloc((char*)"BT_NBF", (char*)"employee", 0);
	btaddattribute(&buf, (char*)"name", (char*)"zhfeng", 6);
	long empid = 1001;
	btaddattribute(&buf, (char*)"id", (char*)&empid, sizeof(empid));

	long sendlen = strlen(buf);
	long rcvlen = 16;
	char* rcvbuf =(char*) tpalloc ((char*) "X_OCTET", NULL, rcvlen);

	int id = ::tpcall((char*) "PBF", (char*) buf, sendlen, (char**) &rcvbuf, &rcvlen, (long) 0);

	BT_ASSERT(tperrno == 0);
	BT_ASSERT(id != -1);
	BT_ASSERT_MESSAGE(rcvbuf, strcmp(rcvbuf, "nbf_service") == 0);

	tpfree(rcvbuf);
}

void nbf_service(TPSVCINFO *svcinfo) {
	btlogger((char*) "nbf_service");
	char* buf = svcinfo->data;
	
	btlogger(buf);

	int rc;
	char name[16];
	long empid;
	int len = 16;

	rc = btgetattribute(buf, (char*)"name", 0, (char*) name, &len);
	BT_ASSERT(rc == 0);
	BT_ASSERT(len == 6);
	BT_ASSERT(strcmp(name, "zhfeng") == 0);

	len = 0;
	rc = btgetattribute(buf, (char*)"id", 0, (char*) &empid, &len);
	BT_ASSERT(rc == 0);
	BT_ASSERT(len == sizeof(long));
	BT_ASSERT(empid == 1001);

	len = 16;
	char* toReturn = ::tpalloc((char*) "X_OCTET", NULL, len);
	if(rc == 0) {
		strcpy(toReturn, "nbf_service");
	} else {
		strcpy(toReturn, "fail");
	}

	tpreturn(TPSUCCESS, 0, toReturn, len, 0);
}
