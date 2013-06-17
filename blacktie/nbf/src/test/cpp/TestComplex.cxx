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
#include <stdlib.h>
#include "btnbf.h"
#include "xatmi.h"
#include "btlogger.h"

extern "C" {
#include "btclient.h"
}

#include "TestAssert.h"
#include "TestComplex.h"

void TestComplex::setUp() {
}

void TestComplex::tearDown() {
	::clientdone(0);
}

void TestComplex::test_attribute() {
	btlogger((char*) "test_attribute");
	int rc;
	char name[16];
	long id;
	int len = 0; 

	char* employee = tpalloc((char*)"BT_NBF", (char*)"employee", 0);
	BT_ASSERT(employee != NULL);
	rc = btaddattribute(&employee, (char*)"name", (char*)"zhfeng", 6);	
	BT_ASSERT(rc == 0);
	id = 1001;
	rc = btaddattribute(&employee, (char*)"id", (char*)&id, sizeof(long));
	BT_ASSERT(rc == 0);

	char* buf = tpalloc((char*)"BT_NBF", (char*)"test", 0);
	BT_ASSERT(buf != NULL);
	rc = btaddattribute(&buf, (char*)"employee", employee, 0);

	rc = btsetattribute(&employee, (char*)"name", 0, (char*)"tom", 3);
	id = 1002;
	rc = btsetattribute(&employee, (char*)"id", 0, (char*)&id, sizeof(long));
	rc = btaddattribute(&buf, (char*)"employee", employee, 0);
	tpfree(employee);

	int n;
	n = btgetoccurs(buf, (char*)"employee");

	char* tmp_employee;
	for(int i = 0; i < n; i++) {
		btlogger((char*) "get employee for index %d", i);
		rc = btgetattribute(buf, (char*)"employee", i, (char*) &tmp_employee, &len);
		BT_ASSERT(rc == 0);

		//printf("%s\n", tmp_employee);
		btgetattribute(tmp_employee, (char*)"id", 0, (char*) &id, &len);
		len = 16;
		btgetattribute(tmp_employee, (char*)"name", 0, (char*) name, &len);
		btlogger((char*)"id = %d, name = %s", id, name);
		tpfree(tmp_employee);
	}

	rc = btdelattribute(buf, (char*)"employee", 0);
	BT_ASSERT(rc == 0);

	rc = btgetattribute(buf, (char*)"employee", 0, (char*) &tmp_employee, &len);
	BT_ASSERT(rc != 0);

	rc = btgetattribute(buf, (char*)"employee", 1, (char*) &tmp_employee, &len);
	BT_ASSERT(rc == 0);
	rc = btsetattribute(&tmp_employee, (char*)"name", 0, (char*)"another_tom", 12);
	BT_ASSERT(rc == 0);
	rc = btsetattribute(&buf, (char*)"employee", 0, tmp_employee, 0);
	BT_ASSERT(rc == 0);
	printf("%s\n", buf);

	tpfree(tmp_employee);
	tpfree(buf);
}
