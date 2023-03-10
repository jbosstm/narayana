/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package io.narayana.lra.client;

import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class LRAParticipantData {
    private String data;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
