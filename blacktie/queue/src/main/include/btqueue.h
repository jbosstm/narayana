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

#ifndef BLACKTIE_QUEUE_H
#define BLACKTIE_QUEUE_H

#include "atmiBrokerQueueMacro.h"

#ifdef __cplusplus
extern "C" {
#endif

typedef struct msg_opts {
	int priority;	/* msg priority from 0 (lowest) to 9 - only used with btenqueue */
	long ttl;	/* maximum no of milliseconds before giving up */
	long long schedtime; /* scheduled delivery time (absolute time in millis since epoch ) */
} msg_opts_t;

extern BLACKTIE_QUEUE_DLL int btenqueue(char * svc, msg_opts_t* headers, char* idata, long ilen, long flags); // COMMUNICATION
extern BLACKTIE_QUEUE_DLL int btdequeue(char * svc, msg_opts_t* headers, char ** odata, long *olen, long flags); // COMMUNICATION

#ifdef __cplusplus
}
#endif
#endif // XATMI_H
