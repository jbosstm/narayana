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

import java.io.IOException;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.ObjectStoreIterator;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import com.arjuna.ats.internal.arjuna.tools.log.EditableTransaction;

/**
 * Commands:
 * 
 * ls <type> - list logs for specified type. If no type specified then
 * must be already attached.
 * select [<type] - browse a specific type of transaction. Automatically detaches.
 * attach <log> - cannot be attached to another log
 * detach - must be attached to log 
 * forget <pid> - must be attached to log
 * delete <pid> - must be attached to log 
 * types - lists supported transaction types
 * quit - exit
 * help - help
 * 
 * @author marklittle
 */

class LogConsole
{
    public static final int MAX_COMMAND_LEN = 1024; // bytes
    public static final String DEFAULT_TYPE = "AtomicAction";

    private enum Command
    {
        invalid, ls, attach, detach, forget, delete, quit, select, types, help
    };

    public static final String ls = "ls";
    public static final String attach = "attach";
    public static final String detach = "detach";
    public static final String forget = "forget";
    public static final String delete = "delete";
    public static final String quit = "quit";
    public static final String select = "select";
    public static final String types = "types";
    public static final String help = "help";
    
    private static final String SPACE = " ";
    private static final String END = "\n";
    
    public void doWork ()
    {
        boolean attached = false;  // move these to member variables
        boolean exit = false;
        boolean selected = false;

        while (!exit)
        {
            byte[] command = new byte[MAX_COMMAND_LEN];
            
            try
            {
                System.out.print("\n"+_transactionType+" - "+_currentLog+ " > ");

                System.in.read(command);

                String commandString = new String(command);

                Command com = validCommand(commandString);

                switch (com)
                {
                case quit:
                    return;
                case help:
                    help();
                    break;
                case ls:
                    if (!selected)
                        System.err.println("No transaction type selected.");
                    else
                    {
                        if (_currentLog == "")
                            listLogs(_transactionType);
                        else
                            dumpLog(new Uid(_currentLog));
                    }

                    System.out.println();
                    
                    break;
                case select:
                    if (attached)
                    {
                        System.out.println("Detaching from existing log.");
                        
                        attached = false;
                    }

                    setTransactionType(commandString);
                    
                    if ("".equals(_transactionType))
                    {
                        System.err.println("Unsupported type.");
                        
                        selected = false;
                    }
                    else
                        selected = true;
                    
                    break;
                case attach:
                    if (attached)
                        System.err.println("Already attached.");
                    else
                    {
                        setLogId(commandString);
                        
                        if ("".equals(_currentLog))
                        {
                            System.err.println("Invalid log id.");
                        }
                        else
                            attached = true;
                    }
                    break;
                case detach:
                    if (!attached)
                        System.err.println("Not attached.");
                    
                    _currentLog = "";
                    
                    attached = false;
                    break;
                case forget:
                    if (!attached)
                        System.err.println("Not attached.");
                    
                    Uid u = new Uid(_currentLog);
                    EditableTransaction act = TransactionTypeManager.getInstance().getTransaction(_transactionType, u);
                    
                    try
                    {
                        act.moveHeuristicToPrepared(getIndex(commandString));
                    }
                    catch (final IndexOutOfBoundsException ex)
                    {
                        System.err.println("Invalid index.");
                    }

                    dumpLog(u);
                    break;
                case delete:
                    if (!attached)
                        System.err.println("Not attached.");
                    
                    Uid uid = new Uid(_currentLog);
                    EditableTransaction ract = TransactionTypeManager.getInstance().getTransaction(_transactionType, uid);
                    
                    try
                    {
                        ract.deleteHeuristicParticipant(getIndex(commandString));
                    }
                    catch (final IndexOutOfBoundsException ex)
                    {
                        System.err.println("Invalid index.");
                    }
                    
                    dumpLog(uid);
                    
                    break;
                case types:
                    printSupportedTypes();
                    break;
                default:
                    System.err
                            .println("Invalid command " + new String(command));
                    break;
                }
            }
            catch (final Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }
    
    private void dumpLog (final Uid u)
    {
        EditableTransaction act = TransactionTypeManager.getInstance().getTransaction(_transactionType, u);
        
        if (act == null)
            System.out.println("Dump failed! Unknown type "+_transactionType);
        else
            System.out.println(act.toString());
    }
    
    private void printSupportedTypes ()
    {
        System.out.println(DEFAULT_TYPE);
    }
    
    private final Command validCommand (String command)
    {
        if (command == null)
            return Command.invalid;

        if (!command.startsWith(ls))
        {
            if (!command.startsWith(attach))
            {
                if (!command.startsWith(detach))
                {
                    if (!command.startsWith(forget))
                    {
                        if (!command.startsWith(delete))
                        {
                            if (!command.startsWith(quit))
                            {
                                if (!command.startsWith(select))
                                {
                                    if (!command.startsWith(types))
                                    {
                                        if (!command.startsWith(help))
                                            return Command.invalid;
                                        else
                                            return Command.help;
                                    }
                                    else
                                        return Command.types;
                                }
                                else
                                    return Command.select;
                            }
                            else
                                return Command.quit;
                        }
                        else
                            return Command.delete;
                    }
                    else
                        return Command.forget;
                }
                else
                    return Command.detach;
            }
            else
                return Command.attach;
        }
        else
            return Command.ls;
    }

    private final void setTransactionType (String command)
    {
        int index = command.indexOf(SPACE);
        int end = command.indexOf(END);
        
        if (index != -1)
            _transactionType = new String(command.substring(index + 1, end).trim());
        else
            _transactionType = DEFAULT_TYPE;
        
        if (!TransactionTypeManager.getInstance().present(_transactionType))
        {
            System.err.println("Transaction log type "+_transactionType+" not supported.");            

            _transactionType = "";
        }
    }
    
    private final void setLogId (String command)
    {
        int index = command.indexOf(SPACE);
        int end = command.indexOf(END);
        
        if (index != -1)
        {
            _currentLog = new String(command.substring(index + 1, end).trim());

            if (!supportedLog(_currentLog))
                _currentLog = "";
        }
        else
            _currentLog = "";
    }
    
    private final int getIndex (String command)
    {
        int index = command.indexOf(SPACE);
        int end = command.indexOf(END);
        
        if (index != -1)
        {
            try
            {
                return Integer.parseInt(command.substring(index+1, end).trim());
            }
            catch (final Exception ex)
            {
                return -1;
            }
        }
        else
            return -1;
    }

    /*
     * Go through the log and print out all of the instances.
     */
    
    private final void listLogs (String type) throws IOException
    {
        InputObjectState buff = new InputObjectState();
        
        try
        {
            if (StoreManager.getRecoveryStore().allObjUids(type, buff))
            {
                Uid u = null;

                do
                {
                    u = UidHelper.unpackFrom(buff);

                    if (Uid.nullUid().notEquals(u))
                    {
                        System.out.println("Log: " + u);
                    }
                }
                while (Uid.nullUid().notEquals(u));
            }
        }
        catch (final ObjectStoreException ex)
        {
            throw new IOException();
        }
    }
    
    /*
     * Is this a type/instance in the log that we support?
     */
    
    private final boolean supportedLog (String logID)
    {
        Uid id = new Uid(logID);
        
        if (id.equals(Uid.nullUid()))
            return false;

        ObjectStoreIterator iter = new ObjectStoreIterator(StoreManager.getRecoveryStore(), _transactionType);
        Uid u;

        do
        {
            u = iter.iterate();

            if (u.equals(id))
                return true;
        }
        while (Uid.nullUid().notEquals(u));

        return false;
    }
    
    private final void help ()
    {
        System.out.println("\nls <type> - list logs for specified type. If no type specified then must be already attached.");
        System.out.println("select [<type] - browse a specific type of transaction. Automatically detaches.");
        System.out.println("attach <log> - cannot be attached to another log.");
        System.out.println("detach - must be attached to log.");
        System.out.println("forget <pid> - must be attached to log.");
        System.out.println("delete <pid> - must be attached to log.");
        System.out.println("types - lists supported transaction types.");
        System.out.println("quit - exit.");
        System.out.println("help - print out commands.\n");
    }
    
    private String _currentLog = "";
    private String _transactionType = "";
}

public class LogBrowser
{
    public static final void main (String[] args)
    {
        LogConsole console = new LogConsole();

        console.doWork();
    }
}