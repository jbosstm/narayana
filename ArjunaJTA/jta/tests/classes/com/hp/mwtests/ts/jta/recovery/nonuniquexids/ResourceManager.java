/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2020 Red Hat, Inc., and individual contributors
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
