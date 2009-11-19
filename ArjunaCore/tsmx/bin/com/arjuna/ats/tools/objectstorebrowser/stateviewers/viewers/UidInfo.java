/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2008,
 * @author JBoss Inc.
 */
package com.arjuna.ats.tools.objectstorebrowser.stateviewers.viewers;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.tools.objectstorebrowser.UidConverter;

import javax.transaction.xa.Xid;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.IOException;

/**
 * Base class for representing common state relevant to Uid's and Xid's
 */
public class UidInfo
{
	private static DateFormat formatter = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss Z");
	private static UidConverter uidConverter;

	public static void setUidConverter(UidConverter uidConverter)
	{
		UidInfo.uidConverter = uidConverter;
	}

	private Uid uid;
	private String instanceName;
	private HeaderInfo header;

	public UidInfo(Uid uid, String instanceName)
	{
		this.uid = uid;
		this.instanceName = instanceName;
	}

	public Uid getUid()
	{
		return uid;
	}

	public String getInstanceName()
	{
		return instanceName;
	}

    public long getCreationTime()
	{
		return header != null ? header.birthDate : -1;
	}

	public long getAge()
	{
		return (getCreationTime() < 0 ? -1 : (System.currentTimeMillis() / 1000) - getCreationTime());
	}

	// static utility methods
	public static String formatTime(long seconds)
	{
		return seconds < 0 ? "" : formatter.format(new Date(seconds * 1000L));
	}

	public static Uid toUid(Xid xid)
	{
		return uidConverter.toUid(xid);
	}

	private static String inet4AddressToString(int ip)
	{
		StringBuffer sb = new StringBuffer(15);

		for (int shift=24; shift > 0; shift -= 8)
		{
			sb.append( Integer.toString((ip >>> shift) & 0xff)).append('.');
		}

		return sb.append( Integer.toString(ip & 0xff)).toString();
	}

	public void setCommitted(InputObjectState os)
	{
		try {
			header = new HeaderInfo(os);
		} catch (IOException e) {
		}
	}

	private class HeaderInfo
	{
		String state;
		Uid txId = Uid.nullUid();
		Uid processUid = Uid.nullUid();
		long birthDate = -1;

		HeaderInfo(InputObjectState os) throws IOException {
			unpackHeader(os);
		}

		void unpackHeader(InputObjectState os) throws IOException {
			if (os != null) {
				state = os.unpackString();
				byte[] txIdBytes = os.unpackBytes();
				txId = new Uid(txIdBytes);

				if (state.equals("#ARJUNA#")) {
					if (!txId.equals(Uid.nullUid())) {
						byte[] pUidBytes = os.unpackBytes();
						processUid = new Uid(pUidBytes);
					}

					birthDate = os.unpackLong() / 1000L;
				}
			}
		}
	}
}
