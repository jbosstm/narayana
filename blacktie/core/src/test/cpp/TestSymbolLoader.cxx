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

#include "TestSymbolLoader.h"

#include "AtmiBrokerEnv.h"
#include "btlogger.h"
#include "SymbolLoader.h"
#include <stdlib.h>

void TestSymbolLoader::setUp() {

	// Perform global set up
	TestFixture::setUp();

#ifdef WIN32
	putenv("BLACKTIE_CONFIGURATION=win32");
#else
	putenv("BLACKTIE_CONFIGURATION=linux");
#endif

}

void TestSymbolLoader::tearDown() {
	// Perform clean up
	putenv("BLACKTIE_CONFIGURATION=");

	// Perform global clean up
	TestFixture::tearDown();
}

void TestSymbolLoader::test() {
	AtmiBrokerEnv* env = AtmiBrokerEnv::get_instance();

	char* lib = (char *) env->getenv((char *) "test-lib");
	char* symbol = (char *) env->getenv((char *) "test-symbol");
	BT_ASSERT(::lookup_symbol(lib, symbol) != NULL);
	btlogger((char*) "found symbol");

	AtmiBrokerEnv::discard_instance();
}
#ifdef WIN32
#define EXPORT_SYMBOL __declspec(dllexport)
#else
#define EXPORT_SYMBOL
#endif

#ifdef __cplusplus
extern "C" {
#endif
EXPORT_SYMBOL void checkIfSymbolsCanBeLoadedFromMainExecutableOnAllPlatforms() {
}
#ifdef __cplusplus
}
#endif

void TestSymbolLoader::test_executable() {
#ifdef WIN32
	BT_ASSERT(::lookup_symbol("testsuite.exe",
			"checkIfSymbolsCanBeLoadedFromMainExecutableOnAllPlatforms")
			!= NULL);
#else
	BT_ASSERT(::lookup_symbol(NULL,
			"checkIfSymbolsCanBeLoadedFromMainExecutableOnAllPlatforms")
			!= NULL);
#endif
	btlogger((char*) "found symbol");
}
