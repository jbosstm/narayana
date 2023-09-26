/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.internal.jta.tools.osb.mbean.jts;

import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.tools.osb.mbean.*;
import com.arjuna.ats.internal.jta.resources.jts.orbspecific.XAResourceRecord;
import com.arjuna.ats.internal.jta.tools.osb.mbean.jta.JTAActionBean;
import com.arjuna.ats.internal.jta.tools.osb.mbean.jta.XARecoveryResourceMBean;
import com.arjuna.ats.internal.jts.resources.ExtendedResourceRecord;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Mike Musgrove
 */
/**
 * @deprecated as of 5.0.5.Final In a subsequent release we will change packages names in order to 
 * provide a better separation between public and internal classes.
 */
@Deprecated // in order to provide a better separation between public and internal classes.
public class JTSActionBean extends JTAActionBean {
    // ExtendedResourceRecord does not statically expose its type
    private static String ERRT = "StateManager/AbstractRecord/ExtendedResourceRecord";
    private static List<XARecoveryResourceMBean> xaRecoveryResourceMBeans = null;

    public JTSActionBean(UidWrapper w) {
        super(w);
    }

    @Override
    protected LogRecordWrapper createParticipant(AbstractRecord rec, ParticipantStatus listType) {
        if (rec instanceof ExtendedResourceRecord)
            return new XAResourceRecordBean(this, rec, listType);

        return super.createParticipant(rec, listType);
    }

    static synchronized void getXARecoveryResourceMBeans(UidWrapper uidWrapper) {
        if (xaRecoveryResourceMBeans == null) {
            xaRecoveryResourceMBeans = new ArrayList<XARecoveryResourceMBean>();

            List<UidWrapper> wrappers = uidWrapper.probe(XAResourceRecord.typeName());

            if (wrappers != null) {
                for (UidWrapper w : wrappers) {
                    OSEntryBean bean = w.getMBean();

                    if (bean != null && bean instanceof XARecoveryResourceMBean)
                        xaRecoveryResourceMBeans.add((XARecoveryResourceMBean) bean);
                }
            }
        }
    }

    private XARecoveryResourceMBean findJTSXAResourceRecordWrapper(XAResourceRecordBean xarrb) {
        getXARecoveryResourceMBeans(_uidWrapper);

        for (XARecoveryResourceMBean bean : xaRecoveryResourceMBeans) {
            if (xarrb.xares.xidImple.equals(((JTSXAResourceRecordWrapper) bean).xidImple))
                return bean;
        }

        return null;
    }

    @Override
    public void register() {
        super.register();

        // for each ExtendedResourceRecord see if there is a corresponding XARecoveryResource entry in the local store
        for (LogRecordWrapper participant : getParticipants()) {
            if (participant instanceof XAResourceRecordBean) {
                // this is an ExtendedResourceRecord
                XAResourceRecordBean xarrb = (XAResourceRecordBean) participant;

                // see if there is a corresponding JTSXAResourceRecordWrapper for this participant
                XARecoveryResourceMBean jtsxaResourceRecordWrapper = findJTSXAResourceRecordWrapper(xarrb);

                if (jtsxaResourceRecordWrapper != null && jtsxaResourceRecordWrapper instanceof JTSXAResourceRecordWrapper)
                    xarrb.setJtsXAResourceRecord((JTSXAResourceRecordWrapper) jtsxaResourceRecordWrapper);
            }
        }
    }
}