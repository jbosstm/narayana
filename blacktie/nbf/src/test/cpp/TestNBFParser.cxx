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
#include "btlogger.h"
#include "TestAssert.h"

#include "TestNBFParser.h"
#include "NBFParser.h"
#include "btnbf.h"
#include "xatmi.h"

void TestNBFParser::test_string_buf() {
	btlogger((char*) "test_string_buf");

	int rc;
	char* buf = tpalloc((char*)"BT_NBF", (char*)"employee", 0);
	BT_ASSERT(buf != NULL);
	rc = btaddattribute(&buf, (char*)"name", (char*)"zhfeng", 6);	
	BT_ASSERT(rc == 0);

	// Cant do this on WIN32 as the parser isn't in src/main/export
#ifndef WIN32
	NBFParser nbf;
	NBFParserHandlers handler("name", 0);
	BT_ASSERT(nbf.parse(buf, "employee", &handler));
#endif

	::tpfree(buf);
}
