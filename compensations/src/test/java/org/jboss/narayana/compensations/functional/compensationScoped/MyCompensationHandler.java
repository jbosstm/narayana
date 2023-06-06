/*
 * SPDX short identifier: Apache-2.0
 */


package org.jboss.narayana.compensations.functional.compensationScoped;

import org.jboss.narayana.compensations.api.CompensationHandler;

import jakarta.inject.Inject;

/**
 * @author paul.robinson@redhat.com 29/07/2013
 */
public class MyCompensationHandler implements CompensationHandler {

    @Inject
    DataPojo myData;

    public static boolean dataAvailable = false;

    public static String expectedData;

    @Override
    public void compensate() {

        if (myData != null && myData.getData() != null && myData.getData().equals(expectedData)) {
            dataAvailable = true;
        } else {
            dataAvailable = false;
        }
    }
}