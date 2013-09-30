/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc., and others contributors as indicated
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
#include "apr-1/apr.h"
#include "btserver.h"
#include "btclient.h"

#include <stdlib.h>
#include <vector>

#include "xatmi.h"
#include "btlogger.h"
#include "string.h"

#include <stdarg.h>
#include <stdio.h>
#include <string.h>

#ifndef WIN32
#include "tx.h"
#include <unistd.h>
#endif

#include "apr-1/apr_thread_mutex.h"
#include "apr-1/apr_thread_proc.h"
#include "apr-1/apr_portable.h"

#ifdef WIN32
#include "atmiBrokerTxMacro.h"
#define TX_OK			  0   /* normal execution */
#define SIGALRM 1
typedef SSIZE_T ssize_t;
#ifdef __cplusplus
extern "C" {
#endif
extern BLACKTIE_TX_DLL int tx_begin(void);
extern BLACKTIE_TX_DLL int tx_close(void);
extern BLACKTIE_TX_DLL int tx_commit(void);
extern BLACKTIE_TX_DLL int tx_open(void);
#ifdef __cplusplus
}
#endif
#endif

static apr_thread_mutex_t* mutex_;
const char *MSG1 = "CLIENT REQUEST		";
const char *MSG2 = "PAUSE - CLIENT REQUEST";
//static char huge_buf[0x100000]; 
//static char huge_buf[0xf00000]; 
static char huge_buf[0xf000000]; 
static char large_buf[0x10000]; 

static int tx = 0;
static int startTx(int);
static int endTx(int);

// data type for controlling the work done by each thread
typedef struct thr_arg {
	int failonerror;
	const char *data;
	const char *msg;
	const char *svc;
	const char *sndtype;
	const char *rcvtype;
	long flags;
	int expect;
	long expect2;
	int signum;
	int result;
} thr_arg_t;

// arbitray subtype for testing C type buffers
typedef struct subtype {
	char data[64];
	int id;
	char op;
} subtype_t;

static void do_assert(int failonerror, int* res, int cond, const char *fmt, ...) {
	char str[1024];
	va_list args;

	va_start(args, fmt);
	vsnprintf(str, 1024, fmt, args);
	va_end(args);

	if (!cond) {
		*res = 1;
		btlogger((char*) "UNSUCCESSFULL assert %s", str);
		if (failonerror)
			_exit(1);
	}

//	btlogger((char*) "successful assert for: cond=%d (%s)", cond, str);
}

/**
 * Split a name value pair of the form "name=value" returning the name and value
 * as distinct strings. In addition if the value is an integral type then its long
 * value representation is returned.
 *
 * WARNING: the input char pointer is modified and therefore must not be a constant
 * char pointer.
 *
 * @param nvp
 * 	the input name value pair
 * @param name
 * 	holds the return value for the name portion of the pair
 * @param value
 * 	holds the return value for the value portion of the pair
 * @param lvalue
 * 	holds the return value for the value portion of the pair as a long
 * 	(only valid if *value is non-zero
 *
 * 	@return
 * 		zero if the value is not null
 * 		non-zero otherwise
 */
int decode_nvp(char *nvp, char** value, long* lvalue) {
	char* v = strchr(nvp, '=');

	*value = (v == NULL ? NULL : v + 1);
	if (v != NULL) {
		*v = '\0';
		*lvalue = atol(*value);
		return 0;
	}

	return 1;
}

