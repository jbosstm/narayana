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
#include "XATMITestSuite.h"
#include "xatmi.h"
#include "tx.h"
#include <string.h>

#include "TestSpecQuickstartTwo.h"

#if defined(__cplusplus)
extern "C" {
#endif
extern void inquiry_svc(TPSVCINFO *svcinfo);
#if defined(__cplusplus)
}
#endif

void TestSpecQuickstartTwo::setUp() {
	btlogger((char*) "TestSpecQuickstartTwo::setUp");
	// Setup server
	BaseServerTest::setUp();

	// Do local work
	int toCheck = tpadvertise((char*) "INQUIRY", inquiry_svc);
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(toCheck != -1);
}

void TestSpecQuickstartTwo::tearDown() {
	btlogger((char*) "TestSpecQuickstartTwo::tearDown");
	// Do local work
	int toCheck = tpunadvertise((char*) "INQUIRY");
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(toCheck != -1);

	// Clean up server
	BaseServerTest::tearDown();
}

void TestSpecQuickstartTwo::test_specquickstarttwo() {
	btlogger((char*) "TestSpecQuickstartTwo::test_specquickstarttwo");
	DATA_BUFFER *ptr; /* DATA_BUFFER is a typed buffer of type */
	long len = 0;
	long event = 0; /* X_C_TYPE and subtype inq_buf. The structure */
	int cd; /* contains a character array named input and an */
	/* array of integers named output. */
	/* allocate typed buffer */
	ptr = (DATA_BUFFER *) tpalloc((char*) "X_C_TYPE", (char*) "inq_buf", 0);
	/* populate typed buffer with input data */
	strcpy(ptr->input, "retrieve all accounts with balances less than 0");
	tx_begin(); /* start global transaction */
	/*connect to conversational service, send input data, & yield control*/
	cd = tpconnect((char*) "INQUIRY", (char *) ptr, 0, TPRECVONLY | TPSIGRSTRT);
	do {
		/* receive 10 account records at a time */
		tprecv(cd, (char **) &ptr, &len, TPSIGRSTRT, &event);
		/*
		 * Format & display in AP-specific manner the accounts returned.
		 */
	} while (tperrno != TPEEVENT);
	if (event == TPEV_SVCSUCC)
		tx_commit(); /* commit global transaction */
	else
		tx_rollback(); /* rollback global transaction */
}

/* this routine is used for INQUIRY */
void inquiry_svc(TPSVCINFO *svcinfo) {
	btlogger((char*) "inquiry_svc");
	DATA_BUFFER *ptr;
	long event;
	int rval;
	/* extract initial typed buffer sent as part of tpconnect() */
	ptr = (DATA_BUFFER *) svcinfo->data;
	/*
	 * Parse input string, ptr->input, and retrieve records.
	 * Return 10 records at a time to client. Records are
	 * placed in ptr->output, an array of account records.
	 */
	for (int i = 0; i < 5; i++) {
		/* gather from DBMS next 10 records into ptr->output array */
		tpsend(svcinfo->cd, (char *) ptr, 0, TPSIGRSTRT, &event);
	}
	// TODO DO OK AND FAIL
	if (ptr->failTest == 0) {
		rval = TPSUCCESS;
	} else {
		rval = TPFAIL; /* global transaction will not commit */
	}
	/* terminate service routine, send no data, and */
	/* terminate connection */
	tpreturn(rval, 0, NULL, 0, 0);
}

CPPUNIT_TEST_SUITE_REGISTRATION( TestSpecQuickstartTwo);
