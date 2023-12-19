/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package io.narayana.lra.client;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

public class ParentLRAJoinExceptionMapper implements ExceptionMapper<ParentLRAJoinException> {
    @Override
    public Response toResponse(ParentLRAJoinException exception) {
        return exception.getReason();
    }
}
