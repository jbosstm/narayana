/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.jta.xa;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import javax.transaction.xa.Xid;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.internal.arjuna.FormatConstants;
import com.arjuna.ats.internal.jta.resources.arjunacore.XAResourceRecordWrappingPlugin;
import com.arjuna.ats.internal.jta.xa.XID;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jta.logging.jtaLogger;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: XATxConverter.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class XATxConverter
{
	private static XAResourceRecordWrappingPlugin xaResourceRecordWrappingPlugin =
            jtaPropertyManager.getJTAEnvironmentBean().getXAResourceRecordWrappingPlugin();
    public static final int FORMAT_ID = FormatConstants.JTA_FORMAT_ID; // different from JTS ones.

    static XID getXid (Uid uid, boolean branch, Integer eisName) throws IllegalStateException
    {
        if (branch)
            return getXid(uid, new Uid(), FORMAT_ID, eisName);
        else
            return getXid(uid, Uid.nullUid(), FORMAT_ID, eisName);
    }

    public static Xid getXid (Uid uid, boolean branch, int formatId) throws IllegalStateException
    {
        XID xid;
        if (branch)
            xid = getXid(uid, new Uid(), formatId, null);
        else
            xid = getXid(uid, Uid.nullUid(), formatId, null);

        return new XidImple(xid);
    }

    private static XID getXid(Uid uid, Uid branch, int formatId, Integer eisName) throws IllegalStateException
    {
        if (uid == null) {
    	    throw new IllegalStateException();
        }

        XID xid = new XID();
        xid.formatID = formatId;

        // gtrid is uid byte form followed by as many chars of the node name as will fit.
        byte[] gtridUid = uid.getBytes();

        if (gtridUid.length > XID.MAXGTRIDSIZE) {
            throw new IllegalStateException(); // Uid is too long!!!!
        }

        byte[] nodeNameBytes = TxControl.getXaNodeNameBytes();

        if (nodeNameBytes == null) {
            throw new IllegalStateException(jtaLogger.i18NLogger.get_nodename_null());
        }

        int nodeNameLengthToUse = nodeNameBytes.length;
        xid.gtrid_length = gtridUid.length+nodeNameLengthToUse;

        // src, srcPos, dest, destPos, length
        System.arraycopy(gtridUid, 0, xid.data, 0, gtridUid.length);
        System.arraycopy(nodeNameBytes, 0, xid.data, gtridUid.length, nodeNameLengthToUse);
        
        
        if (branch.notEquals(Uid.nullUid()))
		{
            // bqual is uid byte form plus EIS name.
            byte[] bqualUid = branch.getBytes();

            if (bqualUid.length > XID.MAXBQUALSIZE) {
                throw new IllegalStateException(); // Uid is too long!!!!
            }

            int spareBqualBytes = XID.MAXBQUALSIZE - (bqualUid.length + 4);
           
            xid.bqual_length = bqualUid.length+4+4;

            // src, srcPos, dest, destPos, length
            int offset = xid.gtrid_length;
            System.arraycopy (bqualUid, 0, xid.data, offset, bqualUid.length);
            setEisName(xid, eisName);
        }
		else
		{
		    /*
		     * Note: for some dbs we seem to be able to get
		     * away with setting the size field to the size
		     * of the actual branch. However, for Oracle,
		     * it appears as though it must always be 64.
		     * (At least for zero branches.)
		     */
		    xid.data[xid.gtrid_length] = (byte) 0;
		    xid.bqual_length = 64;
		}

        return xid;
    }

    public static Uid getUid(XID xid)
    {
        if (xid == null || !FormatConstants.isNarayanaFormatId(xid.formatID)) {
            return Uid.nullUid();
        }

        // The Uid byte are the first in the data array, so we just pass
        // in the whole thing and the additional trailing data is ignored.
        Uid tx = new Uid(xid.data);
        return tx;
    }

    public static void setNodeName(XID theXid, String nodeName) {
        if (theXid == null || !FormatConstants.isNarayanaFormatId(theXid.formatID)) {
            return ;
        }

        byte[] oldNodeName = getNodeName(theXid).getBytes(StandardCharsets.UTF_8);
        byte[] newNodeName = nodeName.getBytes(StandardCharsets.UTF_8);

        // length of the uid is of theXid.gtrid_length
        byte[] dataUid = Arrays.copyOfRange(theXid.data, 0, theXid.gtrid_length - oldNodeName.length);
        // length of real data is theXid.bqual_length
        byte[] databranch = Arrays.copyOfRange(theXid.data, theXid.gtrid_length, theXid.bqual_length);

        // redefine lenght of the gtrid based on the new node name
        theXid.gtrid_length = dataUid.length + newNodeName.length;

        // taking old data concatenating them with the new node name
        System.arraycopy(dataUid, 0, theXid.data, 0, dataUid.length);
        System.arraycopy(newNodeName, 0, theXid.data, dataUid.length, newNodeName.length);
        System.arraycopy(databranch, 0, theXid.data, theXid.gtrid_length, databranch.length);
    }

	public static String getNodeName(XID xid) {
		if (xid == null || !FormatConstants.isNarayanaFormatId(xid.formatID)) {
			return null;
		}

		Xid xidImple = new XidImple(xid);
		byte[] globalTransactionId = xidImple.getGlobalTransactionId();

		// the node name follows the Uid with no separator, so the only
		// way to tell where it starts is to figure out how long the Uid is.
		int offset = Uid.UID_SIZE;

		return new String(Arrays.copyOfRange(globalTransactionId, offset, globalTransactionId.length), StandardCharsets.UTF_8);
	}

	public static void setSubordinateNodeName(XID theXid, String xaNodeName) {
		if (theXid == null || !FormatConstants.isNarayanaFormatId(theXid.formatID)) {
			return;
		}
		int length = 0;
		if (xaNodeName != null) {
			length = xaNodeName.length();
		}
		int offset = theXid.gtrid_length + Uid.UID_SIZE + 4;
		theXid.data[offset++] = (byte) (length >>> 24);
		theXid.data[offset++] = (byte) (length >>> 16);
		theXid.data[offset++] = (byte) (length >>> 8);
		theXid.data[offset++] = (byte) (length >>> 0);
		if (length > 0) {
			byte[] nameAsBytes = xaNodeName.getBytes(StandardCharsets.UTF_8);
			System.arraycopy(nameAsBytes, 0, theXid.data, offset, length);
		}
		theXid.bqual_length = Uid.UID_SIZE+4+4+length;
	}
	public static String getSubordinateNodeName(XID xid) {
		if (xid == null || !FormatConstants.isNarayanaFormatId(xid.formatID)) {
			return null;
		}

		Xid xidImple = new XidImple(xid);
		byte[] branchQualifier = xidImple.getBranchQualifier();

		// the node name follows the Uid with no separator, so the only
		// way to tell where it starts is to figure out how long the Uid is.
		int offset = Uid.UID_SIZE + 4;

		int length = (branchQualifier[offset++] << 24)
				+ ((branchQualifier[offset++] & 0xFF) << 16)
				+ ((branchQualifier[offset++] & 0xFF) << 8)
				+ (branchQualifier[offset++] & 0xFF);
		if (length > 0) {
			return new String(Arrays.copyOfRange(branchQualifier, offset, offset+length), StandardCharsets.UTF_8);
		} else {
			return null;
		}
	}


	public static void setBranchUID(XID xid, Uid uid) {
		if (xid == null || !FormatConstants.isNarayanaFormatId(xid.formatID)) {
			return;
		}

		byte[] bqual = uid.getBytes();
		System.arraycopy(bqual, 0, xid.data, xid.gtrid_length, Uid.UID_SIZE);
	}
	
    public static Uid getBranchUid(XID xid)
    {
        if (xid == null || !FormatConstants.isNarayanaFormatId(xid.formatID)) {
            return Uid.nullUid();
        }

        byte[] bqual = new byte[xid.bqual_length];
        System.arraycopy(xid.data, xid.gtrid_length, bqual, 0, xid.bqual_length);

        // The Uid byte are the first in the data array, so we just pass
        // in the whole thing and the additional trailing data is ignored.
        Uid tx = new Uid(bqual);
        return tx;
    }

	public static void setEisName(XID theXid, Integer eisName) {
		if (theXid == null || !FormatConstants.isNarayanaFormatId(theXid.formatID)) {
			return;
		}
		if (eisName == null) {
			eisName = 0;
		}
		int offset = theXid.gtrid_length + Uid.UID_SIZE;
		theXid.data[offset + 0] = (byte) (eisName >>> 24);
		theXid.data[offset + 1] = (byte) (eisName >>> 16);
		theXid.data[offset + 2] = (byte) (eisName >>> 8);
		theXid.data[offset + 3] = (byte) (eisName >>> 0);
	}

    public static Integer getEISName(XID xid)
    {
        if (xid == null || !FormatConstants.isNarayanaFormatId(xid.formatID)) {
            return -1;
        }

		// the node name follows the Uid with no separator, so the only
		// way to tell where it starts is to figure out how long the Uid is.
		int offset = xid.gtrid_length + Uid.UID_SIZE;

		return (xid.data[offset + 0] << 24)
				+ ((xid.data[offset + 1] & 0xFF) << 16)
				+ ((xid.data[offset + 2] & 0xFF) << 8)
				+ (xid.data[offset + 3] & 0xFF);
    }

    public static String getXIDString(XID xid)
    {
        // companion method to XID.toString - try to keep them in sync.

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("< formatId=");
        stringBuilder.append(xid.formatID);
        stringBuilder.append(", gtrid_length=");
        stringBuilder.append(xid.gtrid_length);
        stringBuilder.append(", bqual_length=");
        stringBuilder.append(xid.bqual_length);
        stringBuilder.append(", tx_uid=");
        stringBuilder.append(getUid(xid).stringForm());
        stringBuilder.append(", node_name=");
        stringBuilder.append(getNodeName(xid));
        stringBuilder.append(", branch_uid=");
        stringBuilder.append(getBranchUid(xid));;
        stringBuilder.append(", subordinatenodename=");
        stringBuilder.append(getSubordinateNodeName(xid));
        stringBuilder.append(", eis_name=");
        if (xaResourceRecordWrappingPlugin != null) {
        	stringBuilder.append(xaResourceRecordWrappingPlugin.getEISName(getEISName(xid)));
        } else {
        	stringBuilder.append(getEISName(xid));
        }
        stringBuilder.append(" >");

        return stringBuilder.toString();
    }
}