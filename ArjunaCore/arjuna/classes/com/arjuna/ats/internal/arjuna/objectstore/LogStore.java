/*
 * Copyright (C) 1998, 1999, 2000-2009,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: LogStore.java,v 1.4 2004/11/11 12:22:21 nmcl Exp $
 */

package com.arjuna.ats.internal.arjuna.objectstore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.SyncFailedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.objectstore.ObjectStoreType;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.objectstore.StateType;
import com.arjuna.ats.arjuna.state.*;
import com.arjuna.ats.arjuna.utils.FileLock;

import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;

import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import com.arjuna.ats.internal.arjuna.objectstore.LogInstance.TransactionData;

/**
 * This is the transaction log implementation. It is optimised for the typical
 * mode of the coordinator: write-once and never read or update. Reads or
 * updates occur only in the case of failures, which hopefully are rare; hence
 * the reason we optimise for the non-failure case. This does mean that recovery
 * may take longer than when using other log implementations.
 *
 * There are several implementations of this approach, some of which perform better
 * on one operating system than another. We may put them in to the source eventually
 * and make it clear for which OS combination they are best suited. However, this
 * implementation works well on all operating systems we have tested so is a good
 * default.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: LogStore.java,v 1.4 2004/11/11 12:22:21 nmcl Exp $
 * @since JTS 1.0.
 */

/**
 * Algorithm used: During normal execution of a transaction, we only ever write
 * and then remove the log entry; we never read it. Therefore, optimise for that
 * situation. The log continually builds up in size until a maximum capacity is
 * reached and in which case, we switch to another log. Meanwhile, the recovery
 * manager periodically runs through completed logs and removes those that are
 * no longer needed, truncating those that require recovery (which cannot
 * complete at this time). When writing the initial log entry, we write a
 * redzone marker, followed by the entry size and then the actual entry. Since a
 * log is never shared between VMs, we only need to synchronize between the
 * threads within a given VM: the recovery manager never works on a log that is
 * being used by another VM anyway. The end of a log is marked with a
 * termination record. Obviously if a crash occurs, then no such record will
 * have been written and in which case, the recovery manager determines that the
 * log is no longer required via timeout heuristics.
 *
 * The implementation normally writes removal records to the end of the log
 * when an entry is deleted. This can be disabled and in which case we end up in
 * the same situation as if a failure occurred as the removal record was being written
 * or a crash happened before remove_committed could succeed on any of the other
 * file-based object store implementations: we potentially try to commit transactions
 * that have terminated (either committed or rolled back). In which case we ...
 *
 * (i) call commit on a state that has already been committed and fail to do so. Will
 * eventually move the log record elsewhere and the administrator can deal with it.
 *
 * (ii) call commit on a state that has already been rolled back and again fail to do so.
 * Will eventually move the log record elsewhere as above.
 *
 * If we do not write removal records then we would end up in a situation of trying to
 * commit every log instance multiple times. As such we always try to write records but
 * do them either synchronously or asynchronously (periodically). Of course there's still
 * the chance that a failure will cause problems in both sync and async cases, but we
 * have reduced the probability as well as the number of such problem items. The periodicity
 * of this is the same as pruning the log, i.e., the same thread does both jobs.
 *
 * By default we synchronously add the removal marker to the log, i.e., when remove_committed
 * returns, the marker entry has been appended to the log.
 *
 * NOTE: there is a race where we terminate the log instance and yet transactions may
 * still be using it. This happens with other object store implementations too. However, in
 * this case we could end up with a log that should be deleted because all of the entries
 * have gone. We try to fix this up through allObjUids. If recovery works correctly then
 * these states will eventually get deleted.
 *
 * TODO
 *
 * When truncating logs we write a shadow and then overwrite the original with the shadow
 * when finished. If there is a crash we could end up with the shadow as well as the
 * original. Recovery could tidy this up for us - as long as we have the original then
 * we can continue to recover - the shadow instance may be corrupted so best to ignore
 * it and simply delete it. But we would need to ensure that we didn't delete a shadow that
 * is actually still active.
 *
 * Also we do not use a primary and backup log approach. Whenever we need a new log instance we
 * create one. This means that there could be many logs being used at the same time, which could
 * be a problem for disk space (unlikely these days, but possible). If this approach gets to
 * be an issue then we can limit the number of log instances created.
 */

/**
 * Represents a specific log instance.
 *
 * @author mlittle
 *
 */

class LogInstance
{
	public class TransactionData
	{
		TransactionData (final Uid tx, final long off, final LogInstance parent)
		{
			txId = tx;
			offset = off;
			container = parent;
		}

		public final Uid txId;
		public final long offset;
		public final LogInstance container;
	}

	public LogInstance(String tn, long size)
	{
		_logName = new Uid();
		_typeName = tn;
		_frozen = false;
		_totalSize = size;
	}

