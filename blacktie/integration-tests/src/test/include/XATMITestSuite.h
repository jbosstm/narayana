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
#ifndef XATMITESTSUITE_H
#define XATMITESTSUITE_H

struct bigdata_t {
	char status[1050];
};
typedef struct bigdata_t BIGDATA;

struct acct_info_t {
	long acct_no;
	char name[50];
	char address[100];
	float foo[2];
	double balances[2];
};
typedef struct acct_info_t ACCT_INFO;

struct deposit_t {
	long acct_no;
	short amount;
	short balance;
	char status[128];
	short status_len;
};
typedef struct deposit_t DEPOSIT;

struct data_buffer_t {
	char input[100];
	int output;
	int failTest;
};
typedef struct data_buffer_t DATA_BUFFER;

#define OK 1
#define NOT_OK -1

#define OK 1
#define NOT_OK -1

#endif
