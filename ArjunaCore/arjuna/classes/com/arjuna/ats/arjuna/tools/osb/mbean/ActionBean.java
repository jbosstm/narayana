package com.arjuna.ats.arjuna.tools.osb.mbean;

import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.RecordList;
import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.tools.osb.util.JMXServer;

import java.lang.reflect.Constructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * MBean implementation of an ObjectStore entry that represents an AtomicAction
 */
public class ActionBean extends OSEntryBean implements ActionBeanMBean {
	// Basic properties this enty
	private StateManagerWrapper sminfo;
	// collection of participants belonging to this BasicAction
	private Collection<LogRecordWrapper> participants = new ArrayList<LogRecordWrapper>();
	// wrapper around the real AtomicAction
	private ActionBeanWrapperInterface ra;

	public ActionBean(UidWrapper w) {
		super(w);

		boolean isJTS = JMXServer.isJTS() && w.getType().endsWith("ArjunaTransactionImple");
		// Participants in a JTS transaction are represented by entries in the ObjectStore
		List<UidWrapper> recuids = null;

		if (isJTS) {
			try {
				Class<ActionBeanWrapperInterface> cl = (Class<ActionBeanWrapperInterface>) Class.forName(JMXServer.AJT_WRAPPER_TYPE);
				Constructor<ActionBeanWrapperInterface> constructor = cl.getConstructor(ActionBean.class, UidWrapper.class);
				ra = constructor.newInstance(this, w);
			} catch (Exception e) { // ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException
				if (tsLogger.logger.isDebugEnabled())
					tsLogger.logger.debug("Error constructing " + JMXServer.AJT_WRAPPER_TYPE + ": " + e);
				ra = new AtomicActionWrapper(w);
			}
		} else {
			ra = new AtomicActionWrapper(w);
		}

		ra.activate();
		sminfo = new StateManagerWrapper(TxControl.getStore(), getUid(), getType());

		if (isJTS) {
			/*
			 * for JTS actions the participants will have entries in the ObjectStore.
			 * these entries will be associated with the current MBean (refer to
			 * the method findParticipants below for details)
			 */
			recuids = w.probe(JMXServer.AJT_RECORD_TYPE, JMXServer.AJT_XAREC_TYPE);
		}

		for (ParticipantStatus lt : ParticipantStatus.values()) {
			findParticipants(recuids, ra.getRecords(lt), lt);
		}
	}

	public StringBuilder toString(String prefix, StringBuilder sb) {
		ra.toString(prefix, sb);
		prefix += '\t';
		sb.append('\n').append(prefix).append(sminfo.getCreationTime());
		sb.append('\n').append(prefix).append(sminfo.getAgeInSeconds());

		for (LogRecordWrapper p : participants) {
			p.toString(prefix, sb);
		}

		return sb;
	}

	/**
	 * return the Uid for given AbstractRecord
	 * @param rec the record whose Uid is required
	 * @return  the Uid of the requested record
	 */
	public Uid getUid(AbstractRecord rec) {
		return ra.getUid(rec);
	}

	/**
	 * Remove this AtomicAction from the ObjectStore
	 * @return a textual indication of whether the remove operation succeeded
	 */
	public String remove() {
		try {
			if (!TxControl.getStore().remove_committed(getUid(), getType()))
				return "remove committed failed"; // TODO com.arjuna.ats.arjuna.tools.osb.mbean.m_1
			else
				w.probe();

			return "remove ok"; // TODO com.arjuna.ats.arjuna.tools.osb.mbean.m_2
		} catch (ObjectStoreException e) {
			return "remove committed exception: " + e.getMessage(); // TODO com.arjuna.ats.arjuna.tools.osb.mbean.m_3
		}
	}

	/**
	 * create MBean representations of the participants of this transaction
	 * @param recuids some transaction participants are represented in the ObjectStore
	 * - if this is the case then recuids contains a list of MBean wrappers representing them.
	 * Otherwise this list will be empty.
	 * @param list the records representing the participants
	 * @param listType indicates the type of the records in list (PREPARED, PENDING, FAILED, READONLY, HEURISTIC)
	 */
	private void findParticipants(List<UidWrapper> recuids, RecordList list, ParticipantStatus listType) {
		if (list != null) {
			for (AbstractRecord rec = list.peekFront(); rec != null; rec = list.peekNext(rec)) {
				LogRecordWrapper lw;
				int i = recuids == null ? -1 : recuids.indexOf(new UidWrapper(ra.getUid(rec)));

				if (i != -1) {
					OSEntryBean p = recuids.get(i).getMBean();

					if (p instanceof LogRecordWrapper) {
						lw = (LogRecordWrapper) p;
						lw.init(this, rec, listType);
					} else {
						if (tsLogger.logger.isDebugEnabled())
							tsLogger.logger.debug("participant record is not a LogRecordWrapper");
						lw = createParticipant(rec, listType);
					}
				} else {
					lw = createParticipant(rec, listType);
				}

				lw.activate();				
				participants.add(lw);
			}
		}
	}

