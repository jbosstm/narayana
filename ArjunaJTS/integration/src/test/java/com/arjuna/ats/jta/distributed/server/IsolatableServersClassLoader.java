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
package com.arjuna.ats.jta.distributed.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

//import sun.misc.Resource;
//import sun.misc.URLClassPath;
import java.net.URLClassLoader;

public class IsolatableServersClassLoader extends ClassLoader {

	private Map<String, Class<?>> clazzMap = new HashMap<String, Class<?>>();
	//private URLClassPath ucp;
	private URLClassLoader ucp;
	private String ignoredPackage;
	private String includedPackage;
	private String otherIgnoredPackage;

	public IsolatableServersClassLoader(String includedPackage, String ignoredPackage, ClassLoader parent) throws SecurityException, NoSuchMethodException,
			IOException {
		super(parent);
		this.includedPackage = includedPackage;
		this.otherIgnoredPackage = ignoredPackage;
		this.ignoredPackage = IsolatableServersClassLoader.class.getPackage().getName();
		String property = System.getProperty("java.class.path");
		String[] split = property.split(System.getProperty("path.separator"));
		List<URL> urls = new ArrayList<URL>();
		for (int i = 0; i < split.length; i++) {
			String url = split[i];
			if (url.endsWith(".jar")) {
				urls.add(new URL("jar:file:" + url + "!/"));
			} else {
				urls.add(new URL("file:" + url + "/"));
			}
		}
		Enumeration<URL> manifestUrls =
				getResources("META-INF/MANIFEST.MF");
		while (manifestUrls.hasMoreElements()) {
			try {
				URL manifestUrl = manifestUrls.nextElement();
				if(manifestUrl.getProtocol().equals("jar")) {
					urls.add(new URL(manifestUrl.getFile().substring(0,
							manifestUrl.getFile().lastIndexOf("!"))));
				}
			} catch (MalformedURLException ex) {
				throw new AssertionError();
			}
		}
		this.ucp = new URLClassPath(urls.toArray(new URL[]{}));
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

		if (clazz == null) {
			if (!name.startsWith("com.arjuna") || name.matches(ignoredPackage + ".[A-Za-z0-9]*")
					|| (includedPackage != null && !name.startsWith(includedPackage))) {
				clazz = getParent().loadClass(name);
			} else {
				String path = name.replace('.', '/').concat(".class");
				URL res = ucp.getResource(path);
				if (res == null) {
					throw new ClassNotFoundException(name);
				}
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				InputStream is = null;
				try {
					try {
						is = res.openStream();
						byte[] byteChunk = new byte[4096];
						int n;

						while ((n = is.read(byteChunk)) > 0) {
							baos.write(byteChunk, 0, n);
						}
					} finally {
						if (is != null) { is.close(); }
					}
					byte[] classData = baos.toByteArray();
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
