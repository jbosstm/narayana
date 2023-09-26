/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.jta.resources;

import javax.transaction.xa.XAResource;

/**
 * @deprecated This interface is replaced by org.jboss.tm.FirstResource in the SPI
 */
public interface StartXAResource extends XAResource
{
}