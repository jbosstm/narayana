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
 * Copyright (C) 2001,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: SocketProcessId.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.StringTokenizer;

import com.arjuna.ats.arjuna.logging.tsLogger;

import com.arjuna.ats.arjuna.exceptions.FatalError;

/**
 * Obtains a unique value to represent the process id via reflection.
 */

public class ExecProcessId implements com.arjuna.ats.arjuna.utils.Process
{

    /**
     * @return the process id. This had better be unique between processes on
     *         the same machine. If not we're in trouble!
     * @message com.arjuna.ats.internal.arjuna.utils.ExecProcessId_1
     *          [com.arjuna.ats.internal.arjuna.utils.ExecProcessId_1]-
     *          Could not get back a valid pid.
     * @message com.arjuna.ats.internal.arjuna.utils.ExecProcessId_2
     *          [com.arjuna.ats.internal.arjuna.utils.ExecProcessId_2] -
     *          Problem executing getpids utility:
     * @message com.arjuna.ats.internal.arjuna.utils.ExecProcessId_3
     *          [com.arjuna.ats.internal.arjuna.utils.ExecProcessId_3] -
     *          Problem executing command:
     * @message com.arjuna.ats.internal.arjuna.utils.ExecProcessId_4
     *          [com.arjuna.ats.internal.arjuna.utils.ExecProcessId_4] -
     *          Problem getting pid information from stream:
     * @message com.arjuna.ats.internal.arjuna.utils.ExecProcessId_5
     *          [com.arjuna.ats.internal.arjuna.utils.ExecProcessId_5] -
     *          Encountered a problem when closing the data stream {0}
     */

    public int getpid ()
    {
        synchronized (ExecProcessId._lock)
        {
            if (_pid == -1)
            {
                String cmd[] = null;
                File tempFile = null;

                if (System.getProperty("os.name").toLowerCase().indexOf(
                        "windows") == -1)
                    cmd = new String[]
                    { "/bin/sh", "-c", "echo $$ $PPID" };
                else
                {
                    try
                    {
                        // http://www.scheibli.com/projects/getpids/index.html (GPL)
                        tempFile = File.createTempFile("getpids", "ts");
    
                        byte[] bytes = new byte[1024];
                        int read;
                        InputStream in  = ExecProcessId.class.getResourceAsStream("getpids.exe");
                        OutputStream out = new FileOutputStream(tempFile);
                        
                        try
                        {
                            while ((read = in.read(bytes)) != -1)
                                out.write(bytes, 0, read);
                        }
                        finally
                        {
                            in.close();
                            out.close();
                        }
    
                        cmd = new String[] { tempFile.getAbsolutePath() };
                    }
                    catch (final Exception ex)
                    {
                        throw new FatalError(
                                tsLogger.log_mesg
                                        .getString("com.arjuna.ats.internal.arjuna.utils.ExecProcessId_2")+" "+ex);
                    }
                }
                
                if (cmd != null)
                {
                    Process p = null;
                    
                    try
                    {
                        p = Runtime.getRuntime().exec(cmd);
                    }
                    catch (final IOException ex)
                    {
                        throw new FatalError(
                                tsLogger.log_mesg
                                        .getString("com.arjuna.ats.internal.arjuna.utils.ExecProcessId_3")+" "+ex);
                    }
                    
                    ByteArrayOutputStream bstream = new ByteArrayOutputStream();                    
                    byte[] bytes = new byte[1024];
                    int read;
                    
                    try
                    {
                        while ((read = p.getInputStream().read(bytes)) != -1)
                            bstream.write(bytes, 0, read);
                    }
                    catch (final Exception ex)
                    {
                        throw new FatalError(
                                tsLogger.log_mesg
                                        .getString("com.arjuna.ats.internal.arjuna.utils.ExecProcessId_4")+" "+ex);
                    }
                    finally
                    {
                        try
                        {
                            bstream.close();
                        }
                        catch (final Exception ex)
                        {
                            if (tsLogger.arjLoggerI18N.isWarnEnabled())
                            {
                                    tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.utils.ExecProcessId_5", new Object[]
                                    { ex });
                            }
                        }
                    }
                    
                    if (tempFile != null)
                        tempFile.delete();

                    StringTokenizer theTokenizer = new StringTokenizer(bstream.toString());
                    theTokenizer.nextToken();
                    
                    String pid = theTokenizer.nextToken();

                    try
                    {
                        _pid = Integer.parseInt(pid);
                    }
                    catch (final Exception ex)
                    {
                        
                    }
                }
            }
        }

        if (_pid == -1)
            throw new FatalError(
                    tsLogger.log_mesg
                            .getString("com.arjuna.ats.internal.arjuna.utils.ExecProcessId_1"));

        return _pid;
    }

    private static final Object _lock = new Object();

    private int _pid = -1;
}
