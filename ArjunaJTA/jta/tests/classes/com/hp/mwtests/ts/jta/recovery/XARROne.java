/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.recovery;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.xa.XAResource;

import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;

public class XARROne implements XAResourceRecoveryHelper {

    private List<XAResource> resources = new ArrayList<XAResource>();

    public XARROne() throws IOException {
        File file = new File("XARR.txt");
        if (file.exists()) {
            resources.add(new XARRTestResource("XARROne", file));
        }
    }

    @Override
    public XAResource[] getXAResources() {
        return resources.toArray(new XAResource[] {});
    }

    @Override
    public boolean initialise(String p) throws Exception {
        return true;
    }

}