/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.orbportability.initialisation;

public interface TestAttributeCallback
{
    public void preInitAttributeCalled();
    
    public void postInitAttributeCalled();
}