	/*
	 * Once frozen we will not use the log again except for recovery and
	 * pruning.
	 *
	 * We could consider another algorithm that reuses the log once it has
	 * dropped below a threshold size. Probably not worth it at the moment.
	 */

	public final boolean isFrozen()
	{
		return _frozen;
	}

	public final void freeze() // one way operation.
	{
		_frozen = true;
	}

	public final int numberOfUsers()
	{
		return _transactions.size();
	}

	public final Uid getName()
	{
		return _logName;
	}

	public final String getTypeName()
	{
		return _typeName;
	}

	public final InputObjectState allObjUids () throws ObjectStoreException
	{
		OutputObjectState state = new OutputObjectState();
		Iterator<Uid> iter = _ids.keySet().iterator();

		try
		{
			while (iter.hasNext())
			{
			    UidHelper.packInto(iter.next(), state);
			}

			// don't forget to null terminate

			UidHelper.packInto(Uid.nullUid(), state);
		}
		catch (final IOException ex)
		{
			throw new ObjectStoreException(ex);
		}

		return new InputObjectState(state);
	}

	public final boolean present(Uid id)
	{
		return _ids.containsKey(id);
	}

	public final TransactionData getTxId (Uid txId)
	{
		return new TransactionData(txId, _used, this);
	}

	public final TransactionData addTxId (Uid txId, long size)
	{
		TransactionData td = new TransactionData(txId, _used, this);

		_transactions.add(td);  // allow multiple entries in the same log
		_ids.put(txId, txId);
		_used += size;

		return td;
	}

	public final long remaining()
	{
		return _totalSize - _used;
	}

	public final void resize (long size)
	{
		_totalSize = size;
	}

	public String toString()
	{
		return "LogInstance < " + _logName + ", " + _typeName + ", "
				+ numberOfUsers() + ", " + remaining() + " >";
	}

	private Uid _logName;
	private String _typeName;
	private boolean _frozen;
	private Stack<TransactionData> _transactions = new Stack<TransactionData>();
	private HashMap<Uid, Uid> _ids = new HashMap<Uid, Uid>();
	private long _used = 0;
	private long _totalSize;
}

/*
 * Time based, but it would be good to have it triggered on the number of
 * entries that need to be added.
 */

class LogPurger extends Thread
{
	private enum Status {ACTIVE, PASSIVE, TERMINATED};

	class LogElement
	{
		public LogElement(final String t, final Uid u, final int s)
		{
			tn = t;
			uid = u;
			state = s;
		}

		public String tn;
		public Uid uid;
		public int state;
	};

	/*
	 * Purge every N seconds.
	 *
	 * TODO purge after number of logs > M
	 */

	public static final long DEFAULT_PURGE_TIME = 100000; // 100 seconds

	public LogPurger(LogStore instance)
	{
		this(instance, DEFAULT_PURGE_TIME);
	}

	public LogPurger(LogStore instance, long purgeTime)
	{
        super("Log Purger");
		_objStore = instance;
		_purgeTime = purgeTime;
	}

	public void addRemovedState(final Uid u, final String tn, final int state)
	{
		synchronized (_entries)
		{
			_entries.put(u, new LogElement(tn, u, state));
		}
	}

	public void purge()
	{
		try
		{
			_objStore.truncateLogs(true);
		}
		catch (final Exception ex)
		{
		}
	}

	public void writeRemovalEntries()
	{
		synchronized (_entries)
		{
			if (_entries.size() > 0)
			{
				Collection<LogElement> entries = _entries.values();
				Iterator<LogElement> iter = entries.iterator();

				while (iter.hasNext())
				{
					LogElement val = iter.next();

					try
					{
						_objStore.removeState(val.uid, val.tn, val.state);
					}
					catch (final Exception ex)
					{
						// TODO log warning, but there's nothing else we can do.
					}
				}

				_entries.clear();
			}
		}
	}

	/**
	 * Poke the thread into doing some work even if it normally
	 * would not.
	 */

	public void trigger ()
	{
		synchronized (_lock)
		{
			if (_status == Status.PASSIVE)
				_lock.notify();
		}
	}

	public void run()
	{
		for (;;)
		{
			// TODO activate thread during read and get it to write deleted states

			try
			{
				synchronized (_lock)
				{
					_status = Status.PASSIVE;

					_lock.wait(_purgeTime);
				}
			}
			catch (final Exception ex)
			{
				_status = Status.ACTIVE;
			}

			/*
			 * Write any asynchronous delete records.
			 */

			writeRemovalEntries();

			/*
			 * Now truncate any logs we've been working on.
			 */

			try
			{
				_objStore.truncateLogs();
			}
			catch (final Exception ex)
			{
			}
		}

		// _status = Status.TERMINATED;
	}

	private HashMap<Uid, LogElement> _entries = new HashMap<Uid, LogElement>();
	private long _purgeTime;
	private LogStore _objStore;
	private Status _status;
	private Object _lock = new Object();
}

