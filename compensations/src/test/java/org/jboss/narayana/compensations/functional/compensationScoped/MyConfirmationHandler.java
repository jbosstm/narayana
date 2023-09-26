/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.narayana.compensations.functional.compensationScoped;

import org.jboss.narayana.compensations.api.ConfirmationHandler;

import jakarta.inject.Inject;

/**
 * @author paul.robinson@redhat.com 29/07/2013
 */
public class MyConfirmationHandler implements ConfirmationHandler {

    @Inject
    DataPojo myData;

    public static boolean dataAvailable = false;

    public static String expectedData;

    @Override
    public void confirm() {

        if (myData != null && myData.getData() != null && myData.getData().equals(expectedData)) {
            dataAvailable = true;
        } else {
            dataAvailable = false;
        }
    }
}