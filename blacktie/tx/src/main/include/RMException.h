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
#ifndef RMEXCEPTION_H
#define RMEXCEPTION_H


#include <stdexcept>
//using std::runtime_error;
using std::invalid_argument;

class RMException : public invalid_argument
{
public:
	RMException(std::string const& msg, int xaErrorCode)
		: invalid_argument(msg), xaErrorCode_(xaErrorCode) {}
	int getXAError() {return xaErrorCode_;}

private:
	int xaErrorCode_;
};

#endif // RMEXCEPTION_H
