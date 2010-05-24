package com.arjuna.ats.arjuna.tools.osb.mbean;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;

/**
 * An MBean implementation for representing a participant in an Atomic Action or transaction
 */
public class LogRecordWrapper extends OSEntryBean implements LogRecordWrapperMBean {
	protected ActionBean parent;
	protected AbstractRecord rec;
	protected boolean activated;
	protected ParticipantStatus listType;
	protected String objName;

	public LogRecordWrapper(Uid uid) {
		objName = "type=unitialised,puid=" + uid.fileStringForm();
	}

	public LogRecordWrapper(ActionBean parent, AbstractRecord rec, ParticipantStatus listType) {
		init(parent,  rec, listType);
	}

	public void init(ActionBean parent, AbstractRecord rec, ParticipantStatus listType) {
		this.parent = parent;
		this.rec = rec;
		this.listType = listType;
		objName = parent.getName() + ",puid=" + rec.order().fileStringForm();
	}

	public String getName() {
		return objName;
	}

	public boolean isParticipant() {
		return true;
	}

	public String getStatus() {
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
		if (parent != null && parent.setStatus(this, newState)) {
			listType = newState;
			return "status change was successful";
		} else {
			return "failed";
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
			activated = rec.activate();

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
			sb.append('\n').append(prefix).append(objName);
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
}
