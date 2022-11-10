/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.narayana.osgi.jta;

import javax.management.MBeanException;
import java.util.List;

/**
 * @author <a href="mailto:zfeng@redhat.com">Amos Feng</a>
 * 
 * @deprecated The OSGi module will be removed. Other OSGi Transaction Manager implementations can be used.
 * Release 6.x (Jakarta) will not include OSGi module because:
 * <p/>
 * i) The OSGi compendium fully Jakarta release has not been released yet;
 * ii) Product(s) using Narayana and supporting OSGi has not yet moved to Jakarta;
 * <p/>
 */

@Deprecated
public interface ObjStoreBrowserService {
    void probe() throws MBeanException;
    List<String> types();
    boolean select(String itype);
    void list(String itype);

    void attach(String id);
    void detach();
    void forget(int idx);
    void delete(int idx);

    void start();
    void stop();
}
