/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.recovery;

import org.jboss.tm.XAResourceWrapper;

import javax.transaction.xa.XAResource;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class TestXAResourceWrapper extends TestXAResource implements XAResourceWrapper {

    private final String productName;

    private final String productVersion;

    private final String jndiName;

    public TestXAResourceWrapper(final String productName, final String productVersion, final String jndiName) {
        this.productName = productName;
        this.productVersion = productVersion;
        this.jndiName = jndiName;
    }

    @Override
    public XAResource getResource() {
        return this;
    }

    @Override
    public String getProductName() {
        return productName;
    }

    @Override
    public String getProductVersion() {
        return productVersion;
    }

    @Override
    public String getJndiName() {
        return jndiName;
    }

}