	/**
	 * Extension point for other Bean implementations to provide an implementation bean for its participants.
	 * For example @see com.arjuna.ats.internal.jta.tools.osb.mbean.jta.JTAActionBean
	 * @param rec the record that should be represented by an MBean
	 * @param listType the status of the record
	 * @return the MBean implementation of the participant
	 */
	protected LogRecordWrapper createParticipant(AbstractRecord rec, ParticipantStatus listType) {
		return new LogRecordWrapper(this, rec, listType);
	}

	/**
	 * See if there is participant Bean corresponding to the given record
	 * @param rec the record for the target participant
	 * @return the bean corresponding to the requested record
	 */
	public LogRecordWrapper getParticipant(AbstractRecord rec) {
		for (LogRecordWrapper w : participants)
			if (w.getRecord().equals(rec))
				return w;

		return null;
	}

	/**
	 * register this bean (and its participants) with the MBeanServer
	 */
	public void register() {
		super.register();

		for (LogRecordWrapper p : participants)
			JMXServer.getAgent().registerMBean(p.getName(), p);
	}

	/**
	 * unregister this bean (and its participants) with the MBeanServer
	 */
	public void unregister() {
		for (LogRecordWrapper p : participants)
			JMXServer.getAgent().unregisterMBean(p.getName());

		super.unregister();
	}

	public long getAgeInSeconds() {
		return sminfo.getAgeInSeconds();
	}

	public String getCreationTime() {
		return sminfo.getCreationTime();
	}

	public boolean isParticipant() {
		return false;
	}

	/**
	 * Request a change in status of a participant. For example if a record has a
	 * heuristic status then this method could be used to move it back into the
	 * prepared state so that the recovery system can replay phase 2 of the
	 * committment protocol
	 * @param logrec the record whose status is to be changed
	 * @param newStatus the desired status
	 * @return true if the status was changed
	 */
	public boolean setStatus(LogRecordWrapper logrec, ParticipantStatus newStatus) {
		ParticipantStatus lt = logrec.getListType();
		AbstractRecord targRecord = logrec.getRecord();

		RecordList oldList = ra.getRecords(lt);
		RecordList newList = ra.getRecords(newStatus);

		if (lt.equals(ParticipantStatus.HEURISTIC) && !targRecord.forgetHeuristic()) {
			return false;
		}

		// move the record from currList to targList
		if (oldList.remove(targRecord)) {
			
			if (newList.insert(targRecord)) {
				if (lt.equals(ParticipantStatus.HEURISTIC)) {
					switch (newStatus) {
						case FAILED:
							ra.clearHeuristicDecision(TwoPhaseOutcome.FINISH_ERROR);
							break;
						case PENDING:
							ra.clearHeuristicDecision(TwoPhaseOutcome.NOT_PREPARED);
							break;
						case PREPARED:
							ra.clearHeuristicDecision(TwoPhaseOutcome.PREPARE_OK);
							break;
						case READONLY:
							ra.clearHeuristicDecision(TwoPhaseOutcome.PREPARE_READONLY);
							break;
						default:
							break;
					}
				}

				ra.doUpdateState();
				
				return true;
			}
		}

		return false;
	}

	/**
	 *
	 * @return the MBeans corresponding to the participants within this action
	 */
	public Collection<LogRecordWrapper> getParticipants() {
		return Collections.unmodifiableCollection(participants);
	}

	/**
	 * The ActionBean needs access to the participant lists maintained by an AtomicAction but these
	 * lists are protected. Therefore define a simple extension class to get at these records:
	 */
	class AtomicActionWrapper extends AtomicAction implements ActionBeanWrapperInterface {
		boolean activated;

		public AtomicActionWrapper(UidWrapper w) {
			super(w.getUid());
		}

		public boolean activate() {
			if (!activated)
				activated = super.activate();

			return activated;
		}

		public void doUpdateState() {
			updateState();
		}

		public Uid getUid(AbstractRecord rec) {
			return get_uid();
		}
		
		public StringBuilder toString(String prefix, StringBuilder sb) {
			prefix += '\t';
			return sb.append('\n').append(prefix).append(get_uid());
		}

		public void clearHeuristicDecision(int newDecision) {
			if (super.heuristicList.size() == 0)
				setHeuristicDecision(newDecision);
		}

		public RecordList getRecords(ParticipantStatus type) {
			switch (type) {
				default:
				case PREPARED: return preparedList;
				case FAILED: return failedList;
				case HEURISTIC: return heuristicList;
				case PENDING: return pendingList;
				case READONLY: return readonlyList;
			}
		}
	}

}
