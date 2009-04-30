/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2008,
 * @author JBoss Inc.
 */
package org.jboss.jbosstm.tools.mbean;

import org.jboss.jbosstm.tools.embedded.EmbeddedToolsFramework;

/**
 * Service mbean implementation for starting jbosstm tools via a service archive
 *
 */
public class EmbeddedTools implements EmbeddedToolsMBean
{
    private static String MBEAN_NAME = "Transaction Tools";

    public void startEmbeddedTools()
    {
      new Thread()
      {
         public void run()
         {
                new EmbeddedToolsFramework();
         }
      }
      .start();
    }

    public String getName()
    {
        return MBEAN_NAME;
    }

    public int getState() {
        return 0;
    }

    public String getStateString()
    {
        return null;
    }

    public void jbossInternalLifecycle(String string) throws Exception
    {

    }

    public void create() throws Exception
    {
    }

    public void start() throws Exception
    {
    }

    public void stop()
    {
    }

    public void destroy()
    {      
    }
}
