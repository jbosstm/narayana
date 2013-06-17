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
#include "Worker.h"

log4cxx::LoggerPtr Worker::logger(log4cxx::Logger::getLogger("Worker"));

Worker::Worker(CORBA::ORB_ptr orb, char* orbName) {
	//m_orb = CORBA::ORB::_duplicate(orb);
	this->orb = orb;
	this->orbName = orbName;
	LOG4CXX_TRACE(logger, "created" << orbName);
}

int Worker::svc(void) {
	try {
		this->orb->run();
		LOG4CXX_TRACE(logger, "terminated");
//		LOG4CXX_TRACE(logger, "terminating" << orbName);
	} catch (CORBA::Exception& e) {
		LOG4CXX_ERROR(logger, (char*) "Unexpected CORBA exception: "
			<< e._name() << " orb: " << orbName);
	} catch (...) {
		LOG4CXX_ERROR(logger, (char*) "Worker caught unknown error running orborb: " << orbName);
	}
	return 0;
}
