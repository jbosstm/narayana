/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.jta.resources;

import javax.transaction.xa.XAResource;

/**
 * Marker interface for Last Resource Commit Optimisation.
 * 
 * @author Kevin Conner (Kevin.Conner@arjuna.com)
 * @version $Id$
 * @since ATS 4.1
 */
public interface LastResourceCommitOptimisation extends XAResource
{
}