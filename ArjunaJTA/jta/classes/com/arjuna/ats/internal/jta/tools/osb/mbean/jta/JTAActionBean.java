/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.internal.jta.tools.osb.mbean.jta;

import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.internal.arjuna.tools.osb.mbean.ActionBean;
import com.arjuna.ats.internal.arjuna.tools.osb.mbean.LogRecordWrapper;
import com.arjuna.ats.internal.arjuna.tools.osb.mbean.ParticipantStatus;
import com.arjuna.ats.internal.arjuna.tools.osb.mbean.UidWrapper;

/**
 * JTA specific version of an ActionBean that knows when a participant record
 * corresponds to an XAResource
 *
 * @author Mike Musgrove
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
}
