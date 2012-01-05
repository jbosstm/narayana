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