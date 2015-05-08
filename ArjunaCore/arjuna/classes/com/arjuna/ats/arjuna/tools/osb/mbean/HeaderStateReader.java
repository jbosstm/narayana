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
package com.arjuna.ats.arjuna.tools.osb.mbean;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.state.InputObjectState;

import java.io.IOException;

/**
 * Per record header reader. The majority of records pack the header state {@link HeaderState} first.
 * This class unpacks that state. If a record type packs information before the standard header
 * then a subclass of this type should be provided to
 * {@link com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreBrowser#registerHandler}. For example
 * see {@link com.arjuna.ats.internal.jta.tools.osb.mbean.jts.ServerTransactionHeaderReader}
 *
 * @author Mike Musgrove
 */
/**
 * @deprecated as of 5.0.5.Final In a subsequent release we will change packages names in order to 
 * provide a better separation between public and internal classes.
 */
@Deprecated // in order to provide a better separation between public and internal classes.
public class HeaderStateReader {
    protected HeaderState unpackHeader(InputObjectState os) throws IOException {
        HeaderState hs = null;

        if (os != null) {

            String state = os.unpackString();
            byte[] txIdBytes = os.unpackBytes();
            Uid txUid = new Uid(txIdBytes);
            Uid processUid = null;
            long birthDate = 0;

            if (state.equals("#ARJUNA#")) {
                if (!txUid.equals(Uid.nullUid())) {
                    byte[] pUidBytes = os.unpackBytes();
                    processUid = new Uid(pUidBytes);
                }

                birthDate = os.unpackLong();
            }

            hs = new HeaderState(state, txUid, processUid, birthDate);
        }

        return hs;
    }
}
