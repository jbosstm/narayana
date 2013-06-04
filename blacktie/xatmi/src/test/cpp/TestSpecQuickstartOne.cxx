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

#include "TestSpecQuickstartOne.h"

#if defined(__cplusplus)
extern "C" {
#endif
extern void debit_credit_svc(TPSVCINFO *svcinfo);
#if defined(__cplusplus)
}
#endif

void TestSpecQuickstartOne::setUp() {
	btlogger((char*) "TestSpecQuickstartOne::setUp");
	// Setup server
	BaseServerTest::setUp();

	// Do local work
	int toCheck = tpadvertise((char*) "DEBIT", debit_credit_svc);
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(toCheck != -1);

	toCheck = tpadvertise((char*) "CREDIT", debit_credit_svc);
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(toCheck != -1);
}

void TestSpecQuickstartOne::tearDown() {
	btlogger((char*) "TestSpecQuickstartOne::tearDown");
	// Do local work
	int toCheck = tpunadvertise((char*) "DEBIT");
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(toCheck != -1);

	toCheck = tpunadvertise((char*) "CREDIT");
	BT_ASSERT(tperrno == 0);
	BT_ASSERT(toCheck != -1);

	// Clean up server
	BaseServerTest::tearDown();
}

/* this test is taken from the XATMI specification */

void TestSpecQuickstartOne::test_specquickstartone() {
	btlogger((char*) "TestSpecQuickstartOne::test_specquickstartone");
	DATA_BUFFER *dptr; /* DATA_BUFFER is a typed buffer of type */
	DATA_BUFFER *cptr; /* X_C_TYPE and subtype dc_buf. The structure */
	long dlen = 0;
	long clen = 0; /* contains a character array named input and an */
	int cd; /* integer named output. */
	/* allocate typed buffers */
	dptr = (DATA_BUFFER *) tpalloc((char*) "X_C_TYPE", (char*) "dc_buf", 0);
	cptr = (DATA_BUFFER *) tpalloc((char*) "X_C_TYPE", (char*) "dc_buf", 0);
	/* populate typed buffers with input data */
	strcpy(dptr->input, "debit account 123 by 50");
	strcpy(cptr->input, "credit account 456 by 50");
	tx_begin(); /* start global transaction */
	/* issue asynchronous request to DEBIT, while it is processing... */
	cd = tpacall((char*) "DEBIT", (char *) dptr, 0, TPSIGRSTRT);
	/* ...issue synchronous request to CREDIT */
	tpcall((char*) "CREDIT", (char *) cptr, 0, (char **) &cptr, &clen,
			TPSIGRSTRT);
	/* retrieve DEBIT�s reply */
	tpgetrply(&cd, (char **) &dptr, &dlen, TPSIGRSTRT);
	if (dptr->output == OK && cptr->output == OK)
		tx_commit(); /* commit global transaction */
	else
		tx_rollback(); /* rollback global transaction */
}

/* this routine is used for DEBIT and CREDIT */
void debit_credit_svc(TPSVCINFO *svcinfo) {
	btlogger((char*) "debit_credit_svc");
	DATA_BUFFER *dc_ptr;
	int rval;
	/* extract request typed buffer */
	dc_ptr = (DATA_BUFFER *) svcinfo->data;
	/*
	 * Depending on service name used to invoke this
	 * routine, perform either debit or credit work.
	 */
	if (!strcmp(svcinfo->name, "DEBIT")) {
		/*
		 * Parse input data and perform debit
		 * as part of global transaction.
		 */
	} else {
		/*
		 * Parse input data and perform credit
		 * as part of global transaction.
		 */
	}
	// TODO MAKE TWO TESTS
	if (dc_ptr->failTest == 0) {
		rval = TPSUCCESS;
		dc_ptr->output = OK;
	} else {
		rval = TPFAIL; /* global transaction will not commit */
		dc_ptr->output = NOT_OK;
	}
	/* send reply and return from service routine */
	tpreturn(rval, 0, (char *) dc_ptr, 0, 0);
}

CPPUNIT_TEST_SUITE_REGISTRATION( TestSpecQuickstartOne);
