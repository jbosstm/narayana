/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 1998, 1999, 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: RecordList.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.coordinator;

import java.io.PrintWriter;

import com.arjuna.ats.arjuna.logging.tsLogger;

/**
 * This class manages instances of the classes derived from AbstractRecord in
 * the form of an ordered doubly-linked list. The ordering and insertion
 * criteria are not particularly standard - see the comment on 'insert' for the
 * actual algorithm used in insertion. The algorithm assumes that one or more
 * different record type instances (LockRecords, RecoveryRecords, etc.) will be
 * inserted into the list at different times. Each such record contains specific
 * information managing certain properties of any particular object. As
 * execution progresses newly created records may need to be merged with,
 * replace entirely, or be added to existing records that relate to an object.
 * Note, the methods of this class do not need to be synchronized because
 * instances of this class are only used from within synchronized classes.
 * Applications should not use this class.
 * 
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: RecordList.java 2342 2006-03-30 13:06:17Z $
 * @since JTS 1.0.
 */

public class RecordList
{

    public RecordList()
    {
        listHead = null;
        listTail = null;
        noEntries = 0;
    }

    public RecordList(RecordList copy)
    {
        listHead = copy.listHead;
        listTail = copy.listTail;
        noEntries = copy.noEntries;
    }

    /**
     * Remove and return the element at the front of the list.
     * 
     * @return the front element.
     */

    public final AbstractRecord getFront ()
    {
        AbstractRecord temp = listHead;

        if (noEntries == 1)
        {
            listHead = listTail = null;
            noEntries = 0;
        }
        else if (noEntries > 1)
        {
            listHead = listHead.getNext();
            listHead.setPrevious(null);
            temp.setNext(null);
            temp.setPrevious(null);
            noEntries--;
        }

        return temp;
    }

    /**
     * Remove and return the element at the tail of the list.
     * 
     * @return the last element.
     */

    public final AbstractRecord getRear ()
    {
        AbstractRecord temp = listTail;

        if (noEntries == 1)
        {
            listHead = listTail = null;
            noEntries = 0;
        }
        else if (noEntries > 1)
        {
            listTail = listTail.getPrevious();
            listTail.setNext(null);
            temp.setPrevious(null);
            noEntries--;
        }

        return temp;
    }

    public AbstractRecord getNext (AbstractRecord current)
    {
        AbstractRecord rec = current.getNext();

        if (remove(rec))
            return rec;
        else
            return null;
    }

    /**
     * Insert the entry at the head of the list.
     */

    public final boolean insert (AbstractRecord newRecord)
    {
        /* Do the insert starting at the head of the list */
        return insert(newRecord, listHead);
    }

    public final void print (PrintWriter strm)
    {
        AbstractRecord arp = listHead;

        for (int i = 0; i < noEntries; i++)
        {
            strm.print(arp);
            arp = arp.getNext();
        }
    }

    /**
     * Explicit push onto front of list.
     */

    public final void putFront (AbstractRecord newRecord)
    {
        if (listHead == null)
        {
            listHead = listTail = newRecord;
            newRecord.setNext(null);
            newRecord.setPrevious(null);
        }
        else
        {
            listHead.setPrevious(newRecord);
            newRecord.setPrevious(null);
            newRecord.setNext(listHead);
            listHead = newRecord;
        }

        noEntries++;
    }

    /**
     * Explicit push onto rear of list.
     */

    public final void putRear (AbstractRecord newRecord)
    {
        if (listTail == null)
        {
            listHead = listTail = newRecord;
            newRecord.setNext(null);
            newRecord.setPrevious(null);
        }
        else
        {
            listTail.setNext(newRecord);
            newRecord.setPrevious(listTail);
            newRecord.setNext(null);
            listTail = newRecord;
        }

        noEntries++;
    }

    public final AbstractRecord peekFront ()
    {
        return listHead;
    }

    public final AbstractRecord peekRear ()
    {
        return listTail;
    }

    public final AbstractRecord peekNext (AbstractRecord curr)
    {
        return curr.getNext();
    }

    /*
     * Assume it's in this list!
     */

    public final boolean remove (AbstractRecord oldRecord)
    {
        if (oldRecord == null)
            return false;

        if (noEntries == 1)
        {
            listHead = listTail = null;
            noEntries = 0;
        }
        else if (noEntries > 1)
        {
            if (listHead == oldRecord)
            {
                listHead = listHead.getNext();

                if (listHead != null)
                    listHead.setPrevious(null);

                oldRecord.setNext(null);
                oldRecord.setPrevious(null);
            }
            else
            {
                if (listTail == oldRecord)
                {
                    listTail = listTail.getPrevious();

                    if (listTail != null)
                        listTail.setNext(null);

                    oldRecord.setNext(null);
                    oldRecord.setPrevious(null);
                }
                else
                {
                    if (oldRecord.getPrevious() != null)
                        oldRecord.getPrevious().setNext(oldRecord.getNext());

                    if (oldRecord.getNext() != null)
                        oldRecord.getNext()
                                .setPrevious(oldRecord.getPrevious());
                }
            }

            noEntries--;
        }

        return true;
    }

    /**
     * @return the number of items in the current list.
     */