//static int do_tpcall(int failonerror, const char *data, const char *msg, const char *svc, const char *sndtype, long flags, int expect) {
static int do_tpcall(thr_arg_t *args) {
	int tpstatus = 0;
	char *rbuf;
	char *sbuf;
	char type[20];
	char subtype[20];
	int isSbCType = (strcmp(args->sndtype, X_C_TYPE) == 0); // is send buffer is X_C_TYPE
	int isRbCType = (strcmp(args->rcvtype, X_C_TYPE) == 0); // is recv buffer is X_C_TYPE
	long sbufsz = (isSbCType ? 0 : strlen(args->data) + 1);	// api sets the buffer size if X_C_TYPE
	long rbufsz = (isRbCType ? 0 : 64);
	int res;

	sbuf = tpalloc((char *) args->sndtype, (isSbCType ? (char*) "sub_type" : NULL), sbufsz);
	rbuf = tpalloc((char *) args->rcvtype, (isRbCType ? (char*) "sub_type" : NULL), rbufsz);

	do_assert(args->failonerror, &args->result, sbuf != 0, "tpalloc send buf failed tperrno=%d", tperrno);
	do_assert(args->failonerror, &args->result, rbuf != 0, "tpalloc recv buf failed tperrno=%d", tperrno);

	strcpy(sbuf, args->data);
	memset(rbuf, 0, rbufsz);

	tptypes(sbuf, type, subtype);
	btlogger((char *) "sbuf type: %s sbufsz: %d rbuf type: %s type: %s subtype: %s %d vrs %d",
		args->sndtype, sbufsz, args->rcvtype, type, subtype, tpstatus, args->expect);

	if (strstr(args->data, "T8") == args->data) {
		btlogger((char *) "T8: startTX");
		if (startTx(true) != 0)
			do_assert(args->failonerror, &res, false, "Could not open or begin transaction: ");

		btlogger((char *) "T8: tpacall");
		int cd = tpacall((char *) args->svc, sbuf, sbufsz, args->flags);
		btlogger((char *) "T8: endTx");
		int txres = endTx(true);

		btlogger((char *) "T8: check assert");
		do_assert(args->failonerror, &res, txres != TX_OK, "commit or close transaction succeeded with active descriptors");

		btlogger((char *) "T8: tpgetrply");
		tpstatus = tpgetrply(&cd, (char **) (char **) &rbuf, &rbufsz, args->flags);
		btlogger((char *) "T8: finished");

	} else {
		btlogger((char *) "Invoking test");
		tpstatus = tpcall((char *) args->svc, sbuf, sbufsz, (char **) &rbuf, &rbufsz, args->flags);
	}

	res = (tperrno == args->expect ? 0 : 1);
	if (tpstatus)
		btlogger((char *) "tpcall returned %d tperrno=%d expect=%d", tpstatus, tperrno, args->expect);

	// check that tperrno has the expected value
	do_assert(args->failonerror, &args->result, tperrno == args->expect,
		"%s: wrong response from tpcall %s %s tpstatus=%d flags=%d expect=%d tperrno=%d",
		args->msg, args->svc, args->sndtype, tpstatus, args->flags, args->expect, tperrno);

	// if there was no service error then check that the service returned the expected value
	if (tperrno == 0)
		do_assert(args->failonerror, &args->result, tpurcode == args->expect2,
			"tpurcode: expected=%d tpurcode=%d",
			args->expect2, tpurcode);
	tpfree(sbuf);
	tpfree(rbuf);

	return res;
}

// thread entry point
static void* APR_THREAD_FUNC work(apr_thread_t * thread, void *args)
{
	(void) do_tpcall((thr_arg_t *) args);
	return args;
}

static void signal_thread(apr_thread_t* tid, int signum)
{
	btlogger((char*) "sleep 2 secs before sending signal %d to thread %d", signum, tid);
	// allow enough time for the thread to perform a tpcall request
	apr_sleep(apr_time_from_sec(2));
	btlogger((char*) "sending signal %d to thread %d", signum, tid);
#ifndef WIN32
	apr_os_thread_t* osthd;
	apr_os_thread_get(&osthd, tid);
	int rv1 = pthread_kill (*osthd, signum);
	btlogger((char*) "thread kill returned %d", rv1);
#endif
	// sending a signal to the process doesn't really test TPSIGRSTRT since the
	// signal is unlikely to be sent to the thread that issued the tpcall with
	// the TPSIGRSTRT flag set. But we test it anyway.
#if 0
	apr_proc_t proc;
	proc.pid = getpid();
	int rv2 = apr_proc_kill(&proc, signum);
	btlogger((char*) "process kill returned %d", rv2);
#endif
}

