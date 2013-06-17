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
#include "xatmi.h"

extern "C"
JNIEXPORT jboolean JNICALL Java_org_jboss_narayana_blacktie_jatmibroker_RunClient_test_1x_1octet
(JNIEnv *, jobject) {
	bool success = false;
	char* toSend = tpalloc((char*) "X_OCTET", NULL, 10);
	char* toRecv = tpalloc((char*) "X_OCTET", NULL, 10);
	strcpy(toSend, (char*) "123456789");
	long rcvLen = 0;
	int status = tpcall((char*) "LOOPY", toSend, 10, &toRecv, &rcvLen, 0);
	if (tperrno == 0) {
		if (status == 0) {
			if (strcmp(toRecv, (char*) "987654321") == 0) {
				success = true;
			}
		}
	}
	return success;
}
