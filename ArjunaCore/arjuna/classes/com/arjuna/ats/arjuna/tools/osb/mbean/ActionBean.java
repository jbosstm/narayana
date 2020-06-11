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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.management.MBeanException;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.arjuna.coordinator.RecordList;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.tools.osb.util.JMXServer;

/**
 * MBean implementation of an ObjectStore entry that represents an AtomicAction
 *
 * @deprecated as of 5.0.5.Final In a subsequent release we will change packages names in order to 
 * provide a better separation between public and internal classes.
 *
 * @author Mike Musgrove
 */
@Deprecated // in order to provide a better separation between public and internal classes.
public class ActionBean extends OSEntryBean implements ActionBeanMBean {
    // Basic properties this enty
    private StateManagerWrapper sminfo;
    // collection of participants belonging to this BasicAction
    private Collection<LogRecordWrapper> participants = new ArrayList<LogRecordWrapper>();
    // wrapper around the real AtomicAction
    protected ActionBeanWrapperInterface ra;

    protected List<UidWrapper> recuids = new ArrayList<UidWrapper>();
    private static final ThreadLocal<String> classname = new ThreadLocal<String>();

    public ActionBean(UidWrapper w) {
        super(w);

        boolean isJTS = JMXServer.isJTS() && w.getType().contains("ArjunaTransactionImple");

        if (isJTS) {
            try {
                UidWrapper.setRecordWrapperTypeName(w.getType());
                Class<ActionBeanWrapperInterface> cl = (Class<ActionBeanWrapperInterface>) Class.forName(w.getClassName());
                Constructor<ActionBeanWrapperInterface> constructor = cl.getConstructor(ActionBean.class, UidWrapper.class);
                ra = constructor.newInstance(this, w);
                ra.activate();
            } catch (Exception e) { // ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException
                if (tsLogger.logger.isTraceEnabled())
                    tsLogger.logger.trace("Error constructing " + JMXServer.AJT_WRAPPER_TYPE + ": " + e);
                ra = createWrapper(w, true);
            }

            /*
             * For JTS we also store participant details under "CosTransactions/XAResourceRecord"
             * We may at some point want to augment the beans created in findParticipants below with
             * w.probe(JMXServer.AJT_RECORD_TYPE);
             */
        } else {
            ra = createWrapper(w, true);  // com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeManager.manager()
        }

        sminfo = new StateManagerWrapper(StoreManager.getRecoveryStore(), getUid(), getType());

        for (ParticipantStatus lt : ParticipantStatus.values()) {
            findParticipants(recuids, ra.getRecords(lt), lt);
        }
    }

