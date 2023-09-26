/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.tools.log;

import java.util.HashMap;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.arjuna.tools.log.EditableAtomicAction;
import com.arjuna.ats.internal.arjuna.tools.log.EditableTransaction;

/*
 * A default implementation for the default logstore implementation.
 */

class AtomicActionTypeMap implements TransactionTypeManager.TransactionTypeMap
{
    public EditableTransaction getTransaction (final Uid u)
    {
        return new EditableAtomicAction(u);
    }
    
    public String getType ()
    {
        return "AtomicAction";
    }
    
    public String getRealType ()
    {
        return _type;
    }
    
    private static final String _type = new AtomicAction().type();
}

public class TransactionTypeManager
{
    /**
     * Only allows the movement of heuristic participants to the prepared list.
     * Maybe allow general editing of both lists, including bidirectional
     * movement (point?) and deletion.
     */

    public interface TransactionTypeMap
    {
        public EditableTransaction getTransaction (final Uid u);

        public String getType (); // the shorthand name (could be the same as getRealType iff we want people to write really loooong strings).
        
        public String getRealType ();  // the real type (used by object store operations)
    }

    public EditableTransaction getTransaction (final String type, final Uid u)
    {
        if (type == null)
            throw new IllegalArgumentException();

        TransactionTypeMap map = _maps.get(type);

        if (map != null)
            return map.getTransaction(u);
        else
            return null;
    }
    
    public String getTransactionType (final String type)
    {
        if (type == null)
            throw new IllegalArgumentException();

        TransactionTypeMap map = _maps.get(type);

        if (map != null)
            return map.getRealType();
        else
            return null;
    }
    
    /**
     * Is this transaction log one we support?
     * 
     * @param type the name of the log.
     * @return true if supported, false otherwise.
     */
    
    public boolean present (final String type)
    {
        return (_maps.get(type) != null);
    }

    public void addTransaction (TransactionTypeMap map)
    {
        if (map == null)
            throw new IllegalArgumentException();

        _maps.put(map.getType(), map);
    }

    public void removeTransaction (String type)
    {
        if (type == null)
            throw new IllegalArgumentException();

        _maps.remove(type);
    }

    public static TransactionTypeManager getInstance ()
    {
        return _manager;
    }

    /*
     * All log implementations that we want to support should be in here
     * so they are registered. We could use a dynamic approach, but we don't
     * have a rapidly growing number of object stores anyway to justify that
     * overhead.
     */
    
    private TransactionTypeManager()
    {   
        addTransaction(new AtomicActionTypeMap());
    }

    private HashMap<String, TransactionTypeMap> _maps = new HashMap<String, TransactionTypeMap>();

    private static final TransactionTypeManager _manager = new TransactionTypeManager();
}