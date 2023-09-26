/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.recovery.nonuniquexids;

import org.jboss.tm.XAResourceWrapper;

import javax.transaction.xa.XAResource;

// unit test helper based on a Jonathan Halliday's code
public class XAResourceWrapperImpl extends XAResourceImpl implements XAResourceWrapper {

    public XAResourceWrapperImpl(ResourceManager resourceManager, String name) {
        super(resourceManager, name);
    }

    @Override
    public XAResource getResource() {
        return this;
    }

    @Override
    public String getProductName() {
        return null;
    }

    @Override
    public String getProductVersion() {
        return null;
    }

    @Override
    public String getJndiName() {
        return resourceManager.getName();
    }
}