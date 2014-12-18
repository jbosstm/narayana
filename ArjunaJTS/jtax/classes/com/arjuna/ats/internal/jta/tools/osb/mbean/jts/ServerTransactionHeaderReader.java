/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.arjuna.ats.internal.jta.tools.osb.mbean.jts;

import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.tools.osb.mbean.HeaderState;
import com.arjuna.ats.arjuna.tools.osb.mbean.HeaderStateReader;

import java.io.IOException;

/**
 * ObjectStore record header reader for ServerTransaction
 *
 * @author Mike Musgrove
 */
public class ServerTransactionHeaderReader extends HeaderStateReader {
    protected HeaderState unpackHeader(InputObjectState os) throws IOException {
        boolean haveRecCoord = os.unpackBoolean();

        if (haveRecCoord)
            os.unpackString(); // read ior

        return super.unpackHeader(os);
    }
}
