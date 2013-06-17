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
#include "ThreadLocalStorage.h"

#include "ace/TSS_T.h"
#include <map>
#include <iostream>

class TSSData {
public:
	~TSSData() {
	}
	bool set(int k, void* v) {
		tssmap[k] = v;
		return true;
	}
	void* get(int k) {
		return tssmap[k];
	}
	bool destroy(int k) {
		tssmap[k] = 0;
		return true;
	}

	std::map<int, void*> tssmap;
};

static ACE_TSS<TSSData> tss;

extern int getKey() {
	return -1;
}

extern bool setSpecific(int key, void* threadData) {
	return tss->set(key, threadData);
}

extern void* getSpecific(int key) {
	return tss->get(key);
}

extern bool destroySpecific(int key) {
	return tss->destroy(key);
}

char* TSS_TPERESET = (char*) "0";
char* TSS_TPEBADDESC = (char*) "2";
char* TSS_TPEBLOCK = (char*) "3";
char* TSS_TPEINVAL = (char*) "4";
char* TSS_TPELIMIT = (char*) "5";
char* TSS_TPENOENT = (char*) "6";
char* TSS_TPEOS = (char*) "7";
char* TSS_TPEPROTO = (char*) "9";
char* TSS_TPESVCERR = (char*) "10";
char* TSS_TPESVCFAIL = (char*) "11";
char* TSS_TPESYSTEM = (char*) "12";
char* TSS_TPETIME = (char*) "13";
char* TSS_TPETRAN = (char*) "14";
char* TSS_TPGOTSIG = (char*) "15";
char* TSS_TPEITYPE = (char*) "17";
char* TSS_TPEOTYPE = (char*) "18";
char* TSS_TPEEVENT = (char*) "22";
char* TSS_TPEMATCH = (char*) "23";
