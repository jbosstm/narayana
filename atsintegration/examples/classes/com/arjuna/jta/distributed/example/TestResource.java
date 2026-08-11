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

package com.arjuna.jta.distributed.example;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import com.arjuna.ats.arjuna.common.Uid;

/**
 * This is a simple TestResource, any knowledge it has of the rest of the
 * example is purely for debugging. It should be considered a black box.
 */
public class TestResource implements XAResource {
	private Xid xid;

	protected int timeout = 0;

	private File file;

	private String localServerName;

	public TestResource(String localServerName) {
		this.localServerName = localServerName;
	}

	public TestResource(String localServerName, File file) throws IOException {
		this.localServerName = localServerName;
		this.file = file;
		DataInputStream fis = new DataInputStream(new FileInputStream(file));
		final int formatId = fis.readInt();
		final int gtrid_length = fis.readInt();
		final byte[] gtrid = new byte[gtrid_length];
		fis.read(gtrid, 0, gtrid_length);
		final int bqual_length = fis.readInt();
		final byte[] bqual = new byte[bqual_length];
		fis.read(bqual, 0, bqual_length);
		this.xid = new Xid() {

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
		};
	}

	public synchronized int prepare(Xid xid) throws XAException {
		System.out.println("        TestResource (" + localServerName + ")      XA_PREPARE [" + xid + "]");

		File dir = new File(System.getProperty("user.dir") + "/distributedjta-examples/TestResource/" + localServerName + "/");
		dir.mkdirs();
		file = new File(dir, new Uid().fileStringForm() + "_");
		try {
			file.createNewFile();
			final int formatId = xid.getFormatId();
			final byte[] gtrid = xid.getGlobalTransactionId();
			final int gtrid_length = gtrid.length;
			final byte[] bqual = xid.getBranchQualifier();
			final int bqual_length = bqual.length;

			DataOutputStream fos = new DataOutputStream(new FileOutputStream(file));
			fos.writeInt(formatId);
			fos.writeInt(gtrid_length);
			fos.write(gtrid, 0, gtrid_length);
			fos.writeInt(bqual_length);
			fos.write(bqual, 0, bqual_length);
			fos.flush();
			fos.close();
		} catch (IOException e) {
			throw new XAException(XAException.XAER_RMERR);
		}
		return XA_OK;
	}

	public synchronized void commit(Xid id, boolean onePhase) throws XAException {
		System.out.println("        TestResource (" + localServerName + ")      XA_COMMIT  [" + id + "]");
		if (file != null) {
			file.delete();
		}
		this.xid = null;
	}

	public synchronized void rollback(Xid xid) throws XAException {
		System.out.println("        TestResource (" + localServerName + ")      XA_ROLLBACK[" + xid + "]");
		if (file != null) {
			file.delete();
		}
		this.xid = null;
	}

	public void start(Xid xid, int flags) throws XAException {
		System.out.println("        TestResource (" + localServerName + ")      XA_START   [" + xid + "] Flags=" + flags);
	}

	public void end(Xid xid, int flags) throws XAException {
		System.out.println("        TestResource (" + localServerName + ")      XA_END     [" + xid + "] Flags=" + flags);
	}

	public void forget(Xid xid) throws XAException {
		System.out.println("        TestResource (" + localServerName + ")      XA_FORGET[" + xid + "]");
	}

	public int getTransactionTimeout() throws XAException {
		return (timeout);
	}

	public boolean isSameRM(XAResource xares) throws XAException {
		if (xares instanceof TestResource) {
			TestResource other = (TestResource) xares;
			if ((this.xid != null && other.xid != null)) {
				if (this.xid.getFormatId() == other.xid.getFormatId()) {
					if (Arrays.equals(this.xid.getGlobalTransactionId(), other.xid.getGlobalTransactionId())) {
						if (Arrays.equals(this.xid.getBranchQualifier(), other.xid.getBranchQualifier())) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public Xid[] recover(int flag) throws XAException {
		Xid[] toReturn = null;
		if ((flag & XAResource.TMSTARTRSCAN) == XAResource.TMSTARTRSCAN) {
			System.out.println("        TestResource (" + localServerName + ")      RECOVER[XAResource.TMSTARTRSCAN]: " + localServerName);
			if (xid != null) {
				toReturn = new Xid[] { xid };
			}
		}
		if ((flag & XAResource.TMENDRSCAN) == XAResource.TMENDRSCAN) {
			System.out.println("        TestResource (" + localServerName + ")      RECOVER[XAResource.TMENDRSCAN]: " + localServerName);
		}
		if (flag == XAResource.TMNOFLAGS) {
			System.out.println("        TestResource (" + localServerName + ")      RECOVER[XAResource.TMENDRSCAN]: " + localServerName);
		}
		return toReturn;
	}

	public boolean setTransactionTimeout(int seconds) throws XAException {
		timeout = seconds;
		return (true);
	}
}
