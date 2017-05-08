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
 * Copyright (C) 2003, 2004,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: CacheStore.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna.objectstore;

import java.util.LinkedList;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.objectstore.StateType;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;

/**
 * A cached object store implementation.
 * 
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: CacheStore.java 2342 2006-03-30 13:06:17Z $
 * @since JTS 3.0.
 */

public class CacheStore extends HashedStore
{
    static final int NO_STATE_TYPE = -1;

    public boolean commit_state (Uid objUid, String tName)
            throws ObjectStoreException
    {
        return CacheStore._storeManager.addWork(this, AsyncStore.COMMIT,
                objUid, tName, null, CacheStore.NO_STATE_TYPE);
    }

    public void sync () throws java.io.SyncFailedException,
            ObjectStoreException
    {
        CacheStore._storeManager.flush();
    }

    protected boolean remove_state (Uid objUid, String name, int ft)
            throws ObjectStoreException
    {
        /*
         * Is it in the cache?
         */

        int status = CacheStore._storeManager.removeState(objUid, ft);

        if (status == AsyncStore.REMOVED)
            return true;

        /*
         * Check to see if there's a state on disk. If there is, then we still
         * need to add a remove work item to the cache queue.
         */

        int fileState = currentState(objUid, name);

        if ((fileState != StateStatus.OS_UNKNOWN)
                || (status == AsyncStore.IN_USE))
        {
            return CacheStore._storeManager.addWork(this, AsyncStore.REMOVE,
                    objUid, name, null, fileState);
        }
        else
        {
            if (fileState == StateStatus.OS_UNKNOWN)
                return false;
            else
                return true;
        }
    }

    protected boolean write_state (Uid objUid, String tName,
            OutputObjectState state, int ft) throws ObjectStoreException
    {
        /*
         * If there is already a write operation in the cache for exactly this
         * state and type, then remove it and any corresponding remove_state
         * there might be. This is because write_state overwrites the state
         * rather than appending some operational work. If we used an appender
         * log then obviously this isn't appropriate.
         */

        int status = CacheStore._storeManager.removeWriteState(objUid, ft);

        return CacheStore._storeManager.addWork(this, AsyncStore.WRITE, objUid,
                tName, state, ft);
    }

    /**
     * Before we look at the disk let's look in the state cache first for the
     * state, just in case it hasn't been written out to persistent store yet.
     */

    protected InputObjectState read_state (Uid objUid, String tName, int ft)
            throws ObjectStoreException
    {
        OutputObjectState state = CacheStore._storeManager.getState(objUid, ft);

        if (state == null) // not in the cache
            return super.read_state(objUid, tName, ft);
        else
            return new InputObjectState(state);
    }

    /*
     * The methods that do the real work when the thread gets round to it.
     */

    protected boolean commitState (Uid objUid, String tName)
            throws ObjectStoreException
    {
        return super.commit_state(objUid, tName);
    }

    protected boolean removeState (Uid objUid, String name, int ft)
            throws ObjectStoreException
    {
        return super.remove_state(objUid, name, ft);
    }

    protected boolean writeState (Uid objUid, String tName,
            OutputObjectState state, int ft) throws ObjectStoreException
    {
        return super.write_state(objUid, tName, state, ft);
    }

    public CacheStore(ObjectStoreEnvironmentBean objectStoreEnvironmentBean) throws ObjectStoreException
    {
        super(objectStoreEnvironmentBean);

        super.syncWrites = objectStoreEnvironmentBean.isCacheStoreSync();
    }

    static final AsyncStore _storeManager = new AsyncStore();
}

class StoreElement
{

    public StoreElement(CacheStore s, int tow, Uid ou, String tn,
            OutputObjectState st, int ft)
    {
        store = s;
        typeOfWork = tow;
        objUid = new Uid(ou);
        tName = tn;
        state = st;
        fileType = ft;
        removed = false;
    }

    public void remove ()
    {
        store = null;
        typeOfWork = AsyncStore.NO_WORK;
        objUid = null;
        tName = null;
        state = null;
        fileType = CacheStore.NO_STATE_TYPE;
        removed = true;
    }

    public String toString ()
    {
        return "< " + typeOfWork + ", " + objUid + ", " + tName + ", "
                + fileType + ", " + removed + " >";
    }

