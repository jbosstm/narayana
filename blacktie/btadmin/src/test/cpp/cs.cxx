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
#include "btserver.h"

#include "btlogger.h"

#include "xatmi.h"

#include "ace/DLL.h"
#include "ace/OS_NS_stdlib.h"
#include "ace/OS_NS_stdio.h"
#include "ace/OS_NS_string.h"


int main(int argc, char **argv) {

#ifdef WIN32
	ACE_OS::putenv("BLACKTIE_CONFIGURATION=win32");
#else
	ACE_OS::putenv("BLACKTIE_CONFIGURATION=linux");
#endif

	int exit_status = serverinit(argc, argv);

	if (exit_status != -1) {
		//SERVICE_ADVERTISEMENTS
		exit_status = serverrun();
	} else {
		btlogger((char*) "error initialising server");
	}
	serverdone();
	return exit_status;
}