    protected ActionBeanWrapperInterface createWrapper(UidWrapper w, boolean activate) {
        GenericAtomicActionWrapper action = new GenericAtomicActionWrapper(w.getClassName(), w);

        if (activate)
            action.activate();

        return action;
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
     * @throws MBeanException 
     */
    public String remove() throws MBeanException {
        // first unregister each participant of this action
        Iterator<LogRecordWrapper> i = participants.iterator();
        int removeCount = 0;
        int participantCount = participants.size();

        while (i.hasNext()) {
            LogRecordWrapper w = i.next();

            w.remove(false);

            if (w.isRemoved())
                removeCount += 1;

            i.remove();
        }

        try {
            if (removeCount == participantCount) {
                if (!StoreManager.getRecoveryStore().remove_committed(getUid(), getType()))
                    return "Attempt to remove transaction failed";

                _uidWrapper.unregister();
            }
            return "Transaction successfully removed";
        } catch (ObjectStoreException e) {
            return "Unable to remove transaction: " + e.getMessage();
        } finally {
            _uidWrapper.probe();
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
                        if (tsLogger.logger.isTraceEnabled())
                            tsLogger.logger.trace("participant record is not a LogRecordWrapper");
                        lw = createParticipant(rec, listType, recuids.get(i));
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

    protected LogRecordWrapper createParticipant(AbstractRecord rec, ParticipantStatus listType, UidWrapper wrapper) {
        return new LogRecordWrapper(this, rec, listType, wrapper);
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
     * commitment protocol
     * @param logrec the record whose status is to be changed
     * @param newStatus the desired status
     * @return true if the status was changed
     */
    public boolean setStatus(LogRecordWrapper logrec, ParticipantStatus newStatus) {
        ParticipantStatus lt = logrec.getListType();
        AbstractRecord targRecord = logrec.getRecord();

        RecordList oldList = ra.getRecords(lt);
        RecordList newList = ra.getRecords(newStatus);

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
                            targRecord.clearHeuristicDecision();
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
     * remove the a participant
     * @param logRecordWrapper the wrapped log record
     */
    public void remove(LogRecordWrapper logRecordWrapper) {
        ra.remove(logRecordWrapper);
    }

    /**
     * The ActionBean needs access to the participant lists maintained by an AtomicAction but these
     * lists are protected. Therefore define a simple extension class to get at these records:
     */
    public static class GenericAtomicActionWrapper implements ActionBeanWrapperInterface {
        boolean activated;
        BasicAction action;
        Map<String, RecordList> recs;
        Method setHeuristicDecision;
        Method updateState = null;
        UidWrapper uidWrapper;

        private static BasicAction createAction(String classType, UidWrapper wrapper) {
            if (classType == null)
                classType = "com.arjuna.ats.arjuna.AtomicAction";

            try {
                Class cls = Class.forName(classType);

                Class pTypes[] = new Class[1];
                pTypes[0] = Uid.class;
                Constructor ctor = cls.getConstructor(pTypes);
                Object args[] = new Object[1];
                args[0] = wrapper.getUid();
                return (BasicAction) ctor.newInstance(args);
            } catch (Exception e) {
                if (tsLogger.logger.isDebugEnabled())
                    tsLogger.logger.debug("unable to create log wrapper for type " + wrapper.getType() + ": error: " + e.getMessage());

                return null;
            }
        }

        public GenericAtomicActionWrapper(BasicAction ba, UidWrapper w) {
            action = ba;
            uidWrapper = w;
            recs = new HashMap<String, RecordList>();

            if (action != null) {
                setHeuristicDecision = getMethod(action.getClass(), "setHeuristicDecision", int.class);
                updateState = getMethod(action.getClass(), "updateState");

                if (setHeuristicDecision != null)
                    setHeuristicDecision.setAccessible(true);

                if (updateState != null)
                    updateState.setAccessible(true);
            }
        }

        public GenericAtomicActionWrapper(String classType, UidWrapper w) {
            this(createAction(classType, w), w);
        }

        public BasicAction getAction() {
            return action;
        }

        public boolean activate() {
            if (!activated && action != null) {
                try {
                    activated = action.activate();
                } catch (Exception e) {
                    activated = false;
                    tsLogger.logger.warn("Activate of " + action + " failed: " + e.getMessage());
                }
            }

            return activated;
        }

        public void doUpdateState() {
            if (updateState != null && action != null) {
                try {
                    updateState.invoke(action);
                } catch (IllegalAccessException e) {
                    if (tsLogger.logger.isDebugEnabled())
                        tsLogger.logger.debug("failed to update heuristic for " + action.toString() + ": error: " + e.getMessage());
                } catch (InvocationTargetException e) {
                    if (tsLogger.logger.isDebugEnabled())
                        tsLogger.logger.debug("failed to update heuristic for " + action.toString() + ": error: " + e.getMessage());
                }
            }
        }

        public Uid get_uid() {
            return action != null ? action.get_uid() : uidWrapper.getUid();
        }

        public Uid getUid(AbstractRecord rec) {
            return rec.order(); //get_uid();
        }

        public StringBuilder toString(String prefix, StringBuilder sb) {
            prefix += '\t';
            return sb.append('\n').append(prefix).append(get_uid());
        }

        public void clearHeuristicDecision(int newDecision) {
            RecordList rl = getRecords("heuristicList");

            if (setHeuristicDecision != null && rl != null && rl.size() == 0)
                try {
                    setHeuristicDecision.invoke(action, newDecision);
                } catch (IllegalAccessException e) {
                    if (tsLogger.logger.isDebugEnabled())
                        tsLogger.logger.debug("failed to update heuristic for " + action.toString() + ": error: " + e.getMessage());
                } catch (InvocationTargetException e) {
                    if (tsLogger.logger.isDebugEnabled())
                        tsLogger.logger.debug("failed to update heuristic for " + action.toString() + ": error: " + e.getMessage());
                }
        }

        public boolean removeRecords(RecordList rl, LogRecordWrapper logRecordWrapper) {
            if (rl != null && rl.size() > 0) {
                AbstractRecord ar = logRecordWrapper.getRecord();

                boolean forgotten = ar.forgetHeuristic();
                boolean removeAllowed = arjPropertyManager.getObjectStoreEnvironmentBean().isIgnoreMBeanHeuristics();

                if (forgotten || removeAllowed) {
                    // remove the transaction log for the record
                    if (rl.remove(ar)) {
                        return true;
                    }
                }
            }

            return false;
        }

        @Override
        public void remove(LogRecordWrapper logRecordWrapper) {
            if (logRecordWrapper.removeFromList(getRecords(logRecordWrapper.getListType()))) {
                doUpdateState(); // rewrite the list
            }
        }

        private Field getField(Class cl, String fn) {
            try {
                return cl.getDeclaredField(fn);
            } catch (NoSuchFieldException e) {
                return getField(cl.getSuperclass(), fn);
            }
        }

        private Method getMethod(Class cl, String mn, Class<?>... parameterTypes) {
            try {
                if (cl == null)
                    return null;
                return cl.getDeclaredMethod(mn, parameterTypes);
            } catch (NoSuchMethodException e) {
                return getMethod(cl.getSuperclass(), mn, parameterTypes);
            }
        }

        public RecordList getRecords(String ln) {
            if (action == null)
                return null;

            if (recs.containsKey(ln))
                return recs.get(ln);

            Field f = getField(action.getClass(), ln);
            f.setAccessible(true);
            try {
                RecordList rl = (RecordList) f.get(action);

                if (rl != null)
                    recs.put(ln, rl);

                return rl;
            } catch (IllegalAccessException e) {
                return null;
            }
        }

        public RecordList getRecords(ParticipantStatus type) {

            switch (type) {
                default:
                case PREPARED: return getRecords("preparedList");
                case FAILED: return getRecords("failedList");
                case HEURISTIC: return getRecords("heuristicList");
                case PENDING: return getRecords("pendingList");
                case READONLY: return getRecords("readonlyList");
            }
        }
    }
}
