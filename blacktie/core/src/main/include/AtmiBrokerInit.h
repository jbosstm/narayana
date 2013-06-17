/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and others contributors as indicated
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
#ifndef _ATMIBROKERINIT_H
#define _ATMIBROKERINIT_H

#include "AtmiBrokerSingleton.h"

class BLACKTIE_CORE_DLL AtmiBrokerInit : public AtmiBrokerSingleton {

private:
    AtmiBrokerInit();
    ~AtmiBrokerInit();
	friend class ACE_Singleton<AtmiBrokerInit, ACE_Recursive_Thread_Mutex>;
};

typedef ACE_Singleton<AtmiBrokerInit, ACE_Recursive_Thread_Mutex> AtmiBrokerInitSingleton;
#if defined (ACE_HAS_EXPLICIT_TEMPLATE_INSTANTIATION)
template class ACE_Singleton<AtmiBrokerInit, ACE_Recursive_Thread_Mutex>;
#elif defined (ACE_HAS_TEMPLATE_INSTANTIATION_PRAGMA)
pragma instantiate ACE_Singleton<AtmiBrokerInit, ACE_Recursive_Thread_Mutex>;
#endif /* ACE_HAS_EXPLICIT_TEMPLATE_INSTANTIATION */

#endif  /* _ATMIBROKERINIT_H */
