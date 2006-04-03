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
 * Ant task to convert an HP-TS version number to a property filename part.
 * Parameters are <EM>version</EM>, <EM>property</EM>.
 *
 * Takes an HTPS version number in the form <EM>A.B.C</EM> and stores the
 * HPTS property filename part as <EM>_A_B</EM> in the specified Ant property.
 * Generates an empty string for version numbers not in the format
 * <EM>A.B.C</EM>.
 *
 * @author Julian Coleman (julian_coleman@hp.com)
 * @version $Id: VersToProp.java 2342 2006-03-30 13:06:17Z  $
 */

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class VersToProp extends Task {

	private String property, version;

	/**
	 * Set the property name to store the property filename part.
	 */
	public void setProperty(String property) {
		this.property = property;
	}

	/**
	 * Set the version number to convert.
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * Do the conversion and store the result in the specified property.
	 */
	public void execute() throws BuildException {

		int	dot1;
		String	fileVersion = "_";

		try {
			dot1 = version.indexOf('.');
			fileVersion = fileVersion.concat(version.substring(0, dot1));
			fileVersion = fileVersion.concat("_");
			fileVersion = fileVersion.concat(version.substring(dot1 + 1, version.indexOf('.', dot1 + 1)));
			project.setProperty(property, fileVersion);
		}
		catch(ArrayIndexOutOfBoundsException e) {
			project.setProperty(property, "");
		}
		catch(StringIndexOutOfBoundsException e) {
			project.setProperty(property, "");
		}
	}
}