// another thread entry point
static int tcnt_ = 0;
static void* APR_THREAD_FUNC work2(apr_thread_t* thread, void *args)
{
	thr_arg_t *params = (thr_arg_t *) args;
	char *s1, *s2;
	int ncalls = 2;
	int okcalls = 0;
	int rv;

	s1 = (char *) "BAR";
	s2 = (char *) "BAR";

	apr_thread_mutex_lock(mutex_);
	tcnt_ += 1;
	tpfree(tpalloc((char *) params->sndtype, 0, 10));
#if 0	/* I've disabled using multiple service since it fails with just one service */
	if (tcnt_  % 2 == 0)
		s2 = (char *) "TestTPCall";
	else
		s1 = (char *) "TestTPCall";
#endif
	apr_thread_mutex_unlock(mutex_);

//XXX	apr_sleep(apr_time_from_sec(4));	// yield to ensure that all threads have initialised env (see bug BLACKTIE-211)

	for (int i = 0; i < ncalls; i++) {
		btlogger((char*) "%s: loop %d of %d", params->svc, i, ncalls);
		params->svc = s1;
		if ((rv = do_tpcall(params)))
			btlogger((char*) "%s: tpcall %d error: %d", params->svc, i, rv);
		else
			okcalls += 1;
		params->svc = s2;
		btlogger((char*) "%s: loop %d of %d call 2", params->svc, i, ncalls);
		if ((rv = do_tpcall(params)))
			btlogger((char*) "%s: tpcall %d error: %d", params->svc, i, rv);
		else
			okcalls += 1;
	}

	btlogger("Thread (t) finished %d out of %d calls successful\n", okcalls, ncalls * 2);

	params->result = ((okcalls == ncalls * 2)?0:1);
	return args;
}

static int lotsofwork(int nthreads, apr_thread_start_t tfunc, thr_arg_t* arg) {
	std::vector<apr_thread_t*> tids;
	int i;

	apr_pool_t* pool;

	if(apr_pool_create(&pool,NULL) != APR_SUCCESS)
		btlogger("Unable to create apr pool\n");

        if(apr_thread_mutex_create(&mutex_,0, pool) != APR_SUCCESS)
                btlogger("Unable to initialise mutex\n");

	btlogger("lotsofwork: spawning %d threads\n", nthreads);
	// spawn nthreads threads
	for(i = 0; i < nthreads; i++)
	{
		apr_thread_t* tid;
		if (apr_thread_create(&tid, // return thread id for each thread
			NULL,
			tfunc, // entry point for new thread
			(void *) arg,	// args for thread entry point
			pool) != APR_SUCCESS) {
				btlogger("Unable to start request number of threads\n");
		}
		tids.push_back(tid);
	}

	if (arg->signum > 0)
		signal_thread(tids[0], arg->signum);

	btlogger("lotsofwork: joining ...\n");
	apr_status_t status;
	for (int i = 0; i < nthreads; i++)
		if (tids[i] != 0)
			apr_thread_join(&status, tids[i]);

	btlogger("lotsofwork: joined res=%d\n", arg->result);
	apr_pool_destroy(pool);

	return arg->result;
}

// XsdValidator is not thread safe
static int bug211() {
	thr_arg_t args = {1, MSG1, "bug211: two threads reading env", "BAR", X_OCTET, X_OCTET, 0, 0, 99, 0};
	return lotsofwork(2, work, &args);
}

// tpcall should return TPEINVAL if the service name is invalid
static int bug213() {
	thr_arg_t args = {1, MSG1, "bug213: NULL service name", NULL, X_OCTET, X_OCTET, 0, TPEINVAL, 0, 0};
	return do_tpcall(&args);
}

