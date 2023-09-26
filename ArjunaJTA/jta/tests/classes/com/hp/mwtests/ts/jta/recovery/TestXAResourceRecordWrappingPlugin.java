/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.recovery;

import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.internal.jta.resources.arjunacore.XAResourceRecord;
import com.arjuna.ats.internal.jta.resources.arjunacore.XAResourceRecordWrappingPlugin;
import org.jboss.tm.XAResourceWrapper;

import javax.transaction.xa.XAResource;
import java.io.IOException;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class TestXAResourceRecordWrappingPlugin implements XAResourceRecordWrappingPlugin {

    @Override
    public void transcribeWrapperData(final XAResourceRecord xaResourceRecord) {
        final XAResource xaResource = (XAResource) xaResourceRecord.value();

        if (xaResource instanceof XAResourceWrapper) {
            XAResourceWrapper xaResourceWrapper = (XAResourceWrapper) xaResource;
            xaResourceRecord.setProductName(xaResourceWrapper.getProductName());
            xaResourceRecord.setProductVersion(xaResourceWrapper.getProductVersion());
            xaResourceRecord.setJndiName(xaResourceWrapper.getJndiName());
        }
    }

    @Override
    public Integer getEISName(XAResource xaResource) throws IOException, ObjectStoreException {
        return 0;
    }

    @Override
    public String getEISName(Integer eisName) {
        return "unknown eis name";
    }
}