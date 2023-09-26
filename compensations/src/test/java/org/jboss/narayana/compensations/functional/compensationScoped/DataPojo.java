/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.narayana.compensations.functional.compensationScoped;

import org.jboss.narayana.compensations.api.CompensationScoped;

import jakarta.inject.Named;
import java.io.Serializable;

/**
 * @author paul.robinson@redhat.com 29/07/2013
 */
@CompensationScoped
@Named
public class DataPojo implements Serializable {

    private String data;

    public String getData() {

        return data;
    }

    public void setData(String data) {

        this.data = data;
    }
}