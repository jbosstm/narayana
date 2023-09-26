/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.orbportability.event;


/*
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: EventHandler.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 2.1.
 */

public interface EventHandler
{

public void connected (org.omg.CORBA.Object obj);
public void disconnected (org.omg.CORBA.Object obj);

public String name ();
 
}