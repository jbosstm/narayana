/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.tools.osb.mbean;

import com.arjuna.ats.arjuna.state.OutputObjectState;

/**
 * Wrapper for ObjectOutputState to facilitate use in JMX invocations
 */
public class OutputObjectStateWrapper extends ObjectStateWrapper {
    public OutputObjectStateWrapper(OutputObjectState oos) {
        super(oos);
    }
}
