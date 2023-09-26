/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.narayana.compensations.functional.distributed;

import org.jboss.narayana.compensations.api.CompensationScoped;

import jakarta.inject.Named;
import java.io.Serializable;

/**
 * @author paul.robinson@redhat.com 23/05/2013
 */
@Named("State")
@CompensationScoped
public class State implements Serializable {

    private String value;

    public String getValue() {

        return value;
    }

    public void setValue(String value) {

        this.value = value;
    }
}