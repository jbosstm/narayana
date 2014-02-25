package com.arjuna.ats.internal.jta.tools.osb.mbean.jta;

import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.tools.osb.mbean.ActionBean;
import com.arjuna.ats.arjuna.tools.osb.mbean.LogRecordWrapper;
import com.arjuna.ats.arjuna.tools.osb.mbean.ParticipantStatus;
import com.arjuna.ats.arjuna.tools.osb.mbean.UidWrapper;

/**
 * JTA specific version of an ActionBean that knows when a participant record
 * corresponds to an XAResource
 */
public class JTAActionBean extends ActionBean {
    
    public JTAActionBean(UidWrapper w) {
        super(w);
    }

    @Override
    protected LogRecordWrapper createParticipant(AbstractRecord rec, ParticipantStatus listType) {
        if (rec instanceof com.arjuna.ats.internal.jta.resources.arjunacore.XAResourceRecord)
            return new com.arjuna.ats.internal.jta.tools.osb.mbean.jta.XAResourceRecordBean(this, rec, listType);
        else if (rec instanceof com.arjuna.ats.internal.jta.resources.arjunacore.CommitMarkableResourceRecord)
            return new com.arjuna.ats.internal.jta.tools.osb.mbean.jta.CommitMarkableResourceRecordBean(this, rec, listType);
        else
            return super.createParticipant(rec, listType);
    }
/*
    protected ActionBeanWrapperInterface createWrapper(UidWrapper w) {
        String cn = SubordinateAtomicAction.class.getCanonicalName();
        return new GenericAtomicActionWrapper("com.arjuna.ats.internal.jta.transaction.arjunacore.subordinate.jca.SubordinateAtomicAction", w);
    }



    class SubordinateAtomicActionWrapper extends SubordinateAtomicAction implements ActionBeanWrapperInterface {
        boolean activated;

        public SubordinateAtomicActionWrapper(UidWrapper w) {
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

    }*/
}
