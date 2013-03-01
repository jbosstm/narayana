package org.jboss.jbossts.txbridge.tests.outbound.client;

import java.util.ArrayList;

public interface CommonTestService {

    void doNothing();

    ArrayList<String> getTwoPhaseCommitInvocations();

    void reset();

}
