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

#include <iostream>
#include <map>

#include "apr_portable.h"
#include "apr_thread_proc.h"

std::map<apr_os_thread_t, apr_pool_t*> tls_pools;

apr_pool_t* mutex_pool = NULL;
apr_thread_mutex_t* mutex = NULL;

extern int getKey() {
	return -1;
}

extern bool setSpecific(int key, void* threadData) {
        apr_os_thread_t os_th = apr_os_thread_current();

        std::map<apr_os_thread_t, apr_pool_t*>::iterator it = tls_pools.find(os_th);
       
        apr_pool_t * tls_pool = NULL;
 
        if(it == tls_pools.end())
        {
          apr_pool_create(&tls_pool,NULL);
	  
	  if(mutex == NULL)
	  {
	    apr_pool_create(&mutex_pool,NULL);
	    apr_thread_mutex_create(&mutex, 0, mutex_pool);
	  }
          apr_thread_mutex_lock(mutex);
          tls_pools.insert(std::make_pair<apr_os_thread_t, apr_pool_t*>(os_th,tls_pool));
	  apr_thread_mutex_unlock(mutex);

        }
        else
           tls_pool = it->second;

        apr_thread_t* thread = NULL;
	apr_os_thread_put(&thread, &os_th, tls_pool);
	apr_status_t ret = apr_thread_data_set(threadData, (const char*)&key, NULL, thread);
	return (ret == APR_SUCCESS);
}

extern void* getSpecific(int key) {
        apr_os_thread_t os_th = apr_os_thread_current();

        std::map<apr_os_thread_t, apr_pool_t*>::iterator it = tls_pools.find(os_th);
        
        apr_pool_t * tls_pool = NULL; 
        if(it == tls_pools.end())
        {
          return NULL;
        }
        else
        {
           tls_pool = it->second;
        }

        apr_thread_t* thread = NULL;
        apr_os_thread_put(&thread, &os_th, tls_pool);
        void* threadData = NULL;
        apr_thread_data_get(&threadData, (const char*)&key, thread);
	return threadData;
}

extern bool destroySpecific(int key) {
	return setSpecific(key, 0);
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
