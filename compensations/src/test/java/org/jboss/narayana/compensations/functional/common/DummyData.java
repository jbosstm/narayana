/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.narayana.compensations.functional.common;

import org.jboss.narayana.compensations.api.CompensationScoped;

import jakarta.inject.Named;
import java.io.Serializable;

/**
 * @author paul.robinson@redhat.com 23/04/2013
 */
@Named("DummyData")
@CompensationScoped
public class DummyData implements Serializable {

    private String value = "";

    public void setValue(String value) {

        this.value = value;
    }

    public String getValue() {

        return value;
    }
}