/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.coordinator;

/**
 * A basic iterator for RecordList instances.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: RecordListIterator.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class RecordListIterator
{

    public RecordListIterator (RecordList R)
    {
	curElem = R.listHead;
	curList = R;
    }
    
    public final synchronized void restart ()
    {
	curElem = null;
    }

    /**
     * @return the next entry in the list.
     */

    public final synchronized AbstractRecord iterate ()
    {
        AbstractRecord ret = curElem;

	if (curElem != null)
	    curElem = curElem.getNext();

	return ret;
    }

    private AbstractRecord curElem;
    private RecordList     curList;

}