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

#ifndef BLACKTIEMEM_H
#define BLACKTIEMEM_H

#include <iostream>
#include <vector>
#include "log4cxx/logger.h"
#include "SynchronizableObject.h"
#define MAX_TYPE_SIZE 8
#define MAX_SUBTYPE_SIZE 16
struct _memory_info {
	char* memoryPtr;
	char* type;
	char* subtype;
	int size;
	bool forcedDelete;
};
typedef _memory_info MemoryInfo;

class AtmiBrokerMem {

public:

	AtmiBrokerMem();

	~AtmiBrokerMem();

	char* tpalloc(char* type, char* subtype, long size, bool serviceAllocated);

	char* tprealloc(char * addr, long size, char* type, char* subtype,
			bool force);

	void tpfree(char* ptr, bool force);

	long tptypes(char* ptr, char* type, char* subtype);

	static AtmiBrokerMem* get_instance();
	static void discard_instance();

private:

	static SynchronizableObject* lock;
	static log4cxx::LoggerPtr logger;
	std::vector<MemoryInfo> memoryInfoVector;

	static AtmiBrokerMem * ptrAtmiBrokerMem;

};

#endif