class PurgeShutdownHook extends Thread
{
	public PurgeShutdownHook(LogPurger purger)
	{
		_purger = purger;
	}

	public void run()
	{
		_purger.writeRemovalEntries(); // flush everything in the cache first.
		_purger.purge();
	}

	private LogPurger _purger;
}

/*
 * Derive it directly from FSStore for now, simply because we are unlikely to
 * have many log instances in the store. However, if it becomes a problem, then
 * we can simply derive from the HashedActionStore.
 */

public class LogStore extends FileSystemStore
{
	public static final long LOG_SIZE = 10 * 1024 * 1024;  // default maximum log size in bytes

	private static final String FILE_MODE = "rwd";

	public int typeIs()
	{
		return ObjectStoreType.ACTIONLOG;
	}

	/**
	 * Normally returns the current state of the log entry. However, this is
	 * never called during normal (non-recovery) execution. Therefore, the
	 * overhead of having to scan all of the logs (if it's not one we're using)
	 * is minimal.
	 */

	public int currentState(Uid objUid, String tName)
			throws ObjectStoreException
	{
		InputObjectState ios = new InputObjectState();

		/*
		 * TODO
		 *
		 * It's possible that the entry has been marked to be deleted but
		 * that the removal entry hasn't been written yet. We could check the
		 * async cache. However, since we really only care about this during
		 * recovery, it's not going to cause us  problems anyway.
		 */

		if (allObjUids(tName, ios, StateStatus.OS_UNKNOWN))
		{
			Uid tempUid = new Uid(Uid.nullUid());

			do
			{
				try
				{
				    tempUid = UidHelper.unpackFrom(ios);
				}
				catch (final Exception ex)
				{
					ex.printStackTrace();

					return StateStatus.OS_UNKNOWN;
				}

				if (tempUid.equals(objUid))
					return StateStatus.OS_COMMITTED;

			} while (tempUid.notEquals(Uid.nullUid()));

			return StateStatus.OS_UNKNOWN;
		}
		else
			return StateStatus.OS_UNKNOWN;
	}

	/**
	 * Commit a previous write_state operation which was made with the SHADOW
	 * StateType argument. This is achieved by renaming the shadow and removing
	 * the hidden version.
	 */

	public boolean commit_state(Uid objUid, String tName)
			throws ObjectStoreException
	{
		return true;
	}

	public boolean hide_state(Uid u, String tn) throws ObjectStoreException
	{
		if (tsLogger.arjLogger.isDebugEnabled()) {
            tsLogger.arjLogger.debug("LogStore.hide_state(" + u + ", " + tn + ")");
        }

		return false;
	}

	public boolean reveal_state(Uid u, String tn) throws ObjectStoreException
	{
		if (tsLogger.arjLogger.isDebugEnabled()) {
            tsLogger.arjLogger.debug("LogStore.reveal_state(" + u + ", " + tn + ")");
        }

		return false;
	}

	public InputObjectState read_uncommitted(Uid u, String tn)
			throws ObjectStoreException
	{
		if (tsLogger.arjLogger.isDebugEnabled()) {
            tsLogger.arjLogger.debug("LogStore.read_uncommitted(" + u + ", " + tn + ")");
        }

		return null;
	}

	public boolean remove_uncommitted(Uid u, String tn)
			throws ObjectStoreException
	{
		if (tsLogger.arjLogger.isDebugEnabled()) {
            tsLogger.arjLogger.debug("LogStore.remove_uncommitted(" + u + ", " + tn + ")");
        }

		return false;
	}

	public boolean write_committed(Uid storeUid, String tName,
			OutputObjectState state) throws ObjectStoreException
	{
		if (tsLogger.arjLogger.isDebugEnabled()) {
            tsLogger.arjLogger.debug("LogStore.write_committed(" + storeUid + ", "
                    + tName + ")");
        }

		try
		{
			return super.write_committed(storeUid, tName, state);
		}
		catch (ObjectStoreException ex)
		{
			removeFromLog(storeUid);

			throw ex;
		}
	}

	public boolean write_uncommitted(Uid u, String tn, OutputObjectState s)
			throws ObjectStoreException
	{
		if (tsLogger.arjLogger.isDebugEnabled()) {
            tsLogger.arjLogger.debug("LogStore.write_uncommitted(" + u + ", " + tn + ", " + s
                    + ")");
        }

		return false;
	}

	public boolean allLogUids (String tName, InputObjectState state, int match) throws ObjectStoreException
	{
		return super.allObjUids(tName, state, match);
	}

	/**
	 * This is a recovery-only method and should not be called during normal
	 * execution. As such we need to load in all of the logs we can find that
	 * aren't already loaded (or activated).
	 */