    public CacheStore store;

    public int typeOfWork;

    public Uid objUid;

    public String tName;

    public OutputObjectState state;

    public int fileType;

    public boolean removed;

}

class ShutdownThread extends Thread // used to flush on exit
{

    public ShutdownThread()
    {
        super("CacheStoreShutdownThread");
    }

    public void run ()
    {
        CacheStore._storeManager.flush();

        synchronized (CacheStore._storeManager)
        {
            CacheStore._storeManager.notifyAll();
        }

        synchronized (CacheStore._storeManager._activeLock)
        {
            /*
             * We don't want to exit the VM if the worker thread is currently
             * writing to the disk. That would be very bad :-(! So, just check
             * that the thread isn't actively doing work.
             */

          if (tsLogger.logger.isTraceEnabled()) {
              tsLogger.logger.trace("ShutdownThread.run () - terminating");
          }
      }
   }

}

class AsyncStore extends Thread // keep priority same as app. threads
{

    public static final int NO_WORK = -1;

    public static final int COMMIT = 0;

    public static final int REMOVE = 1;

    public static final int WRITE = 2;

    public static final int IN_USE = 3;

    public static final int REMOVED = 4;

    public static final int NOT_PRESENT = 5;

    public AsyncStore()
    {
        super("AsyncStoreThread");

        _maximumCacheSize = _defaultCacheSize;
        _maximumWorkItems = _defaultWorkItems;
        _maximumRemovedItems = _defaultRemovedItems;
        _scanPeriod = _defaultScanPeriod;

        _workList = new LinkedList[HASH_SIZE];

        setDaemon(true);

        Runtime.getRuntime().addShutdownHook(new ShutdownThread());

        start();
    }

    public final void flush ()
    {
        /*
         * Do it this way because by the time we get here the daemon thread has
         * been removed by the system.
         */

        boolean stop = false;

        do
        {
            synchronized (_workList)
            {
                stop = _numberOfEntries <= 0;
            }

            if (!stop)
            {
                doWork();
            }
        }
        while (!stop);

        _terminated = true;
    }

    public final boolean addWork (CacheStore store, int workType, Uid objUid,
            String tName, OutputObjectState state, int ft)
    {
        /*
         * If the cache is full already, then wait until it has drained. We sit
         * in a while/do loop because many threads could be blocked and only one
         * may get a chance to add something to the queue before it goes full
         * again.
         */

        boolean stop = false;

        do
        {
            synchronized (_overflowLock)
            {
                if (cacheIsFull())
                {
                    try
                    {
                        _overflowLock.wait();
                    }
                    catch (Exception ex)
                    {
                    }
                }
                else
                    stop = true;
            }
        }
        while (!stop);

        StoreElement toAdd = new StoreElement(store, workType, objUid, tName,
                state, ft);

        getList(objUid).addFirst(toAdd);

        synchronized (_workList)
        {
            if (state != null)
                _currentCacheSize += state.size();

            _numberOfEntries++;
        }

        return true;
    }

    /**
     * Remove any item in the cache that operates on this state (must be
     * identical uid and file type (shadowed, hidden etc.) This could
     * potentially leave us with states on disk that should have been deleted
     * but weren't because a crash happened before we could do that. Crash
     * recovery should fix this up later though.
     */

    public final int removeState (Uid objUid, int ft)
    {
        Object[] elements = null;

        /*
         * Check the cache first. Take a snapshot.
         */

        try
        {
            elements = getList(objUid).toArray();
        }
        catch (Exception ex)
        {
            elements = null;
        }

        int status = NOT_PRESENT;
        
        if (elements == null)
            return status;

        synchronized (elements)
        {
            for (int i = 0; i < elements.length; i++)
            {
                StoreElement element = (StoreElement) elements[i];
    
                if ((element != null) && !element.removed
                        && element.objUid.equals(objUid))
                {
                    switch (element.typeOfWork)
                    {
                    case AsyncStore.REMOVE:
                        element.remove();
    
                        synchronized (_workList)
                        {
                            _removedItems++;
                        }
    
                        if (status != IN_USE)
                            status = REMOVED;
    
                        break;
                    case AsyncStore.WRITE:
                        // if (element.fileType == ft)
                    {
                        synchronized (_workList)
                        {
                            if (element.state != null)
                                _currentCacheSize -= element.state.size();
    
                            _removedItems++;
                        }
    
                        element.remove();
    
                        if (status != IN_USE)
                            status = REMOVED;
                    }
    
                        break;
                    default:
                        break;
                    }
                }
            }
        }

        if (cacheIsFull())
        {
            synchronized (_workList)
            {
                _workList.notify();
            }
        }
        
        /*
         * Does the worker thread currently have it?
         */

        if (CacheStore._storeManager.currentWork(objUid, ft))
        {
            status = IN_USE;
        }

        return status;
    }