// tpcall incorrectly returns TPNOTIME whenever the TPNOBLOCK or TPNOTIME flags are specified
static int bug212a() {
	// Specifying TPNOTIME means the caller wants to be imune to blocking conditions (such
	// as no output buffers). Thus if such a condition does not exist the call should succeed as normal.
	// However if bug 212 is present then the call returns TPNOTIME
	long flags2 = TPNOTRAN | TPNOTIME;
	thr_arg_t arg1 = {1, MSG1, "bug212a: TPNOTIME", "BAR", X_OCTET, X_OCTET, flags2, 0, 99, 0};

	return lotsofwork(1, work, &arg1);
}
static int bug212b() {
	// Similarly specifying TPNOBLOCK means that if a blocking condition does exist then the caller
	// should get the error TPEBLOCK
	// However if bug 212 is present then the call returns TPNOTIME
	long flags3 = TPNOTRAN | TPNOBLOCK;
	const char *data = MSG1;
	//const char *data = "T6=4";

	// send a buffer large enough to fill the network buffers. TODO really we need the current sndbuf size
	// getsockopt(socket, SOL_SOCKET, SO_SNDBUF, ...) and then make sure sizeof (huge_buf) is larger
	memset(huge_buf, ' ', sizeof (huge_buf));
	memcpy(huge_buf, data, strlen(data));
	huge_buf[sizeof (huge_buf) - 1] = '\0';

	// int sndbufsz = something small;
	// setsockopt(socket, SOL_SOCKET, SO_SNDBUF, (char *) &sndbufsz, (int) sizeof(sndbufsz));

	thr_arg_t args = {1, huge_buf, "bug212b: TPNOBLOCK", "BAR", X_OCTET, X_OCTET, flags3, TPEBLOCK, 99, 0};

#ifndef WIN32
	return lotsofwork(1, work, &args);
#else
	btlogger((char*) "DISABLING TEST 2121 for WIN32 build");
	return 0;
#endif
}

// TPSIGRSTRT flag isn't supported on tpcall
static int bug214() {
	thr_arg_t args = {1, MSG1, "bug214: TPSIGRSTRT flag not supported on tpcall", "BAR", X_OCTET, X_OCTET, TPSIGRSTRT, 0, 99, 0};
	return lotsofwork(1, work, &args);
}

// tpcall failure with multiple threads
static int bug215() {
	thr_arg_t args = {0, MSG1, "bug215: tpcall failure with lots of threads", "BAR", X_OCTET, X_OCTET, 0, 0, 99, 0};
	return lotsofwork(2, work2, &args);
}

static int bug216a() {
	thr_arg_t args = {1, MSG1, "bug216: tp bufs should morph if they're the wrong type", "BAR", X_OCTET, X_C_TYPE, 0, 0, 99, 0};
	return lotsofwork(1, work, &args);
}

static int bug216b() {
	thr_arg_t args = {1, MSG1, "bug216: passing the wrong return buffer type with TPNOCHANGE",
		"BAR", X_OCTET, X_C_TYPE, TPNOCHANGE, TPEOTYPE, 99, 0};
	return lotsofwork(1, work, &args);
}

static int bug217() {
	thr_arg_t args = {1, MSG1, "bug217: make sure tpurcode works", "BAR", X_OCTET, X_OCTET, 0, 0, 99, 0};
	(void) lotsofwork(1, work, &args);
	return args.result;
}

static int t9001() {
	char *buf = (char *) tpalloc((char *) X_C_TYPE, (char*) "sub_type", 10);
	int res = 0;
	do_assert(1, &res, buf != 0, "tpalloc with X_C_TYPE and non-zero len: tperrno=%d (spec says size is optional)", tperrno);
	tpfree(buf);
	return res;
}