	public boolean allObjUids(String tName, InputObjectState state, int match)
			throws ObjectStoreException
	{
		/*
		 * match will always be OS_COMMITTED since that's all we ever write for
		 * the logs.
		 */

		// in case of asynchronous removals trigger the purger now.

		_purger.trigger();

		/*
		 * Get a list of logs. Load them in to memory if we aren't already
		 * working on them/it. But we can prune the entry once we're
		 * finished or the memory footprint will grow. We should do this
		 * for all frozen entries eventually too.
		 */

		InputObjectState logs = new InputObjectState();
		OutputObjectState objUids = new OutputObjectState();

		/*
		 * We never call this method except during recovery. As such we shouldn't
		 * need to worry about optimizations such as checking whether or not the
		 * log is in current working memory.
		 */

		if (!super.allObjUids(tName, logs, match))
			return false;
		else
		{
			/*
			 * Now we have all of the log names let's attach to each one
			 * and locate the committed instances (not deleted.)
			 */

			Uid logName = new Uid(Uid.nullUid());

			try
			{
				do
				{
				    logName = UidHelper.unpackFrom(logs);

					if (logName.notEquals(Uid.nullUid()))
					{
						/*
						 * Could check to see if log is in current working memory.
						 */

						/*
						 * TODO
						 *
						 * First purge the log if we can, but we need to know that
						 * we're not playing with an instance that is being manipulated
						 * from another VM instance.
						 */

						ArrayList<InputObjectState> txs = scanLog(logName, tName);

						if (txs.size() > 0)
						{
							for (int i = 0; i < txs.size(); i++)
							{
							    UidHelper.packInto(txs.get(i).stateUid(), objUids);
							}
						}
					}
				} while (logName.notEquals(Uid.nullUid()));

				// remember null terminator

				UidHelper.packInto(Uid.nullUid(), objUids);

				state.setBuffer(objUids.buffer());
			}
			catch (final IOException ex)
			{
				ex.printStackTrace();

				return false;
			}

			return true;
		}
	}

	public LogStore(String locationOfStore)
	{
		this(locationOfStore, StateType.OS_SHARED);
	}

	public LogStore(String locationOfStore, int shareStatus)
	{
		super(shareStatus);

		try
		{
			setupStore(locationOfStore);
		}
		catch (ObjectStoreException e)
		{
			if (tsLogger.arjLoggerI18N.isWarnEnabled())
				tsLogger.arjLogger.warn(e);

			super.makeInvalid();

			throw new com.arjuna.ats.arjuna.exceptions.FatalError(e.toString(), e);
		}
	}

	public LogStore()
	{
		this(StateType.OS_SHARED);
	}

	public LogStore(int shareStatus)
	{
		super(shareStatus);
		
		try
                {
                        setupStore(arjPropertyManager.getObjectStoreEnvironmentBean().getLocalOSRoot());
                }
                catch (ObjectStoreException e)
                {
                        if (tsLogger.arjLoggerI18N.isWarnEnabled())
                                tsLogger.arjLogger.warn(e);

                        super.makeInvalid();

                        throw new com.arjuna.ats.arjuna.exceptions.FatalError(e.toString(), e);
                }
	}

	protected synchronized boolean setupStore(String location)
			throws ObjectStoreException
	{
        if (!checkSync)
        {
            if(arjPropertyManager.getObjectStoreEnvironmentBean().isTransactionSync()) {
                syncOn();
            } else {
                syncOff();
            }
        }

        checkSync = true;

		if (_purger == null)
		{
			_purger = new LogPurger(this, _purgeTime);
			_purger.setDaemon(true);

			Runtime.getRuntime()
					.addShutdownHook(new PurgeShutdownHook(_purger));

			_purger.start();
		}

		return super.setupStore(location);
	}

	/**
	 * Unlock and close the file. Note that if the unlock fails we set the
	 * return value to false to indicate an error but rely on the close to
	 * really do the unlock.
	 */

	protected boolean unlockAndClose(File fd, RandomAccessFile rf)
	{
		if (tsLogger.arjLogger.isDebugEnabled()) {
            tsLogger.arjLogger.debug("RandomAccessFile.unlockAndClose(" + fd + ", " + rf + ")");
        }

		boolean closedOk = unlock(fd);

		try
		{
			rf.close();
		}
		catch (Exception e)
		{
			closedOk = false;
		}

		return closedOk;
	}

	/**
	 * write_state saves the ObjectState in a file named by the type and Uid of
	 * the ObjectState. If the second argument is SHADOW, then the file name is
	 * different so that a subsequent commit_state invocation will rename the
	 * file.
	 *
	 * We need to make sure that each entry is written to the next empty location
	 * in the log even if there's already an entry for this tx.
	 */

