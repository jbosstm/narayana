/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.narayana.compensations.functional.common;

import org.jboss.narayana.compensations.api.CompensationHandler;

import jakarta.inject.Inject;

/**
 * @author paul.robinson@redhat.com 22/04/2013
 */
public class DataCompensationHandler implements CompensationHandler {

    private static Boolean dataAvailable = false;
    private static Boolean dataNotNull = false;
    public static String expectedDataValue = null;

    @Inject
    DummyData data;

    @Override
    public void compensate() {

        if (data != null) {
            dataNotNull = true;
            try {
                if (data.getValue() != null && data.getValue().equals(expectedDataValue)) {
                    dataAvailable = true;
                }
            } catch (Exception e) {
                //unavailable
            }
        }
    }

    public static boolean getDataAvailable() {

        return dataAvailable;
    }

    public static void reset() {

        dataAvailable = false;
        dataNotNull = false;
        expectedDataValue = null;
    }

    public static Boolean getDataNotNull() {

        return dataNotNull;
    }
}