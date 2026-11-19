/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.arjuna.jta.distributed.example.server;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import sun.misc.Resource;
import sun.misc.URLClassPath;

/**
 * This classloader will reload copies of classes (except a package that is
 * configured for ignoring - the interfaces that the root example requires
 * basically).
 */
public class IsolatableServersClassLoader extends ClassLoader {
	private Map<String, Class<?>> clazzMap = new HashMap<String, Class<?>>();
	private URLClassPath ucp;
	private String ignoredPackage;
	private String includedPackage;
	private String otherIgnoredPackage;

	/**
	 * Create the classloader.
	 * 
	 * @param ignoredPackage
	 *            This package will be ignored by this classloader and delegated
	 *            to its parent so that the example testcase can access required
	 *            interfaces of its test.
	 * @param parent
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws MalformedURLException
	 */

	public IsolatableServersClassLoader(String includedPackage, String ignoredPackage, ClassLoader parent) throws SecurityException, NoSuchMethodException,
			MalformedURLException {
		super(parent);
		this.includedPackage = includedPackage;
		this.otherIgnoredPackage = ignoredPackage;
		this.ignoredPackage = IsolatableServersClassLoader.class.getPackage().getName();
		String property = System.getProperty("java.class.path");
		String[] split = property.split(System.getProperty("path.separator"));
		URL[] urls = new URL[split.length];
		for (int i = 0; i < urls.length; i++) {
			String url = split[i];
			if (url.endsWith(".jar")) {
				urls[i] = new URL("jar:file:" + url + "!/");
			} else {
				urls[i] = new URL("file:" + url + "/");
			}
		}
		this.ucp = new URLClassPath(urls);
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		if (clazzMap.containsKey(name)) {
			return clazzMap.get(name);
		}
		return super.findClass(name);
	}

	public Class<?> loadClass(String name) throws ClassNotFoundException {
		if (!name.matches(ignoredPackage + ".[A-Za-z0-9]*") && otherIgnoredPackage != null && name.startsWith(otherIgnoredPackage)) {
			throw new ClassNotFoundException(name);
		}
		Class<?> clazz = null;
		if (clazzMap.containsKey(name)) {
			clazz = clazzMap.get(name);
		}

		if (clazz != null) {
			System.err.println("Already loaded: " + name);
		} else {
			if (!name.startsWith("com.arjuna") || name.matches(ignoredPackage + ".[A-Za-z0-9]*")
					|| (includedPackage != null && !name.startsWith(includedPackage))) {
				clazz = getParent().loadClass(name);
			} else {

				String path = name.replace('.', '/').concat(".class");
				Resource res = ucp.getResource(path, false);
				if (res == null) {
					throw new ClassNotFoundException(name);
				}
				try {
					byte[] classData = res.getBytes();
					clazz = defineClass(name, classData, 0, classData.length);
					clazzMap.put(name, clazz);
				} catch (IOException e) {
					throw new ClassNotFoundException(name, e);
				}
			}

		}
		return clazz;
	}
}