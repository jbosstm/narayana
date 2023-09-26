/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.orbportability.common;

/**
 * Some exception values we may set.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: Exceptions.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class Exceptions
{

public static final int BAD_OPERATION_BASE = 100000;
public static final int NOT_FOUND = BAD_OPERATION_BASE+1;
public static final int CANNOT_PROCEED = BAD_OPERATION_BASE+2;
public static final int INVALID_NAME = BAD_OPERATION_BASE+3;

/*
 * We throw this BAD_PARAM when narrow fails.
 */

public static final int BAD_OBJECT_REF = 10029;
    
}