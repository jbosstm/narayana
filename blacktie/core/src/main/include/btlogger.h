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
#ifndef USERLOG_H
#define USERLOG_H

#include "atmiBrokerCoreMacro.h"
#include <string.h>

#ifdef __cplusplus
extern "C" {
#endif
extern BLACKTIE_CORE_DLL void btlogger_init();
extern BLACKTIE_CORE_DLL void btlogger(const char * format, ...);
extern BLACKTIE_CORE_DLL void btlogger_trace(const char * format, ...);
extern BLACKTIE_CORE_DLL void btlogger_debug(const char * format, ...);
extern BLACKTIE_CORE_DLL void btlogger_warn(const char * format, ...);
extern BLACKTIE_CORE_DLL int btlogger_snprintf(char *str, size_t size, const char * format, ...);
#ifdef __cplusplus
}
#endif

#endif
