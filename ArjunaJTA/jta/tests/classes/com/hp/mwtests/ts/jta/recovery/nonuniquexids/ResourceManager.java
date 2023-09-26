/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.recovery.nonuniquexids;

import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;

import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.util.ArrayList;
import java.util.List;

// unit test helper based on a Jonathan Halliday's code
public class ResourceManager implements XAResourceRecoveryHelper {

    private final String name;
    private final boolean wrapResources;
    private final List<Xid> doubts = new ArrayList<>();

    public ResourceManager(String name, boolean wrapResources) {
        this.name = name;
        this.wrapResources = wrapResources;
    }

    public String getName() {
        return name;
    }

    public void addDoubt(Xid xid) {
        doubts.add(xid);
    }

    public void removeDoubt(Xid xid) {
        doubts.remove(xid);
    }

    public Xid[] getDoubts() {
        return doubts.toArray(new Xid[0]);
    }

    public boolean isInDoubt(Xid xid) {
        return doubts.contains(xid);
    }

    public XAResource getResource(String name) {
        if (wrapResources) {
            return new XAResourceWrapperImpl(this, name);
        }

        return new XAResourceImpl(this, name);
    }

    ///////////////


    @Override
    public boolean initialise(String p) throws Exception {
        return false;
    }

    @Override
    public XAResource[] getXAResources() throws Exception {
        XAResource[] result = new XAResource[1];
        if (wrapResources) {
            result[0] = new XAResourceWrapperImpl(this, name);
        } else {
            result[0] = new XAResourceImpl(this, "rec");
        }

        return result;
    }
}