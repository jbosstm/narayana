/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2009,
 * @author JBoss by Red Hat.
 */
package com.arjuna.ats.internal.arjuna.coordinator;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/*
 * Encapsulation of a specialised data structure with API and performance characteristics
 * designed specifically for use by the transaction reaper.
 *
 * ReaperElements represent transactions which need timing out. To do this, the reaper needs
 * to wake periodically and process any timeouts that are due. New elements are added on transaction
 * creation and will be removed prior to their timeout if they terminate normally.
 *
 * For high concurrency, normal inserts and removes should not block. However, to determine the next element
 * which needs (will need) processing, the elements must be ordered or at least searched. These requirements
 * are in conflict, since ordering/searching requires stability i.e. locking.
 *
 * To achieve the desired performance characteristics, we combine two data structures: an unsorted, concurrent
 * collection and a sorted, non-threadsafe one which is guarded by the the ReaperElementManager instance lock.
 *
 * Inserts are done, potentially concurrently, to the unsorted hash set. Removes likewise check this first
 * and can return successfully without blocking if the element is found in this collection. Thus the insert/remove
 * are cheap operations.
 *
 * When it is required to know the smallest (i.e. earliest to timeout) element, the contents
 * of the unsorted set are moved to the sorted set. Since this happens infrequently compared to the insert/delete,
 * only a fraction of the elements inserted should ever be copied - most will be removed without ever migrating.
 *
 * Note that additional external synchronization will be needed to ensure first element does not change
 * between getFirst and any operation depending on its timeout value. This is the TransactionReaper's problem. 
 *
 * The sorted set is maintained manually, rather than using Collections.sort or other comparator based structure.
 * This is because compareTo on reaper elements is relatively expensive and we wish to avoid liner scans to minimise
 * the number of such calls. Hence we prefer ArrayList with binary search, despite the higher insert/remove cost
 * compared to LinkedList.
 *
 * Pay careful attention to locking and performance characteristics if altering this class.
 *
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2009-10
 */
public class ReaperElementManager
{
    /**
     * @return the first (i.e. earliest to time out) element of the colleciton.
     */
    public synchronized ReaperElement getFirst() {
        flushPending(); // we need to order the elements before we can tell which is first.
        if(elementsOrderedByTimeout.isEmpty()) {
            return null;
        } else {
            return elementsOrderedByTimeout.get(0);
        }
    }

    // note - unsynchronized for performance.
    public void add(ReaperElement reaperElement) throws IllegalStateException {
        if(pendingInsertions.putIfAbsent(reaperElement, reaperElement) != null) {
            // note this is best effort - we'll allow double inserts if the element is also in the ordered set.
            throw new IllegalStateException();
        }
    }

    /**
     * @param reaperElement the reaper element to reorder in the sorted set.
     * @param delayMillis the ammout of time to increment the element's timeout by.
     * @return the new soonest timeout in the set (not necessarily that of the reordered element)
     */
    public synchronized long reorder(ReaperElement reaperElement, long delayMillis) {
        // assume it must be in the sorted list, as it was likely obtained via getFirst...
        removeSorted(reaperElement);
        // we could add delay to the original timeout, but using current time is probably safer.
        reaperElement.setAbsoluteTimeout((System.currentTimeMillis() + delayMillis));
        // reinsert into its new position.
        insertSorted(reaperElement);

        // getFirst takes care of flushing the pending set for us.
        return getFirst().getAbsoluteTimeout();
    }

    // use only for testing, it's nasty from a performance perspective.
    public synchronized int size() {
        return (elementsOrderedByTimeout.size() + pendingInsertions.size());
    }

    public synchronized boolean isEmpty() {
        return (elementsOrderedByTimeout.isEmpty() && pendingInsertions.isEmpty());
    }

    // strange hack to force instant expire of tx during shutdown.
    public synchronized void setAllTimeoutsToZero() {
        flushPending();
        for(ReaperElement reaperElement : elementsOrderedByTimeout) {
            reaperElement.setAbsoluteTimeout(0);
        }
    }

    // Note - mostly unsynchronized for performance.
    public void remove(ReaperElement reaperElement) {
        if(pendingInsertions.remove(reaperElement) != null) {
            return;
        }

        // we missed finding it in the unsorted set - perhaps it has already been copied to the sorted set...
        synchronized(this) {
            removeSorted(reaperElement);
        }
    }

    ////////////

    // Private methods and structures are guarded where needed by ReaperElementManager instance locks in the
    // public methods - see class header doc comments for concurrency/performance info.

    private final ArrayList<ReaperElement> elementsOrderedByTimeout = new ArrayList<ReaperElement>();
    private final ConcurrentHashMap<ReaperElement, ReaperElement> pendingInsertions = new ConcurrentHashMap<ReaperElement, ReaperElement>();

    private void removeSorted(ReaperElement reaperElement) {
        int location = Collections.binarySearch(elementsOrderedByTimeout, reaperElement);
        if(location >= 0) {
            elementsOrderedByTimeout.remove(location);
        }
    }

    private void insertSorted(ReaperElement reaperElement) {
        int location = Collections.binarySearch(elementsOrderedByTimeout, reaperElement);
        if(location >= 0) {
            throw new IllegalStateException();
        }
        int insertionPoint = -(location + 1);
        elementsOrderedByTimeout.add(insertionPoint, reaperElement);
    }

    private void flushPending() {

        // purge the pending inserts before doing anything else. This is potentially expensive.
        // Future versions may prefer to insert only a portion of the pending set, or
        // iterate it each time to determine the smallest (head) element.
        Set<Map.Entry<ReaperElement,ReaperElement>> entrySet = pendingInsertions.entrySet();
        if(entrySet != null) {
            Iterator<Map.Entry<ReaperElement, ReaperElement>> queueIter = entrySet.iterator();
            // iterator is weakly consistent - will traverse elements present at its time of creation,
            // may or may not see later updates.
            while(queueIter.hasNext()) {
                Map.Entry<ReaperElement,ReaperElement> entry = queueIter.next();
                ReaperElement element = entry.getValue();
                // insert/remove not locked, so we are careful to check that we don't insert
                // an element that has been removed from the pending set by a concurrent thread.
                if(entrySet.remove(entry)) {
                    insertSorted(element);
                }
            }
        }
    }
}