	protected boolean write_state(Uid objUid, String tName,
			OutputObjectState state, int ft) throws ObjectStoreException
	{
		if (tsLogger.arjLogger.isDebugEnabled()) {
            tsLogger.arjLogger.debug("ShadowingStore.write_state(" + objUid + ", " + tName
                    + ", " + StateType.stateTypeString(ft) + ")");
        }

		if (!storeValid())
			return false;

		String fname = null;
		File fd = null;

		if (tName != null)
		{
			int imageSize = (int) state.length();
			byte[] uidString = objUid.stringForm().getBytes();
			int buffSize = _redzone.length + uidString.length + imageSize + 8;  // don't put in endOfLog since we keep overwriting that.
			RandomAccessFile ofile = null;
			java.nio.channels.FileLock lock = null;

			if (imageSize > 0)
			{
				TransactionData theLogEntry = getLogName(objUid, tName, buffSize);		// always adds entry to log
				LogInstance theLog = theLogEntry.container;

				if (theLog == null)
					throw new ObjectStoreException();

				fname = genPathName(theLog.getName(), tName, ft);
				fd = openAndLock(fname, FileLock.F_WRLCK, true);

				if (fd == null)
				{
					if (tsLogger.arjLoggerI18N.isWarnEnabled()) {
                        tsLogger.i18NLogger.warn_objectstore_ShadowingStore_18(fname);
                    }

					return false;
				}

				boolean setLength = !fd.exists();

				try
				{
					ofile = new RandomAccessFile(fd, FILE_MODE);

					if (setLength)
					{
						ofile.setLength(_maxFileSize);
					}
					else
					{
						// may have to resize file if we keep updating this transaction info

						if (theLog.remaining() < buffSize)
						{
							long size = ofile.length() + buffSize - theLog.remaining();

							ofile.setLength(size);

							theLog.resize(size);
						}
					}

					java.nio.ByteBuffer buff = java.nio.ByteBuffer.allocate(buffSize);

					buff.put(_redzone);
					buff.putInt(uidString.length);
					buff.put(uidString);
					buff.putInt(imageSize);
					buff.put(state.buffer());

					synchronized (_lock)
					{
						ofile.seek(theLogEntry.offset);

						ofile.write(buff.array());
					}
				}
				catch (SyncFailedException e)
				{
					unlockAndClose(fd, ofile);

					throw new ObjectStoreException(
							"ShadowingStore::write_state() - write failed to sync for "
							+ fname, e);
				}
				catch (FileNotFoundException e)
				{
					unlockAndClose(fd, ofile);

					e.printStackTrace();

					throw new ObjectStoreException(
							"ShadowingStore::write_state() - write failed to locate file "
							+ fname + ": " + e, e);
				}
				catch (IOException e)
				{
					unlockAndClose(fd, ofile);

					e.printStackTrace();

					throw new ObjectStoreException(
							"ShadowingStore::write_state() - write failed for "
							+ fname + ": " + e, e);
				}
				finally
				{
					try
					{
						if (lock != null)
							lock.release();
					}
					catch (IOException ex)
					{
						ex.printStackTrace();
					}
				}
			}

			if (!unlockAndClose(fd, ofile))
			{
				if (tsLogger.arjLoggerI18N.isWarnEnabled()) {
                    tsLogger.i18NLogger.warn_objectstore_ShadowingStore_19(fname);
                }
			}

			super.addToCache(fname);

			return true;
		}
		else
			throw new ObjectStoreException(
                    "ShadowStore::write_state - "
                            + tsLogger.i18NLogger.get_objectstore_notypenameuid()
							+ objUid);
	}

	/**
	 * Shouldn't be called during normal execution only during recovery.
	 */

	protected InputObjectState read_state(Uid u, String tn, int s)
			throws ObjectStoreException
	{
		/*
		 * In case of asynchronous removals of state, let's trigger the purger
		 * thread to flush its cache now. Try to avoid false positives during
		 * recovery wherever possible!
		 */

		_purger.trigger();

		/*
		 * It's possible that recovery got hold of a state id while it was
		 * being deleted (marker written and pruning thread not yet active).
		 * In which case when it comes to do a read it's not going to find
		 * the state there any longer. Conversely it's possible that it could do
		 * a read on a state that is about to be deleted. Recovery should be
		 * able to cope with these edge cases.
		 */

		TransactionData td = getLogName(u, tn, -1);

		if (td == null)
			throw new ObjectStoreException();

		ArrayList<InputObjectState> states = scanLog(td.container.getName(), tn);

		if ((states == null) || (states.size() == 0))
			return null;

		for (int i = 0; i < states.size(); i++)
		{
			if (states.get(i).stateUid().equals(u))
				return states.get(i);
		}

		/*
		 * Not in the log, so probably removed by now.
		 */

		return null;
	}

	/**
	 * Does nothing except indicate that this thread is finished with the log on
	 * behalf of this transaction.
	 */

