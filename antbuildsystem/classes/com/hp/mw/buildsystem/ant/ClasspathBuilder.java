/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and others contributors as indicated 
 * by the @authors tag. All rights reserved. 
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
package com.hp.mw.buildsystem.ant;

import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.BuildException;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.HashSet;
import java.io.*;

/**
 * ANT task to enable an ANT script to build up a classpath
 * on the fly.
 *
 * @author Richard A. Begg (richard_begg@hp.com)
 */
public class ClasspathBuilder extends org.apache.tools.ant.Task
{
    private static final String FILENAME_DELIMITER = ";";

    protected static long   COUNT = 0;

    protected String[]      _filename = null;
    protected String        _property = null;
    protected ArrayList     _paths = new ArrayList();
    protected boolean       _clear = false;

    /**
     * Set the filename of the file that the classpath will be stored in
     *
     * @param filename The filename of the file to store the classpath in
     */
    public void setFilename(String filename)
    {
        StringTokenizer st = new StringTokenizer(filename,FILENAME_DELIMITER);
        int count = 0;

        _filename = new String[st.countTokens()];

        while (st.hasMoreTokens())
        {
            _filename[count++] = st.nextToken();
        }
    }

    /**
     * Set the clear flag.  If this flag is set (true) then clear the contents
     * of the classpath build file.
     *
     * @param clear True or False
     */
    public void setClear(String clear)
    {
        _clear = new Boolean(clear).booleanValue();
    }

    /**
     * Set the property in which the built classpath is to be placed.
     *
     * @param property The property to store the classpath in
     */
    public void setInproperty(String property)
    {
        _property = property;
    }

    public boolean putClasspathInProperty( String[] filename, String property )
    {
        boolean returnValue = true;
        boolean firstEntry = true;
        StringBuffer classpathString = new StringBuffer();

        /**
         * Open classpath file and read each line into a string buffer putting the necessary classpath
         * separator between each element.
         */
        for (int filenameCount=0;filenameCount<filename.length;filenameCount++)
        {
            try
            {
                BufferedReader in = new BufferedReader( new InputStreamReader( new FileInputStream( filename[filenameCount] ) ) );
                String inLine = null;

                while ( ( inLine = in.readLine() ) != null )
                {
                    if (!firstEntry)
                    {
                        classpathString.append( File.pathSeparator );
                    }
                    else
                    {
                        firstEntry = false;
                    }

                    classpathString.append( inLine );
                }

                in.close();
            }
            catch (java.io.IOException e)
            {
            }
        }

        /**
         * Put the built up classpath into the property
         */
        log("Property '"+property+"' set to classpath ("+classpathString.toString()+")");
        project.setProperty( property, classpathString.toString() );

        return(returnValue);
    }

    public Path createClasspath()
    {
        Path c = new Path(project);
        _paths.add(c);

        return(c);
    }

    public void execute() throws BuildException
    {
        if ( _filename == null )
        {
            throw new BuildException("No filename specified to store built classpath!");
        }

        if (_paths.size() > 0)
        {
            try
            {
                for (int filenameCount=0;filenameCount<_filename.length;filenameCount++)
                {
                    BufferedWriter out = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( _filename[filenameCount], !_clear ) ) );

                    for (int count=0;count<_paths.size();count++)
                    {
                        Object obj = _paths.get(count);

                        if ( obj instanceof Path )
                        {
                            Path path = (Path)obj;
                            String[] paths = path.list();

                            for (int pathCount=0;pathCount<paths.length;pathCount++)
                            {
                                out.write(paths[pathCount] + "\n");
                            }
                        }
                    }

                    out.close();
                }
            }
            catch (java.io.IOException e)
            {
                throw new BuildException("Failed to update file (reason: "+e+")");
            }
        }
        else if (_clear)
        {
            /**
             * If a request to clear the file has been made but no entries have been
             * given then we need to delete the classpath builder file.
             */
            for (int filenameCount=0;filenameCount<_filename.length;filenameCount++)
            {
                new File(_filename[filenameCount]).delete();
            }
        }
        if ( _property != null )
        {
            putClasspathInProperty( _filename, _property );
        }
    }
}
