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
#include "stdio.h"
#include "string.h"
#include "xatmi.h"
#include "atmiBrokerXatmiMacro.h"
#include "btlogger.h"

BLACKTIE_XATMI_DLL void BAR(TPSVCINFO * svcinfo) {
	int sendlen = 1;
	char* buffer = tpalloc((char*) "X_OCTET", NULL, sendlen);
	buffer[0] = '1';

	btlogger((char*) "BAR Invoked");
	if(strcmp(svcinfo->data, "error_counter_test") == 0) {
		btlogger((char*) "BAR fail");
		tpreturn(TPFAIL, 1, buffer, sendlen, 0);
	} else {
		btlogger((char*) "BAR success");
		tpreturn(TPSUCCESS, 1, buffer, sendlen, 0);
	}
}
