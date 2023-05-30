/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */

package io.narayana.lra.client.internal.proxy;

import jakarta.ws.rs.NotFoundException;
import java.io.Serializable;
import java.net.URI;
import java.util.concurrent.Future;

public interface LRAProxyParticipant extends Serializable {

    Future<Void> completeWork(URI lraId) throws NotFoundException;

    Future<Void> compensateWork(URI lraId) throws NotFoundException;
}