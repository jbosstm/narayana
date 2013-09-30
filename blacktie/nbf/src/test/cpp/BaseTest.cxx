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
#include "ThreadLocalStorage.h"

#include "xatmi.h"
extern "C" {
#include "btclient.h"
}

#include "malloc.h"

void BaseTest::setUp() {
	// Perform global set up
	TestFixture::setUp();
	// previous tests may have left a txn on the thread
	destroySpecific(TSS_KEY);
}

void BaseTest::tearDown() {
	// Perform clean up
	::clientdone(0);
	char* tperrnoS = (char*) malloc(110);
	sprintf(tperrnoS, "%d", tperrno);
	BT_ASSERT_MESSAGE(tperrnoS, tperrno == 0);
	free(tperrnoS);
	// previous tests may have left a txn on the thread
	destroySpecific(TSS_KEY);

	// Perform global clean up
	TestFixture::tearDown();
}

