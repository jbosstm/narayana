/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wsas.completionstatus;

/**
 * Implementations of this interface represent the various ways in which
 * an activity can terminate. Services are expected to extend this in
 * specific ways. Uniqueness is guaranteed by the package scope of the
 * final implementation.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: CompletionStatus.java,v 1.1 2002/11/25 10:51:41 nmcl Exp $
 * @since 1.0.
 */

/*
 * There is an argument that the basic activity service layer should know
 * nothing about success of failure and hence does not need completion
 * statuses. We have them here for now until we decide to remove them!
 */

public interface CompletionStatus
{

    /**
     * Two statuses are equal if their targets are the same.
     */

    public boolean equals (Object param);
 
}