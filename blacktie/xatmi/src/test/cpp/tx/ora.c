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
/* Based on the code quickstarts in the Oracle Streams Advanced Queuing User's Guide and Reference */

#include "tx/request.h"

#ifdef ORACLE
#include <oci.h>

/* 
 * If a session wont release a lock here's how to recover
 * 
 * find the lock (lock type should be TX):
 * SELECT sid,type,id1,lmode,request from v$lock;
 *
 * find the lock owner:
 * SELECT s.sid, s.serial#, s.osuser, s.program FROM v$session s where s.sid=140;
 * end the session that owns the lock:
 * ALTER SYSTEM KILL SESSION 'sid,serial#';
 *
 */

#ifdef WIN32
extern __declspec(dllimport) struct xa_switch_t xaoswd;
#else
struct xa_switch_t xaoswd;
#endif

/* figure out the oracle error code and message corresponding to an Oracle error */
static void get_error(test_req_t *resp, dvoid *errhp, sword status) {
	if  (status == OCI_ERROR) {
		text buf[256];
		sb4 err = 0;
		(void) OCIErrorGet(errhp, (ub4) 1, (text *) NULL, &err, (text *) buf, (ub4) sizeof (buf), OCI_HTYPE_ERROR);

		btlogger_snprintf(resp->data, sizeof (resp->data), "%s", buf);
	} else {
	   	btlogger_snprintf(resp->data, sizeof (resp->data), "OCI error: %d", (int) status);
	}
}

static void show_error(dvoid *errhp, sword status) {
	if  (status == OCI_ERROR) {
		text buf[256];
		sb4 err = 0;
		(void) OCIErrorGet(errhp, (ub4) 1, (text *) NULL, &err, buf, (ub4) sizeof(buf), OCI_HTYPE_ERROR);
		btlogger_warn( "TxLog OCI error %d: %s", (int) err, buf);
	} else {
		btlogger_warn( "TxLog OCI error: %d", (int) status);
	}
}

/* execute an SQL statement for the given service context */
static int doSql(OCISvcCtx *svcCtx, OCIStmt *stmthp, OCIError *errhp, text *sql, int empno) {
	OCIBind *bndp = (OCIBind *) 0;
	sword status = OCIStmtPrepare(stmthp, errhp, (text *) sql, (ub4) strlen((char *)sql),
		(ub4) OCI_NTV_SYNTAX, (ub4) OCI_DEFAULT);

	btlogger_debug( "TxLog executing statement: %s :1=%d", sql, empno);

	/* bind empno to the statement */
	if (empno > 0 && status == OCI_SUCCESS)
		status = OCIBindByPos(stmthp, &bndp, errhp, 1, (dvoid *) &empno, (sword) sizeof (empno),
				SQLT_INT, (dvoid *) 0, (ub2 *) 0, (ub2 *) 0, (ub4) 0, (ub4 *) 0, OCI_DEFAULT);

	if (status == OCI_SUCCESS)
		status = OCIStmtExecute(svcCtx, stmthp, errhp, (ub4) 1, (ub4) 0,
								(CONST OCISnapshot *) NULL,
								(OCISnapshot *) NULL, OCI_DEFAULT);

	if (status != OCI_SUCCESS)
		show_error(errhp, status);

	return status;

}

/* update test */
static int doUpdate(OCISvcCtx *svcCtx, OCIStmt *stmthp, OCIError *errhp, int empno) {
	text *sql = (text *) "UPDATE EMP SET JOB='DIRECTOR' WHERE EMPNO=:1" ;
	return doSql(svcCtx, stmthp, errhp, sql, empno);
}
/* insert test */
static int doInsert(OCISvcCtx *svcCtx, OCIStmt *stmthp, OCIError *errhp, int empno) {
	text *sql = (text *) "INSERT INTO EMP (EMPNO,ENAME,JOB,MGR,HIREDATE,SAL,COMM,DEPTNO)"
		" VALUES (:1,'Jim','Janitor','7902','17-DEC-80','900','0','20')";
	return doSql(svcCtx, stmthp, errhp, sql, empno);
}
/* delete test WARNING the delete is WHERE EMPNO >= :1 */
static int doDelete(OCISvcCtx *svcCtx, OCIStmt *stmthp, OCIError *errhp, int empno) {
	text *sql = (text *) "DELETE FROM EMP WHERE EMPNO >= :1" ;
	return doSql(svcCtx, stmthp, errhp, sql, empno);
}

