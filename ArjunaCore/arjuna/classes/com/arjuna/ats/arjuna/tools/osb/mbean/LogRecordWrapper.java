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

import javax.management.MBeanException;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.HeuristicInformation;
import com.arjuna.ats.arjuna.coordinator.RecordList;
import com.arjuna.ats.arjuna.logging.tsLogger;

/**
 * An MBean implementation for representing a participant in an Atomic Action or transaction
 *
 * @author Mike Musgrove
 */
/**
 * @deprecated as of 5.0.5.Final In a subsequent release we will change packages names in order to 
 * provide a better separation between public and internal classes.
 */
@Deprecated // in order to provide a better separation between public and internal classes.
public class LogRecordWrapper extends OSEntryBean implements LogRecordWrapperMBean {
	protected ActionBean parent;
	protected AbstractRecord rec;
	protected boolean activated;
	protected ParticipantStatus listType;
    protected boolean removed;
    protected boolean forgetRec;

	public LogRecordWrapper(Uid uid) {
		super(null);
		_uidWrapper.setName(super._uidWrapper.getBrowserMBeanName() + ",itype=uninitialised,puid=" + uid.fileStringForm());
	}

	public LogRecordWrapper(ActionBean parent, AbstractRecord rec, ParticipantStatus listType, UidWrapper wrapper) {
		super(wrapper);
		init(parent,  rec, listType);
	}

	public LogRecordWrapper(ActionBean parent, AbstractRecord rec, ParticipantStatus listType) {
		this(parent, rec, listType, LogRecordWrapper.class.getName());
	}

	public LogRecordWrapper(ActionBean parent, AbstractRecord rec, ParticipantStatus listType, String className) {
		this(parent, rec, listType, makeWrapper(parent, rec, className));
	}

	private static UidWrapper makeWrapper(ActionBean parent, AbstractRecord rec, String beanType) {
		UidWrapper w = new UidWrapper(parent._uidWrapper.getBrowser(), beanType, rec.type(), rec.getClass().getName(), rec.order(), false);
		// TODO look up the hander for rec.type() and use that to create the wrapper
		w.setName(parent.getName() + ",puid=" + rec.order().fileStringForm());

		return w;
	}

	public void init(ActionBean parent, AbstractRecord rec, ParticipantStatus listType) {
		this.parent = parent;
		this.rec = rec;
		this.listType = listType;
		_uidWrapper.setName(parent.getName() + ",puid=" + rec.order().fileStringForm());
	}

	public boolean isParticipant() {
		return true;
	}

	public String getStatus() {
		if (isHeuristic()) {
			String type = getHeuristicStatus();

			if (!type.equals(HeuristicStatus.UNKNOWN.name()))
				return type;
		}

		return listType.toString();
	}

	public void setStatus(String newState) {
		doSetStatus(newState);
	}

    public String clearHeuristic() {
        return doSetStatus("PREPARED");
    }

	public String doSetStatus(String newState) {
		try {
			return setStatus(Enum.valueOf(ParticipantStatus.class, newState.toUpperCase()));
		} catch (IllegalArgumentException e) {
			StringBuilder sb = new StringBuilder("Valid status values are: ");

			for (ParticipantStatus lt : ParticipantStatus.values()) {
				sb.append(lt.name()).append(", ");
			}

			sb.append(" and only HEURISTIC and PREPARED will persist after JVM restart.");

			return sb.toString();
		}
	}

	public String setStatus(ParticipantStatus newState) {
        if (getListType().equals(newState))
            return "participant is prepared for recovery";

		/*
		 * Only move a heuristic to the prepared list if it hasn't already committed or rolled back
		 */
		if (newState.equals(ParticipantStatus.PREPARED) && getListType().equals(ParticipantStatus.HEURISTIC)) {
			HeuristicStatus heuristicStatus = HeuristicStatus.valueOf(getHeuristicStatus());

			if (heuristicStatus.equals(HeuristicStatus.HEURISTIC_COMMIT) ||
				heuristicStatus.equals(HeuristicStatus.HEURISTIC_ROLLBACK)) {
				return "participant has already committed or rolled back";
			}
		}

		if (parent != null && parent.setStatus(this, newState)) {
			listType = newState;

            if (newState == ParticipantStatus.PREPARED )
			    return "participant recovery will be attempted during the next recovery pass";

            return "participant status change was successful";
		} else {
			return "participant status change failed";
		}
	}

	public String getType() {
		return rec == null ? "uninitialised" : rec.type();
	}

	public AbstractRecord getRecord() {
		return rec;
	}

	public ParticipantStatus getListType() {
		return listType;
	}

	public boolean activate() {
		if (!activated && rec != null)
            try {
                activated = rec.activate();
            } catch (Exception e) {
                activated = false;
                tsLogger.logger.warn("Activate of " + rec + " failed: " + e.getMessage());
            }

		return activated;
	}

	public StringBuilder toString(String prefix, StringBuilder sb) {
		prefix += "\t";
		if (parent != null && rec != null) {
			sb.append('\n').append(prefix).append(parent.getUid(rec));
			sb.append('\n').append(prefix).append(listType.toString());
			sb.append('\n').append(prefix).append(rec.type());
			sb.append('\n').append(prefix).append(parent.getCreationTime());
			sb.append('\n').append(prefix).append(parent.getAgeInSeconds());
		} else {
			sb.append('\n').append(prefix).append(_uidWrapper.getName());
		}

		return sb;
	}

	public String callMethod(Object object, String mName)
	{
		try {
			return (String) object.getClass().getMethod(mName).invoke(object);
		} catch (NoSuchMethodException e) {
			return "Not supported";
		} catch (Exception e) {
			return "Error: " + e.getMessage();
		}
	}

	public boolean isHeuristic() {
		return listType.equals(ParticipantStatus.HEURISTIC);
	}

	@Override
	public String getHeuristicStatus() {
		Object heuristicInformation = rec.value();
		HeuristicStatus hs;

		if (heuristicInformation  != null && heuristicInformation instanceof HeuristicInformation) {
			HeuristicInformation hi = (HeuristicInformation) heuristicInformation;
			hs = HeuristicStatus.intToStatus(hi.getHeuristicType());
		} else {
			hs = HeuristicStatus.UNKNOWN;
		}

		return hs.name();
	}

	public String remove(boolean reprobe) throws MBeanException {
		if (parent != null) {
			parent.remove(this);
			_uidWrapper.unregister();
			if (reprobe)
				_uidWrapper.probe();
		}

		return "Record successfully removed";
	}

	@Override
	public String remove() throws MBeanException {
		return remove(true);
	}

	public boolean isRemoved() {
        return removed;
    }

    public boolean removeFromList(RecordList rl) {
        if (rl != null && rl.size() > 0 && rec != null) {
            boolean forgotten = forgetRec || rec.forgetHeuristic();
            boolean removeAllowed = arjPropertyManager.getObjectStoreEnvironmentBean().isIgnoreMBeanHeuristics();

            if (forgotten || removeAllowed) {
                // remove the transaction log for the record
                if (rl.remove(rec)) {
                    removed = true;
                    return true;
                }
            }
        }

        return false;
    }
}
