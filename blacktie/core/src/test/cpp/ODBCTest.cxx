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
#include <cppunit/TestFixture.h>

#ifdef TODO_WIN32
#include <windows.h>
#include <sql.h>
#include <sqltypes.h>
#include <sqlext.h>
#endif

#include "ODBCTest.h"

void ODBCTest::setUp() {
	init_ace();
	// Perform global set up
	TestFixture::setUp();

	// Perform set up
}

void ODBCTest::tearDown() {
	// Perform clean up

	// Perform global clean up
	TestFixture::tearDown();
}

#ifdef TODO_WIN32
void ODBCTest::test() {
	//Declaration:

	SQLHANDLE hdlEnv, hdlConn, hdlStmt, hdlDbc;
	char* stmt = "SELECT * from NutHead"; //SQL statement? NutHead is the Table name

	//for quickstart
	char *dsnName = "COLLECTOR"; //name of your program or what ever�..
	char* userID = "eXceed";
	char* passwd = "hole";
	char* retVal[256];
	SQLINTEGER cbData;

	SQLRETURN sqlRet = SQLAllocHandle(SQL_HANDLE_ENV, SQL_NULL_HANDLE, &hdlEnv);
	SQLSetEnvAttr(hdlEnv, SQL_ATTR_ODBC_VERSION, (void*) SQL_OV_ODBC3, 0);
	SQLAllocHandle(SQL_HANDLE_DBC, hdlEnv, &hdlConn);
	SQLConnect(hdlConn, (SQLCHAR*) dsnName, SQL_NTS, (SQLCHAR*) userID,
			SQL_NTS, (SQLCHAR*) passwd, SQL_NTS);
	SQLAllocHandle(SQL_HANDLE_STMT, hdlDbc, &hdlStmt);
	SQLExecDirect(hdlStmt, (SQLCHAR*) stmt, SQL_NTS);

	//Initialize the database connection

	while (SQL_SUCCEEDED(sqlRet = SQLFetch(hdlStmt))) {
		SQLGetData(hdlStmt, 0, SQL_C_CHAR, retVal, 256, &cbData);
		std::cout << retVal << std::endl;
	}
	SQLFreeHandle(SQL_HANDLE_STMT, hdlStmt);
	SQLFreeHandle(SQL_HANDLE_DBC, hdlConn);
	SQLFreeHandle(SQL_HANDLE_ENV, hdlEnv); //End the connection
}
#else
void ODBCTest::test() {
	BT_FAIL("NOT IMPLEMENTED");
}
#endif