    public final int size ()
    {
        return noEntries;
    }

    public String toString ()
    {
        AbstractRecord rec = listHead;
        String s = "RecordList:";

        if (rec == null)
            s += " empty";
        else
        {
            while (rec != null)
            {
                s += " " + rec.order();
                rec = rec.getNext();
            }
        }

        return s;
    }

    /**
     * This is a variation on ordered insertion. Insertion obeys the following
     * algorithm. Starting at the record indicated by 'startat' examine each
     * entry in the list in turn and perform the following code 1) If the new
     * record should be merged with the old, call nr.merge passing the old
     * record as an argument and then INSERT the new record IN PLACE OF the old
     * and exit 2) If the new record should replace the old then INSERT the new
     * record IN PLACE OF the old and exit 3) If the new record should be added
     * in addition to the old then INSERT the new record BEFORE the old and exit
     * 4) If the two records are the same (determined by the == operator) simply
     * exit 5) Otherwise determine if the new record should be added here
     * regardless due to the ordering constraints on the list and if so add and
     * exit, otherwise step on to the next element and repeat all the steps.
     * Steps 1-4 effectively ensure that information maintained in any two
     * records for the same object is current either by merging in new
     * information, replacing the old with new, adding in new, or leaving the
     * old alone. Step 5 ensures that if no existing record exists insertion
     * takes place at the correct point
     * 
     * @return <code>true</code> if insertion/replacement took place,
     *         <code>false</code> otherwise.
     */

    private final boolean insert (AbstractRecord newRecord,
            AbstractRecord startAt)
    {
        AbstractRecord current = startAt;

        /*
         * Step through the existing list one record at a time
         */

        while (current != null)
        {
            if (newRecord.shouldMerge(current))
            {
                if (tsLogger.arjLogger.isDebugEnabled()) {
                    tsLogger.arjLogger.debug("RecordList::insert("+this+") : merging "+newRecord.type()+
                            " and "+current.type()+" for "+newRecord.order());
                }

                newRecord.merge(current);
                replace(newRecord, current);

                return true;
            }
            else
            {
                if (newRecord.shouldReplace(current))
                {
                    if (tsLogger.arjLogger.isDebugEnabled()) {
                        tsLogger.arjLogger.debug("RecordList::insert("+this+") : replacing "+current.type()+
                                " and "+newRecord.type()+" for "+newRecord.order());
                    }

                    replace(newRecord, current);

                    return true;
                }
                else
                {
                    if (newRecord.shouldAdd(current))
                    {
                        if (tsLogger.arjLogger.isDebugEnabled()) {
                            tsLogger.arjLogger.debug("RecordList::insert("+this+") : adding extra record of type "+
                                newRecord.type()+" before "+current.type()+" for "+newRecord.order());
                        }

                        insertBefore(newRecord, current);

                        return true;
                    }
                    else
                    {
                        if (newRecord.shouldAlter(current))
                            newRecord.alter(current);

                        if (newRecord.equals(current))
                        {
                            return false;
                        }
                        else if (newRecord.lessThan(current))
                        {
                            if (tsLogger.arjLogger.isDebugEnabled()) {
                                tsLogger.arjLogger.debug("RecordList::insert("+this+") : inserting "+
                                        newRecord.type()+" for "+newRecord.order()+" before "+current.type());
                            }

                            insertBefore(newRecord, current);

                            return true;
                        }

                        current = current.getNext();
                    }
                }
            }
        }

        if (current == null)
        {
            if (tsLogger.arjLogger.isDebugEnabled()) {
                tsLogger.arjLogger.debug("RecordList::insert("+this+") : appending "+newRecord.type()+" for "+newRecord.order());
            }

            putRear(newRecord);
        }
        else
        {
            if (tsLogger.arjLogger.isDebugEnabled()) {
                tsLogger.arjLogger.debug("RecordList::insert("+this+") : inserting "+newRecord.type()+" for "+newRecord.order()+
                        " before "+current.type()+" for "+current.order());
            }

            insertBefore(newRecord, current);
        }

        return true;
    }

    /**
     * Insert the first parameter before the second in the list.
     */

    private final void insertBefore (AbstractRecord newRecord,
            AbstractRecord before)
    {
        /* first do the main link chaining */

        newRecord.setPrevious(before.getPrevious());
        newRecord.setNext(before);
        before.setPrevious(newRecord);

        /* determine if insert was at list head */

        if (newRecord.getPrevious() != null)
            (newRecord.getPrevious()).setNext(newRecord);
        else
            /* must be pointing to the head of the list */
            listHead = newRecord;

        noEntries++;
    }

    private final void replace (AbstractRecord newRecord,
            AbstractRecord oldRecord)
    {
        newRecord.setPrevious(oldRecord.getPrevious());
        newRecord.setNext(oldRecord.getNext());

        if (newRecord.getPrevious() != null)
            (newRecord.getPrevious()).setNext(newRecord);
        else
            listHead = newRecord;

        if (newRecord.getNext() != null)
            (newRecord.getNext()).setPrevious(newRecord);
        else
            listTail = newRecord;

        oldRecord = null;
    }

    protected AbstractRecord listHead;

    private AbstractRecord listTail;

    private int noEntries;

}
