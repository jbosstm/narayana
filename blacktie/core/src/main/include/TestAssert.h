/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and others contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conds
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
#ifndef _TEST_ASSERT_H_
#define _TEST_ASSERT_H_

#include <cppunit/extensions/HelperMacros.h>
#include "btlogger.h"

#define	BTDBGPOST	\
	btlogger_debug("POST ASSERT %s:%d", __FILE__, __LINE__)
#define	BTDBGPRE	\
	btlogger_debug("PRE ASSERT %s:%d", __FILE__, __LINE__)
#define	CHKCOND(cond)	\
	if (!(cond)) btlogger_debug("CHKCOND ASSERT FAILED %s:%d", __FILE__, __LINE__)
#define	CHKCOND_MESSAGE(message,cond)	\
	if (!(cond)) btlogger_debug("CHKCOND ASSERT FAILED %s:%d - %s", __FILE__, __LINE__, message)

#define	BT_ASSERT(cond)	{\
	bool _w12pq_u = (cond);	\
	CHKCOND(_w12pq_u);	\
	CPPUNIT_ASSERT(_w12pq_u);}

#define	BT_ASSERT_MESSAGE(message,cond)	{\
	bool _w12pq_u = (cond);	\
	CHKCOND_MESSAGE(message, _w12pq_u);	\
	CPPUNIT_ASSERT_MESSAGE((message),_w12pq_u);} 

#define BT_ASSERT_EQUAL(expected,actual)	\
	BTDBGPRE;CPPUNIT_ASSERT_EQUAL((expected),(actual));BTDBGPOST
#define BT_ASSERT_EQUAL_MESSAGE(message,expected,actual)	\
	BTDBGPRE;CPPUNIT_ASSERT_EQUAL_MESSAGE((message),(expected),(actual));BTDBGPOST

#define BT_FAIL(message)	\
	btlogger_debug("ASSERT FAIL %s:%d", __FILE__, __LINE__);CPPUNIT_FAIL((message))

#define BT_ASSERT_DOUBLES_EQUAL(expected,actual,delta)	\
	BTDBGPRE;CPPUNIT_ASSERT_DOUBLES_EQUAL((expected),(actual),(delta));BTDBGPOST
#define BT_ASSERT_DOUBLES_EQUAL_MESSAGE(message,expected,actual,delta)	\
	BTDBGPRE;CPPUNIT_ASSERT_DOUBLES_EQUAL_MESSAGE((message),(expected),(actual),(delta));BTDBGPOST
#define BT_ASSERT_THROW(expression, ExceptionType)	\
	BTDBGPRE;CPPUNIT_ASSERT_THROW((expression), (ExceptionType));BTDBGPOST
#define BT_ASSERT_THROW_MESSAGE(message, expression, ExceptionType)	\
	BTDBGPRE;CPPUNIT_ASSERT_THROW_MESSAGE((message), (expression), (ExceptionType));BTDBGPOST
#define BT_ASSERT_NO_THROW(expression)	\
	BTDBGPRE;CPPUNIT_ASSERT_NO_THROW((expression)) ;BTDBGPOST

#define BT_ASSERT_ASSERTION_FAIL(assertion)	\
	BTDBGPRE;CPPUNIT_ASSERT_ASSERTION_FAIL((assertion));BTDBGPOST
#define BT_ASSERT_ASSERTION_FAIL_MESSAGE(message, assertion)	\
	BTDBGPRE;CPPUNIT_ASSERT_ASSERTION_FAIL_MESSAGE((message), (assertion));BTDBGPOST
#define BT_ASSERT_ASSERTION_PASS(assertion)	\
	BTDBGPRE;CPPUNIT_ASSERT_ASSERTION_PASS((assertion));BTDBGPOST
#define BT_ASSERT_ASSERTION_PASS_MESSAGE(message, assertion)	\
	BTDBGPRE;CPPUNIT_ASSERT_ASSERTION_PASS_MESSAGE((message), (assertion));BTDBGPOST

#endif /*_TEST_ASSERT_H_*/
