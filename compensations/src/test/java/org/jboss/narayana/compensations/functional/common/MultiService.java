/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.narayana.compensations.functional.common;

import org.jboss.narayana.compensations.api.Compensatable;

import jakarta.inject.Inject;

/**
 * @author paul.robinson@redhat.com 22/03/2013
 */
public class MultiService {

    @Inject
    DummyData dummyData;

    @Inject
    SingleService singleService;

    @Compensatable
    public void testsMulti(boolean throwException) throws MyRuntimeException {

        dummyData.setValue("blah");

        singleService.testSingle1(false);
        singleService.testSingle2(false);

        if (throwException) {
            throw new MyRuntimeException();
        }
    }

    @Compensatable
    public void testAlternative(boolean throwException) throws MyRuntimeException {

        singleService.testSingle1(false);

        dummyData.setValue("blah");

        try {
            singleService.testSingle2DontCancel(true);
        } catch (MyRuntimeException e) {
            singleService.testSingle3(false);
        }

        if (throwException) {
            throw new MyRuntimeException();
        }
    }
}