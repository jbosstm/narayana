/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors 
 * as indicated by the @author tags. 
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
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2004,
 *
 * Arjuna Technologies Ltd,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: RecoveryXAResource.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jta.recovery;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

public class XARRTestResource implements XAResource {

    private Map<File,Xid> xids = new HashMap<File, Xid>();
    private File file;
    private String xarrHelper;

    public XARRTestResource() {
    }

    public XARRTestResource(String xarrHelper, File file) throws IOException {
        this.xarrHelper = xarrHelper;
        this.file = file;
        DataInputStream fis = new DataInputStream(new FileInputStream(file));
        final int formatId = fis.readInt();
        final int gtrid_length = fis.readInt();
        final byte[] gtrid = new byte[gtrid_length];
        fis.read(gtrid, 0, gtrid_length);
        final int bqual_length = fis.readInt();
        final byte[] bqual = new byte[bqual_length];
        fis.read(bqual, 0, bqual_length);
        fis.close();
        xids.put(file, new Xid() {

            @Override
            public byte[] getGlobalTransactionId() {
                return gtrid;
            }

            @Override
            public int getFormatId() {
                return formatId;
            }

            @Override
            public byte[] getBranchQualifier() {
                return bqual;
            }
        });
        fis.close();
    }

    public Xid[] recover(int flag) throws XAException {
        System.out.println("XARRTestResource XA_RECOVER [" + xarrHelper + "] [" + xids.size() + "]");
        return xids.values().toArray(new Xid[] {});
    }

    public int prepare(Xid xid) throws XAException {
        System.out.println("XARRTestResource XA_PREPARE [" + xid + "]");
        try {
            final int formatId = xid.getFormatId();
            final byte[] gtrid = xid.getGlobalTransactionId();
            final int gtrid_length = gtrid.length;
            final byte[] bqual = xid.getBranchQualifier();
            final int bqual_length = bqual.length;

            File file = new File("XARR.txt");
            DataOutputStream fos = new DataOutputStream(new FileOutputStream(file));
            fos.writeInt(formatId);
            fos.writeInt(gtrid_length);
            fos.write(gtrid, 0, gtrid_length);
            fos.writeInt(bqual_length);
            fos.write(bqual, 0, bqual_length);
            fos.flush();
            fos.close();
            return XAResource.XA_OK;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new XAException();
        } catch (IOException e) {
            e.printStackTrace();
            throw new XAException();
        }
    }

    public void commit(Xid xid, boolean onePhase) throws XAException {
        System.out.println("XARRTestResource XA_COMMIT [" + xarrHelper + "] [" + xid + "]");
        if (!this.file.exists()) {
            throw new XAException();
        } else {
            file.delete();
            xids.remove(file);
        }
    }

    public void rollback(Xid xid) throws XAException {
        System.out.println("XARRTestResource XA_ROLLBACK [" + xarrHelper + "] [" + xid + "]");
        if (!this.file.exists()) {
            throw new XAException();
        } else {
            file.delete();
            xids.remove(file);
        }
    }

    public boolean isSameRM(XAResource xares) throws XAException {
        return (xares == this);
    }

    public void start(Xid xid, int flags) throws XAException {
    }

    public void end(Xid xid, int flags) throws XAException {
    }

    public void forget(Xid xid) throws XAException {
    }

    public int getTransactionTimeout() throws XAException {
        return 0;
    }

    public boolean setTransactionTimeout(int seconds) throws XAException {
        return true;
    }

}
