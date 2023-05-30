/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */



package com.hp.mwtests.orbportability.initialisation;

public interface TestAttributeCallback
{
    public void preInitAttributeCalled();
    
    public void postInitAttributeCalled();
}