/* select test */
static int doSelect(OCISvcCtx *svcCtx, OCIStmt *stmthp, OCIError *errhp, int empno, int *rcnt) {
	OCIBind *bndp = (OCIBind *) 0;
	text *sql = (text *) "SELECT ENAME,JOB FROM EMP WHERE EMPNO >= :1" ;
	char emp[20];
	char job[20];

	OCIDefine *stmtdef1 = (OCIDefine *) 0;
	OCIDefine *stmtdef2 = (OCIDefine *) 0;

	btlogger_debug( "TxLog doSelect: :1=%d", empno);
	sword status = OCIStmtPrepare(stmthp, errhp, (text *) sql, (ub4) strlen((char *) sql),
		(ub4) OCI_NTV_SYNTAX, (ub4) OCI_DEFAULT);

	/* bind empno to the statement */
	if (status == OCI_SUCCESS)
		status = OCIBindByPos(stmthp, &bndp, errhp, 1, (dvoid *) &empno, (sword) sizeof (empno),
				SQLT_INT, (dvoid *) 0, (ub2 *) 0, (ub2 *) 0, (ub4) 0, (ub4 *) 0, OCI_DEFAULT);

	/* define the output variables for the select */
	if (status == OCI_SUCCESS)
		status = OCIDefineByPos(stmthp, &stmtdef1, errhp, 1, (dvoid *)&emp,(sword) sizeof (emp), SQLT_STR,
			(dvoid *) 0, (ub2 *)0, (ub2 *)0, (ub4) OCI_DEFAULT);

	if (status == OCI_SUCCESS)
		status = OCIDefineByPos(stmthp, &stmtdef2, errhp, 2, (dvoid *)&job,(sword) sizeof (job), SQLT_STR,
			(dvoid *) 0, (ub2 *)0, (ub2 *)0, (ub4) OCI_DEFAULT);

	/* exectute the select */
	if (status == OCI_SUCCESS)
		status = OCIStmtExecute(svcCtx, stmthp, errhp, (ub4) 0, (ub4) 0, (CONST OCISnapshot *) NULL, (OCISnapshot *) NULL, OCI_DEFAULT);

	btlogger_debug( "TxLog executing statement: %s :1=%d", sql, empno);
	*rcnt = 0;
	if (status != OCI_SUCCESS && status != OCI_NO_DATA) {
		show_error(errhp, status);
		return status;
	} else {
		while (1) {
			status = OCIStmtFetch(stmthp, errhp, (ub4) 1, (ub4) OCI_FETCH_NEXT, (ub4) OCI_DEFAULT);
			if (status != OCI_SUCCESS && status != OCI_SUCCESS_WITH_INFO)
				break;
			btlogger_debug( "TxLog Name: %s Job: %s", emp, job);
			(*rcnt) += 1;
		}
		btlogger_debug( "TxLog result: %d", *rcnt);

		return OCI_SUCCESS;
	}
}

/* the test: insert some data, update it and finally delete it */
static sword doWork(char op, char *arg, OCISvcCtx *svcCtx, OCIStmt *stmthp, OCIError *errhp, test_req_t *resp) {
	sword status = OCI_SUCCESS;
	int empno;

	btlogger_debug( "TxLog doWork op=%c arg=%s", op, arg);
	empno = (*arg ? atoi(arg) : 8000);
	(resp->data)[0] = 0;

	if (op == '0') {
		status = doInsert(svcCtx, stmthp, errhp, empno);
	} else if (op == '1') {
		int rcnt = 0;	// no of matching records
		status = doSelect(svcCtx, stmthp, errhp, empno, &rcnt);
	   	btlogger_snprintf(resp->data, sizeof (resp->data), "%d", rcnt);
	} else if (op == '2') {
		status = doUpdate(svcCtx, stmthp, errhp, empno);
	} else if (op == '3') {
		status = doDelete(svcCtx, stmthp, errhp, empno);
	}

	if (status != OCI_SUCCESS)
		get_error(resp, errhp, status);

	return status;
}

long ora_xaflags()
{
	return xaoswd.flags;
}

/**
 * test that blacktie correctly drives oracle xa
 * Precondition: - there is an active transaction (implies that that there is open XA connection)
 *
 * arguments:
 * op - 0 for insert 1 for select 2 for update and 3 for delete (CRUD)
 * arg - used in selects to hold the expected number of records
 * rbuf - used to report error strings (except in a successful select it holds the number of matching records)
 * bufsz - the length of rbuf
 */
int ora_access(test_req_t *req, test_req_t *resp)
{
	OCIStmt *stmthp;
	OCIError *errhp;
	OCIEnv *xaEnv;
	OCISvcCtx *svcCtx;
	sword status;

	btlogger_debug( "TxLog ora_access op=%c data=%s db=%s", req->op, req->data, req->db);
	/* opening an XA connection creates an environment and service context */
	xaEnv = (struct OCIEnv *) xaoEnv((text *) req->db) ;
	svcCtx = (struct OCISvcCtx *) xaoSvcCtx((text *) req->db);

	if (!xaEnv || !svcCtx)
		return fatal("TxLog ORA:- Unable to obtain env and/or service context!");

	/* initialise OCI handles */
	if (OCI_SUCCESS != OCIHandleAlloc((dvoid *)xaEnv, (dvoid **)&errhp,
		OCI_HTYPE_ERROR, 0, (dvoid **)0))
		return fatal("ORA:- Unable to allocate statement handle");

	if (OCI_SUCCESS != OCIHandleAlloc((dvoid *)xaEnv, (dvoid **)&stmthp,
		OCI_HTYPE_STMT, 0, (dvoid **)0))
		return fatal("ORA:- Unable to allocate error handle");

	/* run the test */
	status = doWork(req->op, req->data, svcCtx, stmthp, errhp, resp);
	btlogger_debug( "TxLog %d: doWork %c returned: %s", status, req->op, resp->data);

//	return status;	// OCI_SUCCESS is 0
	return (status != OCI_SUCCESS);	// 0 means success
}
#endif