	protected boolean remove_state(Uid u, String tn, int s)
			throws ObjectStoreException
	{
		// maybe write a removal entry into the log.

		try
		{
			/*
			 * If we don't add a removal entry then recovery has to work a
			 * little harder to figure things out. But it has to cater for the
			 * situation where a removal record write fails anyway, so this
			 * shouldn't be a big deal. On the up side it improves performance
			 * by 30% for this implementation, which is a 40% improvement over
			 * the basic file-based log!
			 */

			/*
			 * If we write a removal record as a separate entity to the original
			 * data item then we cannot ensure that they will go into the same
			 * log with a pre-set size for the log. Therefore, we have two
			 * options:
			 *
			 * (i) find the old entry in the log and mark it as deleted.
			 * (ii) increase the size of the log to accommodate the removal entry.
			 *
			 * We currently go for option (ii) as this is the quickest.
			 */

			if (_synchronousRemoval)
			{
				OutputObjectState removalState = new OutputObjectState(u, tn);

				removalState.packBytes(_removedState);

				if (!write_state(u, tn, removalState, s))
					throw new ObjectStoreException();
			}
			else
				_purger.addRemovedState(u, tn, s);
		}
		catch (IOException ex)
		{
			throw new ObjectStoreException(ex.toString(), ex);
		}
		catch (final Throwable ex)
		{
		    ex.printStackTrace();
		    
		    throw new ObjectStoreException(ex.toString(), ex);
		}
		finally
		{
			removeFromLog(u);
		}

		return true;
	}

	protected boolean lock(File fd, int lmode, boolean create)
	{
		return true;
	}

	protected boolean unlock(File fd)
	{
		return true;
	}

	protected String genPathName (Uid objUid, String tName, int ft) throws ObjectStoreException
    {
		String fname = super.genPathName(objUid, tName, ft);

		if (ft == StateStatus.OS_UNCOMMITTED)
			fname = fname + HIDDENCHAR;

		return fname;
    }

	boolean removeState(Uid u, String tn, int s) throws ObjectStoreException
	{
		try
		{
			OutputObjectState removalState = new OutputObjectState(u, tn);

			removalState.packBytes(_removedState);

			if (!write_state(u, tn, removalState, s))
				throw new ObjectStoreException();
		}
		catch (IOException ex)
		{
			throw new ObjectStoreException(ex.toString(), ex);
		}

		return true;
	}

	boolean truncateLogs () throws ObjectStoreException
	{
		return truncateLogs(false);
	}

	boolean truncateLogs (boolean force) throws ObjectStoreException
	{
		synchronized (_logNames)
		{
			Iterator<LogInstance> iter = _logNames.iterator();

			/*
			 * Only do this for logs that are full to save time,
			 * except if we are terminating.
			 */

			while (iter.hasNext())
			{
				boolean delete = false;
				LogInstance log = null;

				try
				{
					log = iter.next();

					if (log.isFrozen() || force)
						delete = truncateLog(log, force);
				}
				catch (final Exception ex)
				{
					// TODO log
				}

				if (delete)
					iter.remove();
			}
		}

		return true;
	}

	/*
	 * Return true if the log needs to be deleted.
	 */

	private final boolean truncateLog(final LogInstance log, boolean force) throws ObjectStoreException
	{
		boolean delete = false;

		synchronized (_lock)
		{
			File fd = new File(genPathName(log.getName(), log.getTypeName(), StateStatus.OS_COMMITTED));

			try
			{
				/*
				 * Create a list of ObjectState entries.
				 */

				ArrayList<InputObjectState> objectStates = scanLog(log.getName(), log.getTypeName());

				/*
				 * At this stage we should now have a list of unique
				 * entries. Write them back to the log. Do this
				 * atomically! If the list is empty then delete the
				 * file!
				 */

				if ((objectStates != null) && (objectStates.size() > 0))
				{
					/*
					 * If we are terminating then we can truncate the log to the
					 * real size needed to contain the existing entries since we
					 * will not use it again within another VM except for
					 * recovery purposes.
					 */

					String fname = genPathName(log.getName(), log.getTypeName(), StateStatus.OS_UNCOMMITTED);
					File fd2 = openAndLock(fname, FileLock.F_WRLCK, true);
					RandomAccessFile oFile = new RandomAccessFile(fd2, FILE_MODE);
					int size = 0;

					oFile.setLength(_maxFileSize);

					for (int i = 0; i < objectStates.size(); i++)
					{
						byte[] uidString = objectStates.get(i).stateUid().stringForm().getBytes();
						int buffSize = _redzone.length + uidString.length + objectStates.get(i).buffer().length + 8;
						java.nio.ByteBuffer buff = java.nio.ByteBuffer.allocate(buffSize);

						size += buffSize;

						try
						{
							buff.put(_redzone);
							buff.putInt(uidString.length);
							buff.put(uidString);
							buff.putInt(objectStates.get(i).buffer().length);
							buff.put(objectStates.get(i).buffer(),0, objectStates.get(i).buffer().length);
						}
						catch (final Exception ex)
						{
							ex.printStackTrace();

							// TODO log

							fd2.delete();

							unlockAndClose(fd2, oFile);

							throw new ObjectStoreException(ex.toString(), ex);
						}
					}

					try
					{
						if (force)
						{
							oFile.setLength(size);

							log.freeze();
						}

						fd2.renameTo(fd);
					}
					catch (final Exception ex)
					{
						ex.printStackTrace();

						// TODO log

						throw new ObjectStoreException(ex.toString(), ex);
					}
					finally
					{
						unlockAndClose(fd2, oFile);
					}
				}
				else
				{
					/*
					 * Delete the log if there are no states in it. We could
					 * keep the file around and reuse it, but the advantage of
					 * this is small compared to having to cope with reusing old
					 * log instances.
					 */

					fd.delete();

					/*
					 * Remember to remove the information from the memory cache.
					 */

					delete = true;
				}
			}
			catch (final ObjectStoreException ex)
			{
				ex.printStackTrace();

				throw ex;
			}
			catch (final Exception ex)
			{
				ex.printStackTrace();

				throw new ObjectStoreException(ex.toString(), ex);
			}
		}

		return delete;
	}

