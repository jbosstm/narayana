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
#include <jni.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>

#include <stdlib.h>
#include "string.h"

#include "btserver.h"
#include "btclient.h"
#include "ace/OS_NS_unistd.h"
#include "xatmi.h"
#include "btnbf.h"
#include "XATMITestSuite.h"

#include "ace/OS_NS_unistd.h"

#include "xatmi.h"
#include "btlogger.h"

#include "Sleeper.h"

extern void test_tpcall_TPETIME_service(TPSVCINFO *svcinfo);
extern void test_tpcall_TPEOTYPE_service(TPSVCINFO *svcinfo);
extern void test_tpcall_TPESVCFAIL_service(TPSVCINFO *svcinfo);
extern void test_tprecv_TPEV_DISCONIMM_service(TPSVCINFO *svcinfo);
extern void test_tprecv_TPEV_SVCFAIL_service(TPSVCINFO *svcinfo);
extern void test_no_tpreturn_service(TPSVCINFO *svcinfo);

extern void test_tx_tpcall_x_octet_service_without_tx(TPSVCINFO *svcinfo);
extern void test_tx_tpcall_x_octet_service_with_tx(TPSVCINFO *svcinfo);

extern "C" void BAR(TPSVCINFO * svcinfo) {
	int sendlen = 14;
	char* buffer = tpalloc((char*) "X_OCTET", NULL, sendlen);
	strncpy(buffer, "BAR SAYS HELLO", 14);

	tpreturn(TPSUCCESS, 1, buffer, sendlen, 0);
}

void tpcall_x_octet(TPSVCINFO * svcinfo) {
	int sendlen = 14;
	char* buffer = tpalloc((char*) "X_OCTET", 0, sendlen);
	strncpy(buffer, "BAR SAYS HELLO", 14);
	tpreturn(TPSUCCESS, 1, buffer, sendlen, 0);
}

void loopy(TPSVCINFO* tpsvcinfo) {
	btlogger((char*) "loopy");
}

