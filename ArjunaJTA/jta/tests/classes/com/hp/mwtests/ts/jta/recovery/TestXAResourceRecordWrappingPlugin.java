/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat, Inc., and individual contributors
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
