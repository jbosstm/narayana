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
#ifndef _TXMANAGERAVOID_H
#define _TXMANAGERAVOID_H


extern int txManager_begin(void);
extern int txManager_close(void);
extern int txManager_commit(void);
extern int txManager_open(void);
extern int txManager_rollback(void);
extern int txManager_set_commit_return(long);
extern int txManager_set_transaction_control(long control);
extern int txManager_set_transaction_timeout(long timeout);
extern int txManager_info(void *);

#endif
