/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.tools.log;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.arjuna.tools.log.EditableTransaction;


public class LogEditor
{   
    public static final void main (String[] args)
    {
        String txId = null;
        String type = "AtomicAction";
        boolean dump = true;
        int index = -1;
        
        for (int i = 0; i < args.length; i++)
        {
            if ("-tx".equals(args[i]))
                txId = args[i+1];
            if ("-type".equals(args[i]))
                type = args[i+1];
            if ("-dump".equals(args[i]))
                dump = true;
            if ("-forget".equals(args[i]))
            {
                index = Integer.parseInt(args[i+1]);
            }
            if ("-help".equals(args[i]))
            {
                System.out.println("Usage: [-tx <id>] [-type <type>] [-dump] [-forget <index>] [-help]");
                
                return;
            }
        }

        if (txId == null)
        {
            System.err.println("Error - no transaction log specified!");
            
            return;
        }
        
        if (type == null)
        {
            System.err.println("Error - no transaction type specified!");
            
            return;
        }

        EditableTransaction act = TransactionTypeManager.getInstance().getTransaction(type, new Uid(txId));
        
        System.err.println("Recreated transaction.");
        
        if (dump)
            System.err.println(act.toString());
        
        if (index >= 0)
        {
            try
            {
                act.moveHeuristicToPrepared(index);
            
                System.err.println(act.toString());
            }
            catch (final NullPointerException ex)
            {
                System.err.println("No such participant!");
            }
        }
    }
}