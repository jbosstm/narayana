/*
 * SPDX short identifier: Apache-2.0
 */


package io.narayana.lra.coordinator.tools.osb.mbean;

import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.tools.osb.mbean.ActionBean;
import com.arjuna.ats.arjuna.tools.osb.mbean.ParticipantStatus;
import com.arjuna.ats.arjuna.tools.osb.mbean.UidWrapper;
import io.narayana.lra.coordinator.domain.model.LongRunningAction;

import java.net.URI;

public class LRAActionBean extends ActionBean implements LRAActionBeanMBean {

    public LRAActionBean(UidWrapper w) {
        super(w);
    }

    @Override
    protected  LRAParticipantRecordWrapper createParticipant(AbstractRecord rec, ParticipantStatus listType) {
        return new LRAParticipantRecordWrapper(this, rec, listType);
    }

    @Override
    public URI getLRAId() {
        return getLRA().getId();
    }

    @Override
    public String getLRAClientId() {
        return getLRA().getClientId();
    }

    @Override
    public URI getParentLRAId() {
        return getLRA().getParentId();
    }

    @Override
    public String getLRAStatus() {
        return getLRA().getLRAStatus().name();
    }

    @Override
    public long getStartTime() {
        return getLRA().getLRAData().getStartTime();
    }

    @Override
    public long getFinishTime() {
        return getLRA().getLRAData().getFinishTime();
    }

    private LongRunningAction getLRA() {
        return (LongRunningAction) ra.getAction();
    }
}