    public final int removeWriteState (Uid objUid, int ft)
    {
        int status = NOT_PRESENT;
        Object[] elements = null;

        try
        {
            elements = getList(objUid).toArray();
        }
        catch (Exception ex)
        {
            elements = null;
        }

        if (elements == null)
            return status;

        for (int i = 0; i < elements.length; i++)
        {
            StoreElement element = (StoreElement) elements[i];

            if ((element != null) && !element.removed
                    && element.objUid.equals(objUid))
            {
                switch (element.typeOfWork)
                {
                case AsyncStore.WRITE:
                    if (ft == element.fileType)
                    {
                        synchronized (_workList)
                        {
                            if (element.state != null)
                                _currentCacheSize -= element.state.size();

                            _removedItems++;
                        }

                        element.remove();

                        status = REMOVED;
                    }
                    else
                    {
                        if (ft == StateType.OS_ORIGINAL)
                        {
                            if (element.fileType == StateType.OS_SHADOW)
                            {
                                synchronized (_workList)
                                {
                                    if (element.state != null)
                                        _currentCacheSize -= element.state
                                                .size();

                                    _removedItems++;
                                }

                                element.remove();

                                status = REMOVED;
                            }
                        }
                    }

                    break;
                case AsyncStore.COMMIT:
                    if (ft == StateType.OS_ORIGINAL)
                    {
                        synchronized (_workList)
                        {
                            if (element.state != null)
                                _currentCacheSize -= element.state.size();

                            _removedItems++;
                        }

                        element.remove();

                        status = REMOVED;
                    }

                    break;
                default:
                    break;
                }
            }
        }

        if (cacheIsFull())
        {
            synchronized (_workList)
            {
                _workList.notify();
            }
        }
        
        return status;
    }

    public final OutputObjectState getState (Uid objUid, int ft)
    {
        LinkedList list = getList(objUid);

        synchronized (list)
        {
            for (int i = 0; i < list.size(); i++)
            {
                StoreElement element = (StoreElement) list.get(i);

                if ((element != null) && !element.removed && (element.objUid.equals(objUid)))
                {
                    if (element.fileType == ft)
                        return element.state;
                }
            }

            /*
             * If not in cache then maybe we're working on it?
             */

            synchronized (_workList)
            {
                if ((_work != null) && (objUid.equals(_work.objUid)))
                    return _work.state;
            }
        }

        return null;
    }

    public void run ()
    {
        synchronized (_workList)
        {
            try
            {
                _workList.wait();
            }
            catch (Exception ex)
            {
            }
        }

        while (!_terminated)
        {
            synchronized (_activeLock)
            {
                while (!queueIsEmpty()) // drain the queue
                {
                    doWork();
                }

                /*
                 * We've drained the queue, so notify any blocked threads so
                 * that they get a chance to add something to the queue again.
                 */

                synchronized (_overflowLock)
                {
                    _overflowLock.notifyAll();
                }
            }

            synchronized (_workList)
            {
                if (!cacheIsFull())
                {
                    try
                    {
                        _workList.wait(_scanPeriod);
                    }
                    catch (Exception ex)
                    {
                    }
                }
            }
        }
    }

    public boolean currentWork (Uid objUid, int ft)
    {
        try
        {
            synchronized (_workList)
            {
                if (_work != null)
                {
                    if (_work.objUid.equals(objUid) && (_work.fileType == ft))
                    {
                        return true;
                    }
                }
            }
        }
        catch (Exception ex)
        {
        }

        return false;
    }

