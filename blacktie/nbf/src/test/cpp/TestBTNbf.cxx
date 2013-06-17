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
#include "TestBTNbf.h"

void TestBTNbf::setUp() {
}

void TestBTNbf::tearDown() {
	::clientdone(0);
}

void TestBTNbf::test_addattribute() {
	btlogger((char*) "test_addattribute");
	int rc;
	/*
	char* s = (char*)
		"<?xml version='1.0' ?> \
			<employee xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\
				xmlns=\"http://www.jboss.org/blacktie\" \
				xsi:schemaLocation=\"http://www.jboss.org/blacktie buffers/employee.xsd\"> \
			</employee>";
	char* buf = (char*) malloc (sizeof(char) * (strlen(s) + 1));
	strcpy(buf, s);
	*/
	char name[16];
	char value[16];
	int len = 16;;
	char* buf = tpalloc((char*)"BT_NBF", (char*)"employee", 0);

	BT_ASSERT(buf != NULL);
	strcpy(name, "test");
	rc = btaddattribute(&buf, (char*)"name", name, strlen(name));	
	BT_ASSERT(rc == 0);

	rc = btgetattribute(buf, (char*)"name", 0, (char*)value, &len);
	BT_ASSERT(rc == 0);
	BT_ASSERT(len == 4);
	BT_ASSERT(strcmp(value, "test") == 0);

	long empid = 1234;
	long id = 0;
	rc = btaddattribute(&buf, (char*)"id", (char*)&empid, sizeof(empid));
	BT_ASSERT(rc == 0);

	rc = btgetattribute(buf, (char*)"id", 0, (char*)&id, &len);
	BT_ASSERT(rc == 0);
	BT_ASSERT(len == sizeof(long));
	BT_ASSERT(id == empid);

	rc = btaddattribute(&buf, (char*)"unknow", NULL, 0);
	BT_ASSERT(rc == -1);

	tpfree(buf);
}

void TestBTNbf::test_getattribute() {
	btlogger((char*) "test_getattribute");
	int rc;
	char name[16];
	int len = 16;
	long id = 0;
	/*
	char* buf = (char*)
		"<?xml version='1.0' ?> \
			<employee xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\
				xmlns=\"http://www.jboss.org/blacktie\" \
				xsi:schemaLocation=\"http://www.jboss.org/blacktie buffers/employee.xsd\"> \
				<name>zhfeng</name> \
				<name>test</name> \
				<id>1001</id> \
				<id>1002</id> \
			</employee>";
	*/
	char* buf = tpalloc((char*)"BT_NBF", (char*)"employee", 0);
	btaddattribute(&buf, (char*)"name", (char*)"zhfeng", 6);
	btaddattribute(&buf, (char*)"name", (char*)"test", 4);
	id = 1001;
	btaddattribute(&buf, (char*)"id", (char*)&id, sizeof(id));
	id = 1002;
	btaddattribute(&buf, (char*)"id", (char*)&id, sizeof(id));

	printf("%s\n", buf);
	btlogger((char*) "getattribute of name at index 0");
	rc = btgetattribute(buf, (char*)"name", 0, (char*)name, &len);
	BT_ASSERT(rc == 0);
	BT_ASSERT(len == 6);
	BT_ASSERT(strcmp(name, "zhfeng") == 0);

	btlogger((char*) "getattribute of name at index 1");
	rc = btgetattribute(buf, (char*)"name", 1, (char*)name, &len);
	BT_ASSERT(rc == 0);
	BT_ASSERT(len == 4);
	BT_ASSERT(strcmp(name, "test") == 0);

	len = 0;
	btlogger((char*) "getattribute of id at index 0");
	rc = btgetattribute(buf, (char*)"id", 0, (char*)&id, &len);
	BT_ASSERT(rc == 0);
	btlogger((char*)"len is %d, id is %lu", len, id);
	BT_ASSERT(len == sizeof(long));
	BT_ASSERT(id == 1001);

	len = 0;
	btlogger((char*) "getattribute of id at index 1");
	rc = btgetattribute(buf, (char*)"id", 1, (char*)&id, &len);
	BT_ASSERT(rc == 0);
	BT_ASSERT(len == sizeof(long));
	BT_ASSERT(id == 1002);

	tpfree(buf);
}

