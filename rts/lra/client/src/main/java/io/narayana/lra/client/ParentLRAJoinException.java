/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package io.narayana.lra.client;


import jakarta.ws.rs.core.Response;

public class ParentLRAJoinException extends Exception {

    private static final long serialVersionUID = -5216874174212926195L;
    private final Response reason;

    public ParentLRAJoinException(String lraId, String message, Response reason) {
        super(String.format("%s, lra id: %s", message, lraId));

        this.reason = reason;
    }

    public Response getReason() {
        return reason;
    }
}