// sanity check
static int t0() {
	thr_arg_t args = {1, MSG1, "ok test", "BAR", X_OCTET, X_OCTET, 0, 0, 99, 0};
	return do_tpcall(&args);
}

// tell the server to set a flag on tpreturn (should generate TPESVCERR)
static int t1() {
	thr_arg_t args = {1, "T1", "set flag on tpreturn should fail", "BAR", X_OCTET, X_OCTET, TPNOTRAN, TPESVCERR, 0, 0};
	return do_tpcall(&args);
}
static int t2() {
	thr_arg_t args = {1, "T2", "tell the service to free the the service buffer", "BAR", X_OCTET, X_OCTET, TPNOTRAN, 0, 99, 0};
	return do_tpcall(&args);
}

// telling the service to not tpreturn should generate an error
static int t3() {
	thr_arg_t args = {1, "T3", "no tpreturn", "BAR", X_OCTET, X_OCTET, 0, TPESVCERR, 0, 0};
	return do_tpcall(&args);
}

// telling service to call tpreturn outside service routine should have no effect
static int t4() {
	thr_arg_t args = {1, "T4", "tpreturn outside service routing", "BAR", X_OCTET, X_OCTET, 0, 0, 99, 0};
	return do_tpcall(&args);
}

static int t5() {
	thr_arg_t args = {1, "T5", "tpreturn TPFAIL", "BAR", X_OCTET, X_OCTET, 0, TPESVCFAIL, 99, 0};
	return do_tpcall(&args);
}

static int t6() {
	// TODO this test is for tpcall. Add tests for other xatmi API calls (tpacall etc_)
	thr_arg_t args = {1, "T6=4", "set TPSIGRSTRT flag and send a signal", "BAR", X_OCTET, X_OCTET, TPSIGRSTRT, 0, 99, SIGALRM};
	return lotsofwork(1, work, &args);
}

static int t7() {
	thr_arg_t args = {1, "T6=4", "do not set TPSIGRSTRT flag and send a signal", "BAR", X_OCTET, X_OCTET, 0, TPGOTSIG, 99, SIGALRM};
	return lotsofwork(1, work, &args);
}
static int t8() {
	thr_arg_t args = {1, "T8", "commit tx with active descriptors", "BAR", X_OCTET, X_OCTET, 0, TPEBADDESC, 99, 0};
	return do_tpcall(&args);
}
static int t9() {
	const char *data = MSG1;

	// send a buffer large enough to fill the network buffers. TODO really we need the current sndbuf size
	// getsockopt(socket, SOL_SOCKET, SO_SNDBUF, ...) and then make sure sizeof (large_buf) is larger
	memset(large_buf, ' ', sizeof (large_buf));
	memcpy(large_buf, data, strlen(data));
	large_buf[sizeof (large_buf) - 1] = '\0';

	thr_arg_t args = {1, large_buf, "large buffer test", "BAR", X_OCTET, X_OCTET, 0, 0, 99, 0};

	return lotsofwork(1, work, &args);
}

static int startTx(int enable) {
	if (enable && (tx_open() != TX_OK || tx_begin() != TX_OK))
		return 1;
	return 0;
}
static int endTx(int enable) {
	if (enable && (tx_commit() != TX_OK || tx_close() != TX_OK))
		return 1;
	return 0;
}

