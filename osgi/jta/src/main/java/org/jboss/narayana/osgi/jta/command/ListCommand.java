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
package org.jboss.narayana.osgi.jta.command;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.action.Argument;
import org.jboss.narayana.osgi.jta.ObjStoreBrowserService;

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
@Command(scope = "narayana", name = "ls", description = "List the transactions")
@Service
public class ListCommand implements Action {

    @Reference
    private ObjStoreBrowserService osb;

    @Argument(index = 0, name = "type", description = "The transaction type", required = false, multiValued = false)
    String type;

    @Override
    public Object execute() throws Exception {

        osb.list(type);
        return null;
    }
}