    private final void doWork ()
    {
        synchronized (_workList)
        {
            LinkedList list = getList();

            if (list != null)
            {
            	synchronized (list)
            	{
	                try
	                {
	                    _work = (StoreElement) list.removeLast();
	
	                    _numberOfEntries--;
	
	                    if ((_work.state != null) && !_work.removed)
	                        _currentCacheSize -= _work.state.size();
	
	                    if (_work.removed)
	                    {
	                        _removedItems--;
	                    }
	                }
	                catch (java.util.NoSuchElementException ex)
	                {
	                    _work = null;
	                }
            	}
            }
            else
                _work = null;
        }

        if ((_work != null) && !_work.removed)
        {
            /*
             * Should write any errors to a persistent log so that an admin tool
             * can pick up the pieces later.
             */

            try
            {
                switch (_work.typeOfWork) {
                    case AsyncStore.COMMIT: {
                        if (!_work.store.commitState(_work.objUid, _work.tName)) {
                            tsLogger.i18NLogger.warn_objectstore_CacheStore_1(_work.objUid, _work.tName);
                        }
                    }
                    break;
                    case AsyncStore.REMOVE: {
                        if (!_work.store.removeState(_work.objUid, _work.tName,
                                _work.fileType)) {
                            tsLogger.i18NLogger.warn_objectstore_CacheStore_2(_work.objUid, _work.tName,
                                    Integer.toString(_work.fileType));
                        }
                    }
                    break;
                    case AsyncStore.WRITE: {
                        if (!_work.store.writeState(_work.objUid, _work.tName,
                                _work.state, _work.fileType)) {
                            tsLogger.i18NLogger.warn_objectstore_CacheStore_3(_work.objUid, _work.tName,
                                    _work.state.toString(), Integer.toString(_work.fileType));
                        }
                    }
                    break;
                    default:
                        tsLogger.i18NLogger.warn_objectstore_CacheStore_4(Integer.toString(_work.typeOfWork));
                        break;
                }
            }
            catch (ObjectStoreException ex)
            {
                ex.printStackTrace();
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }

        _work = null;
    }

    /**
     * @return true if the queue is empty, false otherwise.
     */

    private final boolean queueIsEmpty ()
    {
        synchronized (_workList)
        {
            if (_numberOfEntries == 0)
                return true;
            else
                return false;
        }
    }

    /**
     * @return true if the cache is full, false otherwise.
     */

    private final boolean cacheIsFull ()
    {
        synchronized (_workList)
        {
            if ((_currentCacheSize >= _maximumCacheSize)
                    || (_removedItems >= _maximumRemovedItems)
                    || (_numberOfEntries - _removedItems >= _maximumWorkItems))
            {
                _workList.notifyAll();

                return true; // cache is full, so wait
            }
            else
                return false; // cache is ok
        }
    }

    private final LinkedList getList ()
    {
        for (int i = 0; i < HASH_SIZE; i++)
        {
            if ((_workList[i] != null) && (_workList[i].size() > 0))
                return _workList[i];
        }

        return null;
    }

    private final LinkedList getList (Uid objUid)
    {
        int index = objUid.hashCode() % HASH_SIZE;

        synchronized (_workList)
        {
            if (_workList[index] == null)
                _workList[index] = new LinkedList();

            return _workList[index];
        }
    }

    public Object _activeLock = new Object();

    private LinkedList[] _workList = null;

    private int _numberOfEntries = 0;

    private boolean _terminated = false;

    private int _currentCacheSize = 0;

    private int _maximumCacheSize = 0;

    private int _maximumWorkItems = 0;

    private int _maximumRemovedItems = 0;

    private int _scanPeriod = 0;

    private Object _overflowLock = new Object();

    private StoreElement _work = null;

    private int _removedItems = 0;

    private static final int HASH_SIZE = arjPropertyManager.getObjectStoreEnvironmentBean().getCacheStoreHash();

    private static final int _defaultCacheSize = arjPropertyManager.getObjectStoreEnvironmentBean().getCacheStoreSize();

    private static final int _defaultRemovedItems = arjPropertyManager
            .getObjectStoreEnvironmentBean().getCacheStoreRemovedItems();

    private static final int _defaultWorkItems = arjPropertyManager.getObjectStoreEnvironmentBean().getCacheStoreWorkItems();

    private static final int _defaultScanPeriod = arjPropertyManager.getObjectStoreEnvironmentBean().getCacheStoreScanPeriod();

}