int run_client(int argc, char **argv) {
	int res = 0;
	int bug = 217;

	if (argc > 1)
		bug = atoi(argv[1]);

	apr_initialize();

	btlogger((char*) "starting test %d", bug);

	if (startTx(tx) != 0)
		btlogger((char*) "ERROR - Could not open or begin transaction: ");
	else {
		switch (bug) {
		case 211:	res = bug211(); break;
		case 2120:	res = bug212a(); break;
		case 2121:	res = bug212b(); break;
		case 213:	res = bug213(); break;
		case 214:	res = bug214(); break;
		case 215:	res = bug215(); break;
		case 2160:	res = bug216a(); break;
		case 2161:	res = bug216b(); break;

		case 217:	res = bug217(); break;
		case 9001:	res = t9001(); break;
		case 0:		res = t0(); break;
		case 1:		res = t1(); break;
		case 2:		res = t2(); break;
		case 3:		res = t3(); break;
		case 4:		res = t4(); break;
		case 5:		res = t5(); break;
		case 6:		res = t6(); break;
		case 7:		res = t7(); break;
		case 8:		res = t8(); break;
		case 9:		res = t9(); break;
		default: break;
		}

		if (endTx(tx) != 0)
			btlogger((char*) "ERROR - Could not commit or close transaction: ");
	}

	btlogger((char*) "test %d %s with code %d", bug, (res == 0 ? "passed" : "failed"), res);
	clientdone(0);

	apr_terminate();

	return res;
}

void BAR (TPSVCINFO *);
void TestTPCall (TPSVCINFO *);

void BAR(TPSVCINFO * svcinfo) {
	char* buffer;
	int sendlen = 15;
	long rflag = 0L;
	int rval = TPSUCCESS;
	char *arg;
	long larg;

	btlogger((char*) "bar called  - svc=%s data=%s len=%d flags=%d rcode=%d tperrno=%d",
		svcinfo->name, svcinfo->data, svcinfo->len, svcinfo->flags, 99, tperrno);

	decode_nvp(svcinfo->data, &arg, &larg);

	if (strcmp(svcinfo->data, "T1") == 0) {
		rflag = TPEBLOCK;
	} else if (strcmp(svcinfo->data, "T2") == 0) {
		tpfree(svcinfo->data);
	} else if (strcmp(svcinfo->data, "T5") == 0) {
		rval = TPFAIL;
	} else if (strcmp(svcinfo->data, "T6") == 0 && arg != NULL) {
		btlogger((char*) "bar sleeping for %d seconds", larg);
		apr_sleep(apr_time_from_sec(larg));
	}

	buffer = tpalloc((char *) "X_OCTET", 0, sendlen);
	strcpy(buffer, "BAR SAYS HELLO");

	if (strcmp(svcinfo->data, "T3") != 0)
		tpreturn(rval, 99, buffer, sendlen, rflag);

	if (strcmp(svcinfo->data, "T4") == 0)
		tpreturn(TPFAIL, 99, buffer, sendlen, rflag);

	if (tperrno)
		btlogger((char*) "bar returned: tperrno=%d", tperrno);
}

void TestTPCall(TPSVCINFO * svcinfo) {
	BAR(svcinfo);
}

/* the byte pattern written to file descriptor 1 to indicate that the server has advertised its services */
static const unsigned char HANDSHAKE[] = {83,69,82,86,73,67,69,83,32,82,69,65,68,89};
static const ssize_t HANDSHAKE_LEN = 14;

int run_server(int argc, char **argv) {
	int exit_status = serverinit(argc, argv);

	if (exit_status != -1) {
		tpadvertise((char *) "BAR", BAR);
		tpadvertise((char *) "TestTPCall", TestTPCall);
		if (fwrite(HANDSHAKE, sizeof(char), HANDSHAKE_LEN, stdout) != HANDSHAKE_LEN) {
			return -1;
		}
		/* flush stdout */
		fprintf(stdout, "\n");
		exit_status = serverrun();
	} else {
		btlogger((char*) "main Unexpected exception in serverrun()");
	}
	btlogger((char*) "Test Server: calling serverdone()");
	serverdone();
	clientdone(0);
	btlogger((char*) "Test Server: returning status %d", exit_status);
	return exit_status;
}

int main(int argc, char **argv) {
	int i;

	for (i = 0; i < argc; i++) {
		if (strcmp(argv[i], "-i") == 0)
			return run_server(argc, argv);
	}

	return run_client(argc, argv);
}
