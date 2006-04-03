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

/**
 * Ant task to take individual module jar files and create product jar files
 * from them.  Parameters are <EM>destdir</EM>, <EM>destfile</EM>,
 * <EM>workdir</EM> and a fileset containing the complete list of module jar
 * files.
 *
 * Unpacks the module jar files into the work directory.  Jar files with a
 * <EM>-orbname</EM> suffix are unpacked into <EM>-orbname</EM>
 * directories.  Jar files without the suffix are unpacked into the default
 * directory.  All these directories are then packed into jar files in the
 * destination directory and are named with the prefix <EM>destfile</EM>
 * converted to lower case.
 *
 * @author Julian Coleman (julian_coleman@hp.com)
 * @version $Id: ModuleToProduct.java 2342 2006-03-30 13:06:17Z  $
 */

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.taskdefs.Mkdir;
import org.apache.tools.ant.types.FileSet;

public class ModuleToProduct extends org.apache.tools.ant.Task {

	private String	workdir = ".work";
	private String	destdir, destfile;
	private List	 libs = new ArrayList() ;
	private HashSet orbList = new HashSet();
	private boolean ignoreOrb = false ;

	/**
	 * Set the destination directory.
	 */
	public void setDestdir(String destdir) {
		this.destdir = destdir;
	}

	/**
	 * Set the destination filename root.
	 */
	public void setDestfile(String destfile) {
		this.destfile = destfile.toLowerCase();
	}

	/**
	 * Set the temporary working directory.
	 */
	public void setWorkdir(String workdir) {
		this.workdir = workdir;
	}

	/**
	 * Set the list of .jar files to process.
	 */
	public void addFileset(FileSet fileSet) {
		libs.add(fileSet) ;
	}

	/**
	 * Set the ignore orb glaf.
	 */
	public void setIgnoreorb(final boolean ignoreOrb) {
		this.ignoreOrb = ignoreOrb;
	}

	/**
	 * Unjar list of jar files into separate directories.
	 * Jar directories into separate product jar files.
	 */
	public void execute() throws BuildException {
		DirectoryScanner	ds;
		int	hyphen;
		String	orbName, srcDir, destDir;
		Expand	unjar = new Expand();
		Delete  delete = new Delete();
		Mkdir	mkdir = new Mkdir();
		Jar	jar = new Jar();

		try {
			/* Remove any existing work directory */
			delete.setProject(getProject());
			delete.setDir(new File(workdir));
			delete.execute();
	
			/* Get list of files */
			final int numLibs = libs.size() ;
			for(int count = 0 ; count < numLibs ; count++)
			{
				final FileSet fileSet = (FileSet)libs.get(count) ;
				ds = fileSet.getDirectoryScanner(project);
				ds.scan();
				final String[] files = ds.getIncludedFiles() ;
				for (int i = 0; i < files.length;i++) {
					/* Build extract directory name */
					destDir = workdir.concat("/classes");
					hyphen = files[i].lastIndexOf('-');
					orbName = new String();
					if (!ignoreOrb && (hyphen > 0)) {
						orbName = orbName.concat(
					    	files[i].substring(hyphen,
					    	files[i].indexOf(".jar")));
						destDir = destDir.concat(orbName);
						if (!orbList.contains(orbName)) {
							orbList.add(orbName);
						}
					} else {
						if (!orbList.contains("")) {
							orbList.add("");
						}
					}
					/* Unpack jar file */
					unjar.setProject(getProject());
					unjar.setDest(new File(destDir));
					unjar.setSrc(new File(ds.getBasedir(), files[i]));
					unjar.setOverwrite(true);
					unjar.execute();
				}
			}
			for (Iterator i = orbList.iterator(); i.hasNext();) {
				/* Delete META-INF directory */
				orbName = i.next().toString();
				srcDir = workdir.concat("/classes");
				srcDir = srcDir.concat(orbName);
				srcDir = srcDir.concat("/META-INF");
				delete.setDir(new File(srcDir));
				delete.execute();
	
				/* Create destination directory */
				mkdir.setProject(getProject());
				mkdir.setDir(new File(destdir));
				mkdir.execute();
	
				/* Create new jar files */
				srcDir = workdir.concat("/classes");
				srcDir = srcDir.concat(orbName);
				destDir = destdir.concat("/");
				destDir = destDir.concat(destfile);
				destDir = destDir.concat(orbName);
				destDir = destDir.concat(".jar");
				jar.setProject(getProject());
				jar.setBasedir(new File(srcDir));
				jar.setJarfile(new File(destDir));
				jar.execute();
			}
			/* Remove the work directory */
			delete.setDir(new File(workdir));
			delete.execute();
		}
		catch (Exception e) {
			throw new BuildException(e);
		}
	}
}
