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
#ifndef BufferConverterImpl_H_
#define BufferConverterImpl_H_

#include "atmiBrokerHybridMacro.h"

#include "log4cxx/logger.h"

/**
 * The buffer converter is used to convert a portable buffer for network transfer
 */
class BLACKTIE_HYBRID_DLL BufferConverterImpl {
public:
	/**
	 * Convert the buffer for on the wire transfer
	 *
	 * @param bufferType The type of the buffer
	 * @param bufferSubtype The identifier of this buffer
	 * @param memoryFormatBuffer The buffer in memory
	 *
	 * @return wireFormatBuffer The converted buffer for sending on the wire
	 * @return The length of the converted buffer
	 */
	static char* convertToWireFormat(char* bufferType, char* bufferSubtype, char* memoryFormatBuffer, long* wireFormatBufferLength);

	/**
	 * Convert the buffer for on the wire transfer
	 *
	 * @param bufferType The type of the buffer
	 * @param bufferSubtype The identifier of this buffer
	 * @param wireFormatBuffer The buffer as received on the wire
	 *
	 * @return memoryFormatBuffer The converted buffer for use in memory
	 * @return The length of the converted buffer
	 */
	static char* convertToMemoryFormat(char* bufferType, char* bufferSubtype, char* wireFormatBuffer, long* memoryFormatBufferLength);
private:
	/**
	 * Used to log the classes information.
	 */
	static log4cxx::LoggerPtr logger;

};

#endif