	private final ArrayList<InputObjectState> scanLog (final Uid logName, final String typeName) throws ObjectStoreException
	{
		/*
		 * Make sure no new entries can be created while we scan.
		 */

		synchronized (_lock)
		{
			try
			{
				String fname = genPathName(logName, typeName, StateStatus.OS_COMMITTED);
				File fd = openAndLock(fname, FileLock.F_WRLCK, true);
				RandomAccessFile iFile = new RandomAccessFile(fd, FILE_MODE);
				// iFile.getChannel().lock();

				try
				{
					/*
					 * Create a list of ObjectState entries.
					 */

					ArrayList<InputObjectState> objectStates = new ArrayList<InputObjectState>();

					iFile.seek(0); // make sure we're at the start

					while (iFile.getFilePointer() < iFile.length())
					{
						byte[] buff = new byte[_redzone.length];

						iFile.read(buff);

						if (!redzoneProtected(buff))
						{
							// end

							break;

							/*
							 * TODO add an end-of-log entry and check for that. Currently just assume
							 * that no RZ means end, rather than corruption.
							 */
						}
						else
						{
							int uidSize = iFile.readInt();
							byte[] uidString = new byte[uidSize];

							iFile.read(uidString);

							Uid txId = new Uid(new String(uidString));
							int imageSize = iFile.readInt();
							byte[] imageState = new byte[imageSize];

							iFile.read(imageState);

							try
							{
								InputObjectState state = new InputObjectState(
										txId, "", imageState);

								objectStates.add(state);
							}
							catch (final Exception ex)
							{
								ex.printStackTrace();

								throw new ObjectStoreException(ex.toString(), ex);
							}
						}
					}

					unlockAndClose(fd, iFile);
					iFile = null;

					/*
					 * At this stage we now have a list of ObjectState entries.
					 * Now we need to go through and prune the list. This is
					 * complicated by the fact that there can be 1.. entries for
					 * a specific transaction since we continually update the
					 * log as we drive recovery. If an entry hasn't been deleted
					 * then we will keep the latest one we find.
					 */

					/*
					 * First search for those entries that have been deleted.
					 */

					ArrayList<InputObjectState> deletedLogs = new ArrayList<InputObjectState>();

					for (int i = 0; i < objectStates.size(); i++)
					{
						InputObjectState curr = objectStates.get(i);

						try
						{
							if (Arrays.equals(curr.unpackBytes(), _removedState))
							{
								deletedLogs.add(curr);
							}
							else
								curr.reread();  // don't forget to reset the read pointer!
						}
						catch (final Exception ex)
						{
							// if not a delete record then the first entry won't
							// be an the defined byte array.
                            curr.reread();  // don't forget to reset the read pointer!
						}
					}

					if (deletedLogs.size() > 0)
					{
						/*
						 * make sure we remove them from the first list to save time.
						 */

						objectStates.removeAll(deletedLogs);

						deleteEntries(objectStates, deletedLogs);

						/*
						 * At this stage we should only have entries that refer
						 * to in-flight transactions. Go through the list and
						 * remove N-1 references for each transaction id.
						 */

						pruneEntries(objectStates);

						/*
						 * Now return the list of committed entries.
						 */

						return objectStates;
					}
					else
						return objectStates;
				}
				finally
				{
					if (iFile != null)
						unlockAndClose(fd, iFile);
				}
			}
			catch (final ObjectStoreException ex)
			{
				ex.printStackTrace();

				throw ex;
			}
			catch (final Exception ex)
			{
				ex.printStackTrace();

				throw new ObjectStoreException(ex.toString(), ex);
			}
		}
	}

	private final boolean redzoneProtected(final byte[] buff)
	{
		for (int i = 0; i < _redzone.length; i++)
		{
			if (buff[i] != _redzone[i])
				return false;
		}

		return true;
	}

