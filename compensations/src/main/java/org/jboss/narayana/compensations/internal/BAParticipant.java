/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.narayana.compensations.internal;

/**
 * @author paul.robinson@redhat.com 22/03/2013
 */
public interface BAParticipant {


    public void confirmCompleted(boolean confirmed);

    public void close() throws Exception;

    public void cancel() throws Exception;

    public void compensate() throws Exception;

}