void TestBTNbf::test_setattribute() {
	btlogger((char*) "test_setattribute");
	int rc;
	/*
	char* s = (char*)
		"<?xml version='1.0' ?> \
			<employee xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\
				xmlns=\"http://www.jboss.org/blacktie\" \
				xsi:schemaLocation=\"http://www.jboss.org/blacktie buffers/employee.xsd\"> \
				<name>test1</name> \
				<name>test2</name> \
			</employee>";
	char* buf = (char*) malloc (sizeof(char) * (strlen(s) + 1));
	strcpy(buf, s);
	*/
	char* buf = tpalloc((char*)"BT_NBF", (char*)"employee", 0);
	btaddattribute(&buf, (char*)"name", (char*)"test1", 5);
	btaddattribute(&buf, (char*)"name", (char*)"test2", 5);

	long id = 1234;
	char name[16];
	strcpy(name, "new_test");

	char value[16];
	int len = 16;

	rc = btsetattribute(&buf, (char*) "name", 0, (char*)name, 8);
	BT_ASSERT(rc == 0);

	rc = btgetattribute(buf, (char*) "name", 0, (char*)value, &len);
	BT_ASSERT(rc == 0);
	BT_ASSERT(strcmp(value, name) == 0);
	BT_ASSERT(len == 8);

	btlogger((char*)"set no such id");
	// No such attribute
	rc = btsetattribute(&buf, (char*) "id", 0, (char*)&id, sizeof(id));
	BT_ASSERT(rc != 0);

	// no attribute index
	rc = btsetattribute(&buf, (char*) "name", 2, (char*)name, 8);
	BT_ASSERT(rc != 0);

	tpfree(buf);
}

void TestBTNbf::test_delattribute() {
	btlogger((char*) "test_delattribute");
	int rc;
	char name[16];
	int  len = 16;
	/*
	char* s = (char*)
		"<?xml version='1.0' ?> \
			<employee xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\
				xmlns=\"http://www.jboss.org/blacktie\" \
				xsi:schemaLocation=\"http://www.jboss.org/blacktie buffers/employee.xsd\"> \
				<name>zhfeng</name> \
				<name>test</name> \
				<id>1001</id> \
				<id>1002</id> \
			</employee>";
	char* buf = (char*) malloc (sizeof(char) * (strlen(s) + 1));
	strcpy(buf, s);
	*/
	char* buf = tpalloc((char*)"BT_NBF", (char*)"employee", 0);
	btaddattribute(&buf, (char*)"name", (char*)"zhfeng", 6);
	btaddattribute(&buf, (char*)"name", (char*)"test", 4);
	long id = 1001;
	btaddattribute(&buf, (char*)"id", (char*)&id, sizeof(id));

	rc = btgetattribute(buf, (char*) "name", 0, (char*)name, &len);
	BT_ASSERT(rc == 0);
	BT_ASSERT(len == 6);
	BT_ASSERT(strcmp(name, "zhfeng") == 0);

	rc = btdelattribute(buf, (char*) "name", 0);
	BT_ASSERT(rc == 0);

	rc = btdelattribute(buf, (char*) "id", 0);
	BT_ASSERT(rc == 0);

	// double delete failed
	rc = btdelattribute(buf, (char*) "id", 0);
	BT_ASSERT(rc != 0);

	// can not get deleted index
	rc = btgetattribute(buf, (char*) "name", 0, (char*)name, &len);
	BT_ASSERT(rc != 0);

	len = 0;
	rc = btgetattribute(buf, (char*) "id", 0, (char*)&id, &len);
	BT_ASSERT(rc != 0);

	len = 16;
	rc = btgetattribute(buf, (char*) "name", 1, (char*)name, &len);
	BT_ASSERT(rc == 0);
	BT_ASSERT(len == 4);
	BT_ASSERT(strcmp(name, "test") == 0);

	// no such attribute
	rc = btdelattribute(buf, (char*) "unknow", 2);
	BT_ASSERT(rc != 0);

	// no such attribute id
	rc = btdelattribute(buf, (char*) "name", 3);
	BT_ASSERT(rc != 0);

	tpfree(buf);
}

void TestBTNbf::test_getoccurs() {
	btlogger((char*) "test_getoccurs");
	char* buf = tpalloc((char*)"BT_NBF", (char*)"employee", 0);
	int rc;

	BT_ASSERT(buf != NULL);
	rc = btaddattribute(&buf, (char*)"name", (char*)"zhfeng", 6);
	BT_ASSERT(rc == 0);
	rc = btaddattribute(&buf, (char*)"name", (char*)"test", 4);
	BT_ASSERT(rc == 0);

	rc = btgetoccurs(buf, (char*)"name");
	BT_ASSERT(rc == 2);
}