/* this routine is used for DEBIT and CREDIT */
void debit_credit_svc(TPSVCINFO *svcinfo) {
	btlogger((char*) "debit_credit_svc: %d", svcinfo->len);
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
	btlogger((char*) "tpreturn 0 hmm: %d", svcinfo->len);
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

void testtpacall_service(TPSVCINFO *svcinfo) {
	btlogger((char*) "testtpacall_service");
	int len = 20;
	char *toReturn = (char*) malloc(len);
	strcpy(toReturn, "testtpacall_service");
	tpreturn(TPSUCCESS, 0, toReturn, len, 0);
	free(toReturn);
}
void test_tpcall_x_octet_service(TPSVCINFO *svcinfo) {
	btlogger((char*) "test_tpcall_x_octet_service");
	bool ok = false;
	if (svcinfo->data) {
		if (strncmp(svcinfo->data, "test_tpcall_x_octet", svcinfo->len) == 0) {
			ok = true;
		}
	}

	int len = 60;
	char *toReturn = ::tpalloc((char*) "X_OCTET", NULL, len);
	if (ok) {
		strcpy(toReturn, "tpcall_x_octet");
	} else {
		strcpy(toReturn, "fail");
		if (svcinfo->data) {
			strcpy(toReturn, svcinfo->data);
		} else {
			strcpy(toReturn, "dud");
		}
	}
	tpreturn(TPSUCCESS, 20, toReturn, len, 0);
}

void test_tpcall_x_octet_service_zero(TPSVCINFO *svcinfo) {
	btlogger((char*) "test_tpcall_x_octet_service_zero");
	int len = 0;
	char *toReturn = ::tpalloc((char*) "X_OCTET", NULL, len);
	tpreturn(TPSUCCESS, 21, toReturn, len, 0);
}

void test_tpcall_x_common_service(TPSVCINFO *svcinfo) {
	btlogger((char*) "test_tpcall_x_common_service");
	bool ok = false;
	DEPOSIT *dptr = (DEPOSIT*) svcinfo->data;
	if (dptr->acct_no == 12345678 && dptr->amount == 50) {
		ok = true;
	} else {
		char* foo = svcinfo->data;
		for (int i = 0; i < svcinfo->len; i++) {
			btlogger((char*) "Position: %d was: %o", i, foo[i]);
		}
		btlogger((char*) "Data was: %d/%d", dptr->acct_no, dptr->amount);
	}

	int len = 60;
	char *toReturn = ::tpalloc((char*) "X_OCTET", NULL, len);
	if (ok) {
		strcpy(toReturn, "tpcall_x_common");
	} else {
		strcpy(toReturn, "fail");
	}
	tpreturn(TPSUCCESS, 22, toReturn, len, 0);
}

void test_tpcall_x_c_type_service(TPSVCINFO *svcinfo) {
	btlogger((char*) "test_tpcall_x_c_type_service");
	bool ok = false;
	ACCT_INFO *aptr = (ACCT_INFO*) svcinfo->data;
	bool acctEq = aptr->acct_no == 12345678;
	bool nameEq = strcmp(aptr->name, "TOM") == 0;
	bool fooEq = aptr->foo[0] == 1.1F && aptr->foo[1] == 2.2F;
	bool balsEq = aptr->balances[0] == 1.1 && aptr->balances[1] == 2.2;
	if (acctEq && nameEq && fooEq && balsEq) {
		ok = true;
	} else {
		btlogger((char*) "Data was: %ld/%s/%.2f/%.2f/%.2f/%.2f/", 
				aptr->acct_no, aptr->name, aptr->foo[0], aptr->foo[1],
				aptr->balances[0], aptr->balances[1]);
	}

	int len = 60;
	char *toReturn = ::tpalloc((char*) "X_OCTET", NULL, len);
	if (ok) {
		strcpy(toReturn, "tpcall_x_c_type");
	} else {
		strcpy(toReturn, "fail");
	}
	tpreturn(TPSUCCESS, 23, toReturn, len, 0);
}
void testtpcancel_service(TPSVCINFO *svcinfo) {
	btlogger((char*) "testtpcancel_service");
	if (!(svcinfo->flags && TPNOREPLY)) {
		int len = 21;
		char *toReturn = ::tpalloc((char*) "X_OCTET", NULL, len);
		strcpy(toReturn, "testtpcancel_service");
		tpreturn(TPSUCCESS, 0, toReturn, len, 0);
	}
}
void testtpconnect_service(TPSVCINFO *svcinfo) {
	btlogger((char*) "testtpconnect_service");
	tpreturn(TPSUCCESS, 0, NULL, 0, 0);
}
void testTPConversation_service(TPSVCINFO *svcinfo) {

	btlogger((char*) "testTPConversation_service ");
	bool fail = false;
	char *sendbuf = ::tpalloc((char*) "X_OCTET", NULL, 10);
	char *rcvbuf = ::tpalloc((char*) "X_OCTET", NULL, 10);

	char* expectedResult = (char*) malloc(10);
	strncpy(expectedResult, "conversate", 10);

	if (strncmp(expectedResult, svcinfo->data, 10) != 0) {
		btlogger((char*) "Fail");
		if (svcinfo->data != NULL) {
			btlogger((char*) "Got invalid data");
		} else {
			btlogger((char*) "GOT A NULL");
		}
		fail = true;
	} else {
		btlogger((char*) "Chatting");
		long revent = 0;
		for (int i = 10; i < 100; i++) {
			sprintf(sendbuf, "hi%d", i);
			//btlogger((char*) "testTPConversation_service:%s:", sendbuf);
			int result = ::tpsend(svcinfo->cd, sendbuf, svcinfo->len,
					TPRECVONLY, &revent);
			long rlen = 0;
			if (result != -1) {
				result = ::tprecv(svcinfo->cd, &rcvbuf, &rlen, 0,
						&revent);
				if (result == -1 && revent == TPEV_SENDONLY) {
					char* expectedResult = (char*) malloc(5);
					memset(expectedResult, '\0', 5);
					sprintf(expectedResult, "yo%d", i);
					char* receivedResult = (char*) malloc(rlen + 1);
					memset(receivedResult, '\0', rlen + 1);
					memcpy(receivedResult, rcvbuf, rlen);
					//					char* errorMessage = (char*) malloc(svcinfo->len * 2 + 1);
					//					sprintf(errorMessage, "%s/%s", expectedResult, rcvbuf);

					btlogger((char*) "Expected %s/%s was received", expectedResult, receivedResult);
					if (strncmp(expectedResult, rcvbuf, 4) != 0) {
						btlogger((char*) "Failed for unexpected");
						free(expectedResult);
						//						free(errorMessage);
						fail = true;
						break;
					}
					free(expectedResult);
					//					free(errorMessage);
				} else {
					btlogger((char*) "Failed for wrong event");
					fail = true;
					break;
				}
			} else {
				btlogger((char*) "Failed as didnt get -1");
				fail = true;
				break;
			}
		}
		btlogger((char*) "Chatted");
	}

	if (fail) {
		btlogger((char*) "Failed");
		tpreturn(TPESVCFAIL, 0, sendbuf, 0, 0);
	} else {
		btlogger((char*) "Passed");
		sprintf(sendbuf, "hi%d", 100);
		tpreturn(TPSUCCESS, 0, sendbuf, 5, 0);
	}

	::tpfree(rcvbuf);
	free(expectedResult);
	//	free(errorMessage);
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
void testtpdiscon_service(TPSVCINFO *svcinfo) {
	btlogger((char*) "testtpdiscon_service");
	::sleeper(2);
}

void testtpfreeservice_service(TPSVCINFO *svcinfo) {
	btlogger((char*) "testtpfreeservice_service");
	// Allocate a buffer to return
	char *toReturn = tpalloc((char*) "X_OCTET", (char*) "acct_info", 1);

	// Free should be idempotent on the inbound buffer
	::tpfree(svcinfo->data);

	// Get the data from tptypes still
	int toTest = ::tptypes(svcinfo->data, NULL, NULL);

	// Check the values of tptypes (should still have been valid
	if (toTest == -1 || tperrno == TPEINVAL) {
		// False
		toReturn[0] = '0';
	} else {
		// True
		toReturn[0] = '1';
	}

	// Return the data
	tpreturn(TPSUCCESS, 0, toReturn, 1, 0);
}
void testtpgetrply_service(TPSVCINFO *svcinfo) {
	btlogger((char*) "testtpgetrply_service");
	char * toReturn = ::tpalloc((char*) "X_OCTET", NULL, 22);
	strcpy(toReturn, "testtpgetrply_service");
	tpreturn(TPSUCCESS, 0, toReturn, 22, 0);
}
void testtprecv_service(TPSVCINFO *svcinfo) {
	btlogger((char*) "testtprecv_service");
}

void testtpreturn_service(TPSVCINFO *svcinfo) {
	btlogger((char*) "testtpreturn_service");
	char *toReturn = (char*) malloc(21);
	strcpy(toReturn, "testtpreturn_service");
	tpreturn(TPSUCCESS, 0, toReturn, 21, 0);
	free(toReturn);
}

void testtpreturn_service_tpurcode(TPSVCINFO *svcinfo) {
	btlogger((char*) "testtpreturn_service_tpurcode");
	int len = 0;
	char *toReturn = ::tpalloc((char*) "X_OCTET", NULL, len);
	if (strncmp(svcinfo->data, "24", 2) == 0) {
		tpreturn(TPSUCCESS, 24, toReturn, len, 0);
	} else {
		tpreturn(TPSUCCESS, 77, toReturn, len, 0);
	}
}

void testtpsend_service(TPSVCINFO *svcinfo) {
	btlogger((char*) "testtpsend_service");
}

void testtpsend_tpsendonly_service(TPSVCINFO *svcinfo) {
	btlogger((char*) "testtpsend_tpsendonly_service");

	long event = 0;
	int result = ::tpsend(svcinfo->cd, svcinfo->data, svcinfo->len, TPRECVONLY,
			&event);

	long revent = 0;
	long rcvlen;
	char* rcvbuf = (char *) tpalloc((char*) "X_OCTET", NULL, svcinfo->len);
	result = ::tprecv(svcinfo->cd, &rcvbuf, &rcvlen, 0, &revent);
}

void testtpservice_service(TPSVCINFO *svcinfo) {
	btlogger((char*) "testtpservice_service");
}
void testtpunadvertise_service(TPSVCINFO *svcinfo) {
	btlogger((char*) "testtpunadvertise_service");
	char * toReturn = new char[26];
	strcpy(toReturn, "testtpunadvertise_service");
	// Changed length from 0L to svcinfo->len
	tpreturn(TPSUCCESS, 0, toReturn, 25, 0);
	delete toReturn;
}

int n = 0;

void test_TTL_service(TPSVCINFO *svcinfo) {
	btlogger((char*) "test_TTL_service");
	int len = 60;
	char *toReturn = ::tpalloc((char*) "X_OCTET", NULL, len);

	btlogger("Data was %s", svcinfo->data);
	if (strncmp(svcinfo->data, "counter", 7) == 0) {
		sprintf(toReturn, "%d", n);
	} else {
		::sleeper(60);
		n++;

		strcpy(toReturn, "test_tpcall_TTL_service");
	}
	tpreturn(TPSUCCESS, 0, toReturn, len, 0);
}

void test_tpgetrply_TPGETANY_one(TPSVCINFO *svcinfo) {
	char* response = (char*) "test_tpgetrply_TPGETANY_one";
	btlogger(response);

	long sendlen = strlen(response) + 1;
	char * toReturn = ::tpalloc((char*) "X_OCTET", NULL, sendlen);
	strcpy(toReturn, response);
	::sleeper(10);
	tpreturn(TPSUCCESS, 0, toReturn, sendlen, 0);
}

void test_tpgetrply_TPGETANY_two(TPSVCINFO *svcinfo) {
	char* response = (char*) "test_tpgetrply_TPGETANY_two";
	btlogger(response);

	long sendlen = strlen(response) + 1;
	char * toReturn = ::tpalloc((char*) "X_OCTET", NULL, sendlen);
	strcpy(toReturn, response);
	::sleeper(5);
	tpreturn(TPSUCCESS, 0, toReturn, sendlen, 0);
}

void testtpreturn_service_opensession1(TPSVCINFO *svcinfo) {
	btlogger((char*) "testtpreturn_service_opensession1");
	int cd = ::tpacall((char*) "TestTPReturnB", (char *) svcinfo->data,
			svcinfo->len, 0);
	tpreturn(TPSUCCESS, 0, svcinfo->data, svcinfo->len, 0);
}

void testtpreturn_service_opensession2(TPSVCINFO *svcinfo) {
	btlogger((char*) "testtpreturn_service_opensession2");
	tpreturn(TPSUCCESS, 0, svcinfo->data, svcinfo->len, 0);
}

void test_service_nbf(TPSVCINFO *svcinfo) {
	btlogger((char*) "test_service_nbf");
	char* buf = svcinfo->data;
	int rc = 0;

	char name[16];
	long id;
	int  len = 16;

	rc = btgetattribute(buf, (char*)"name", 0, (char*) name, &len);
	if(rc == 0) {
		btlogger((char*) "get name value is %s", name);
	}

	len = 0;
	rc = btgetattribute(buf, (char*)"id", 0, (char*)&id, &len);
	if(rc == 0) {
		btlogger((char*) "get id value is %d", id);
	}

	btlogger((char*)"remove attr");
	rc = btdelattribute(buf, (char*)"name", 0);
	if(rc == 0) {
		btlogger((char*) "remove name");
	}

	id = 1234;
	rc = btsetattribute(&buf, (char*)"id", 0, (char*)&id, sizeof(id));
	if(rc == 0) {
		btlogger((char*) "set id value to 1234");
	}

	tpreturn(TPSUCCESS, 0, buf, strlen(buf), 0);
}

extern "C"
JNIEXPORT void JNICALL Java_org_jboss_narayana_blacktie_jatmibroker_RunServer_serverinit(JNIEnv *, jobject) {
	int exit_status = -1;
	btlogger((char*) "serverinit called");
#ifdef WIN32
	char* argv[] = {(char*)"server", (char*)"-c", (char*)"win32", (char*)"-i", (char*)"1", (char*)"-s", (char*)"testsui", (char*)"-p", (char*)"12342"};
#else
	char* argv[] = {(char*)"server", (char*)"-c", (char*)"linux", (char*)"-i", (char*)"1", (char*)"-s", (char*)"testsui", (char*)"-p", (char*)"12342"};
#endif
	int argc = sizeof(argv)/sizeof(char*);

	exit_status = serverinit(argc, argv);
	exit_status = tpadvertise((char*) "BAR", BAR);
	btlogger((char*) "serverinit returning");

	return;
}

extern "C"
JNIEXPORT void JNICALL Java_org_jboss_narayana_blacktie_jatmibroker_RunServer_serverdone(JNIEnv *, jobject) {
	int exit_status = -1;
	btlogger((char*) "serverdone called");
	exit_status = serverdone();
	clientdone(0);
	btlogger((char*) "serverdone returning");
	return;
}

extern "C"
JNIEXPORT void JNICALL Java_org_jboss_narayana_blacktie_jatmibroker_RunServer_tpadvertiseBAR(JNIEnv *, jobject) {
	// Do local work
	tpadvertise((char*) "BAR", BAR);
}

extern "C"
JNIEXPORT void JNICALL Java_org_jboss_narayana_blacktie_jatmibroker_RunServer_tpadvertiseLOOPY(JNIEnv *, jobject) {
	tpadvertise((char*) "LOOPY", loopy);
}

extern "C"
JNIEXPORT void JNICALL Java_org_jboss_narayana_blacktie_jatmibroker_RunServer_tpadvertiseDEBIT(JNIEnv *, jobject) {
	tpadvertise((char*) "DEBIT", debit_credit_svc);
}

extern "C"
JNIEXPORT void JNICALL Java_org_jboss_narayana_blacktie_jatmibroker_RunServer_tpadvertiseCREDIT(JNIEnv *, jobject) {
	tpadvertise((char*) "CREDIT", debit_credit_svc);
}

extern "C"
JNIEXPORT void JNICALL Java_org_jboss_narayana_blacktie_jatmibroker_RunServer_tpadvertiseINQUIRY(JNIEnv *, jobject) {
	tpadvertise((char*) "INQUIRY", inquiry_svc);
}

extern "C"
JNIEXPORT void JNICALL Java_org_jboss_narayana_blacktie_jatmibroker_RunServer_tpadvertiseTestTPACall(JNIEnv *, jobject) {
	tpadvertise((char*) "TestTPACall", testtpacall_service);
	// TODO tpadvertise exit_status =
}

extern "C"
JNIEXPORT void JNICALL Java_org_jboss_narayana_blacktie_jatmibroker_RunServer_tpadvertisetpcallXOctet(JNIEnv *, jobject) {
	tpadvertise((char*) "tpcall_x_octet", test_tpcall_x_octet_service);
}

extern "C"
JNIEXPORT void JNICALL Java_org_jboss_narayana_blacktie_jatmibroker_RunServer_tpadvertisetpcallXOctetZero(JNIEnv *, jobject) {
	tpadvertise((char*) "tpcall_x_octet", test_tpcall_x_octet_service_zero);
}

extern "C"
JNIEXPORT void JNICALL Java_org_jboss_narayana_blacktie_jatmibroker_RunServer_tpadvertisetpcallXCommon(JNIEnv *, jobject) {
	tpadvertise((char*) "tpcall_x_common", test_tpcall_x_common_service);
}

extern "C"
JNIEXPORT void JNICALL Java_org_jboss_narayana_blacktie_jatmibroker_RunServer_tpadvertisetpcallXCType(JNIEnv *, jobject) {
	tpadvertise((char*) "tpcall_x_c_type", test_tpcall_x_c_type_service);
}

extern "C"
JNIEXPORT void JNICALL Java_org_jboss_narayana_blacktie_jatmibroker_RunServer_tpadvertiseTestTPCancel(JNIEnv *, jobject) {
	tpadvertise((char*) "TestTPCancel", testtpcancel_service);
}

extern "C"
JNIEXPORT void JNICALL Java_org_jboss_narayana_blacktie_jatmibroker_RunServer_tpadvertiseTestTPConnect(JNIEnv *, jobject) {
	tpadvertise((char*) "TestTPConnect", testtpconnect_service);
}

extern "C"
JNIEXPORT void JNICALL Java_org_jboss_narayana_blacktie_jatmibroker_RunServer_tpadvertiseTestTPConversation(JNIEnv *, jobject) {
	tpadvertise((char*) "TestTPConversat",
			testTPConversation_service);
}

extern "C"
JNIEXPORT void JNICALL Java_org_jboss_narayana_blacktie_jatmibroker_RunServer_tpadvertiseTestTPConversa2(JNIEnv *, jobject) {
	tpadvertise((char*) "TestTPConversat",
			testTPConversation_short_service);
}

extern "C"
JNIEXPORT void JNICALL Java_org_jboss_narayana_blacktie_jatmibroker_RunServer_tpadvertiseTestTPDiscon(JNIEnv *, jobject) {
	tpadvertise((char*) "TestTPDiscon", testtpdiscon_service);
}

extern "C"
JNIEXPORT void JNICALL Java_org_jboss_narayana_blacktie_jatmibroker_RunServer_tpadvertiseTestTPFree(JNIEnv *, jobject) {
	tpadvertise((char*) "TestTPFree", testtpfreeservice_service);
}

extern "C"
JNIEXPORT void JNICALL Java_org_jboss_narayana_blacktie_jatmibroker_RunServer_tpadvertiseTestTPGetrply(JNIEnv *, jobject) {
	tpadvertise((char*) "TestTPGetrply", testtpgetrply_service);
}

extern "C"
JNIEXPORT void JNICALL Java_org_jboss_narayana_blacktie_jatmibroker_RunServer_tpadvertiseTestTPRecv(JNIEnv *, jobject) {
	tpadvertise((char*) "TestTPRecv", testtprecv_service);
}

extern "C"
JNIEXPORT void JNICALL Java_org_jboss_narayana_blacktie_jatmibroker_RunServer_tpadvertiseTestTPReturn(JNIEnv *, jobject) {
	tpadvertise((char*) "TestTPReturnA", testtpreturn_service);
}

extern "C"
JNIEXPORT void JNICALL Java_org_jboss_narayana_blacktie_jatmibroker_RunServer_tpadvertiseTestTPReturn2(JNIEnv *, jobject) {
	tpadvertise((char*) "TestTPReturnA",
			testtpreturn_service_tpurcode);
}

extern "C"
JNIEXPORT void JNICALL Java_org_jboss_narayana_blacktie_jatmibroker_RunServer_tpadvertiseTestTPSendTPSendOnly(JNIEnv *, jobject) {
	tpadvertise((char*) "TestTPSend", testtpsend_tpsendonly_service);
}

extern "C"
JNIEXPORT void JNICALL Java_org_jboss_narayana_blacktie_jatmibroker_RunServer_tpadvertiseTestTPSend(JNIEnv *, jobject) {
	tpadvertise((char*) "TestTPSend", testtpsend_service);
}

extern "C"
JNIEXPORT void JNICALL Java_org_jboss_narayana_blacktie_jatmibroker_RunServer_tpadvertiseTestTPService(JNIEnv *, jobject) {
	tpadvertise((char*) "TestTPService", testtpservice_service);
}

extern "C"
JNIEXPORT void JNICALL Java_org_jboss_narayana_blacktie_jatmibroker_RunServer_tpadvertiseTestTPUnadvertise(JNIEnv *, jobject) {
	tpadvertise((char*) "TestTPUnadvertise",
			testtpunadvertise_service);
}

extern "C"
JNIEXPORT void JNICALL Java_org_jboss_narayana_blacktie_jatmibroker_RunServer_tpadvertiseTX1(JNIEnv *, jobject) {
	tpadvertise((char*) "tpcall_x_octet", test_tx_tpcall_x_octet_service_without_tx);
}

extern "C"
JNIEXPORT void JNICALL Java_org_jboss_narayana_blacktie_jatmibroker_RunServer_tpadvertiseTX2(JNIEnv *, jobject) {
	tpadvertise((char*) "tpcall_x_octet", test_tx_tpcall_x_octet_service_with_tx);
}

extern "C"
JNIEXPORT void JNICALL Java_org_jboss_narayana_blacktie_jatmibroker_RunServer_tpadvertiseTestRollbackOnlyTpcallTPETIMEService(JNIEnv *, jobject) {
	tpadvertise((char*) "TestRbkOnly", test_tpcall_TPETIME_service);
}

extern "C"
JNIEXPORT void JNICALL Java_org_jboss_narayana_blacktie_jatmibroker_RunServer_tpadvertiseTestRollbackOnlyTpcallTPEOTYPEService(JNIEnv *, jobject) {
	tpadvertise((char*) "TestRbkOnly", test_tpcall_TPEOTYPE_service);
}

extern "C"
JNIEXPORT void JNICALL Java_org_jboss_narayana_blacktie_jatmibroker_RunServer_tpadvertiseTestRollbackOnlyTpcallTPESVCFAILService(JNIEnv *, jobject) {
	tpadvertise((char*) "TestRbkOnly", test_tpcall_TPESVCFAIL_service);
}

extern "C"
JNIEXPORT void JNICALL Java_org_jboss_narayana_blacktie_jatmibroker_RunServer_tpadvertiseTestRollbackOnlyTprecvTPEVDISCONIMMService(JNIEnv *, jobject) {
	tpadvertise((char*) "TestRbkOnly2", test_tprecv_TPEV_DISCONIMM_service);
}

extern "C"
JNIEXPORT void JNICALL Java_org_jboss_narayana_blacktie_jatmibroker_RunServer_tpadvertiseTestRollbackOnlyTprecvTPEVSVCFAILService(JNIEnv *, jobject) {
	tpadvertise((char*) "TestRbkOnly2", test_tprecv_TPEV_SVCFAIL_service);
}

extern "C"
JNIEXPORT void JNICALL Java_org_jboss_narayana_blacktie_jatmibroker_RunServer_tpadvertiseTestRollbackOnlyNoTpreturnService(JNIEnv *, jobject) {
	tpadvertise((char*) "TestRbkOnly", test_no_tpreturn_service);
}

extern "C"
JNIEXPORT void JNICALL Java_org_jboss_narayana_blacktie_jatmibroker_RunServer_tpadvertiseTTL(JNIEnv *, jobject) {
	tpadvertise((char*) "TTL", test_TTL_service);
}

extern "C"
JNIEXPORT void JNICALL Java_org_jboss_narayana_blacktie_jatmibroker_RunServer_tpadvertiseTestTPGetrplyOne(JNIEnv *, jobject) {
	tpadvertise((char*) "TestTPGetAnyA", test_tpgetrply_TPGETANY_one);
}

extern "C"
JNIEXPORT void JNICALL Java_org_jboss_narayana_blacktie_jatmibroker_RunServer_tpadvertiseTestTPGetrplyTwo(JNIEnv *, jobject) {
	tpadvertise((char*) "TestTPGetAnyB", test_tpgetrply_TPGETANY_two);
}

extern "C"
JNIEXPORT void JNICALL Java_org_jboss_narayana_blacktie_jatmibroker_RunServer_tpadvertiseTestTPReturn3(JNIEnv *, jobject) {
	tpadvertise((char*) "TestTPReturnA", testtpreturn_service_opensession1);
}

extern "C"
JNIEXPORT void JNICALL Java_org_jboss_narayana_blacktie_jatmibroker_RunServer_tpadvertiseTestTPReturn4(JNIEnv *, jobject) {
	tpadvertise((char*) "TestTPReturnB", testtpreturn_service_opensession2);
}

extern "C"
JNIEXPORT void JNICALL Java_org_jboss_narayana_blacktie_jatmibroker_RunServer_tpadvertiseTestNBF(JNIEnv *, jobject) {
	tpadvertise((char*) "NBF", test_service_nbf);
}