	private final void deleteEntries(ArrayList<InputObjectState> allStates,
			ArrayList<InputObjectState> deletedStates)
	{
		/*
		 * Look through the remaining states for entries that have been deleted.
		 */

		for (int i = 0; i < deletedStates.size(); i++)
		{
			Uid txId = deletedStates.get(i).stateUid();

			for (int j = 0; j < allStates.size(); j++)
			{
				if (allStates.get(j).stateUid().equals(txId))
					allStates.remove(j);
			}
		}

		deletedStates.clear();
	}

	private final void pruneEntries(ArrayList<InputObjectState> allStates)
	{
		/*
		 * The ArrayList is ordered with the earliest entries first.
		 */

		for (int j = allStates.size() - 1; j >= 0; j--)
		{
			Uid txId = allStates.get(j).stateUid();

			for (int i = 0; i < j; i++)
			{
				if (allStates.get(i).stateUid().equals(txId))
					allStates.remove(i);
			}
		}
	}

	/*
	 * We maintain a list of log identifiers and the number of threads using
	 * them. If a log size goes over the maximum allowed, then we swap all
	 * threads to a new log with the exception of those that are currently using
	 * the old log.
	 *
	 * We always add a new entry to the log even if one already exists.
	 *
	 * Because normally we are writing to the log we pass in the size that we need to
	 * accommodate. However, during recovery we need to read the state yet still
	 * need the log name. So if we pass a size of -1 this signifies only to
	 * return the log data and not allocate space for a new instance.
	 */

	private final TransactionData getLogName (Uid txid, String tName, long size)
            throws ObjectStoreException
    {
        synchronized (_logNames)
        {
            Iterator<LogInstance> iter = _logNames.iterator();
            LogInstance entry = null;

            /*
             * First check to see if the TxId is in an existing log. Always
             * return the same log instance for the same txid so we can
             * keep all data in the same location. This may mean that we have
             * to extend the size of the log over time to accommodate situations
             * where the log is modified but not deleted for a while, e.g., during
             * recovery.
             */

            while (iter.hasNext())
            {
                entry = (LogInstance) iter.next();

                if (entry.present(txid))
                {
                	if (size == -1) // we are reading only
                		return entry.getTxId(txid);
                	else
                		return entry.addTxId(txid, size);
                }
            }

            /*
             * If we get here then this TxId isn't in one of the
             * logs we are maintaining currently. So go back through
             * the list of logs and find one that is small enough
             * for us to use. The first one with room will do.
             */

            iter = _logNames.iterator();

            while (iter.hasNext())
            {
                entry = (LogInstance) iter.next();

                if (!entry.isFrozen())
                {
                	if (entry.remaining() > size)
                	{
                        return entry.addTxId(txid, size);
                    }
                    else
                    {
                    	/*
                    	 * TODO
                    	 *
                    	 * When can we remove the information about this
                    	 * log from memory? If we do it too soon then it's possible
                    	 * that delete entries will not go into the right log. If we
                    	 * leave it too late then the memory footprint increases. Prune
                    	 * the entry when we prune the log from disk?
                    	 */

                    	entry.freeze();
                    }
                }
            }

            // if we get here, then we need to create a new log

            entry = new LogInstance(tName, _maxFileSize);
            _logNames.add(entry);

            return entry.addTxId(txid, size);
        }
    }

	private final void removeFromLog(Uid txid)
	{
		if (_synchronousRemoval)
		{
			synchronized (_logNames)
			{
				Iterator<LogInstance> iter = _logNames.iterator();
				LogInstance entry = null;

				while (iter.hasNext())
				{
					entry = (LogInstance) iter.next();

					if (entry.present(txid))
					{
						//entry.removeTxId(txid);
						break;
					}
				}
			}
		}
	}

	private static boolean checkSync = false;

	private static Object _lock = new Object();

	private static ArrayList<LogInstance> _logNames = new ArrayList<LogInstance>();

	private static long _maxFileSize = LOG_SIZE;
	private static long _purgeTime = LogPurger.DEFAULT_PURGE_TIME;

	private static LogPurger _purger;
	private static boolean _synchronousRemoval = false;

	private static final byte[] _redzone = { 0x2, 0x4, 0x6, 0x8 };

	private static final byte[] _removedState = { 0xd, 0xe, 0xa, 0xd, 0xb, 0xe, 0xe, 0xf };

	private static final char HIDDENCHAR = '~';

	static
	{
            _synchronousRemoval = arjPropertyManager.getObjectStoreEnvironmentBean().isSynchronousRemoval();

            _purgeTime = arjPropertyManager.getObjectStoreEnvironmentBean().getPurgeTime();

			_maxFileSize = arjPropertyManager.getObjectStoreEnvironmentBean().getTxLogSize();
	}
}
