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
package org.jboss.jbossts.qa.astests.taskdefs;

import java.util.Collection;
import java.util.ArrayList;

public class ServerTaskException extends Exception
{
    Collection<String> errors = new ArrayList<String> ();
    Exception cause;

    public ServerTaskException()
    {
    }

    public ServerTaskException(String message)
    {
        super(message);
    }

    public ServerTaskException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ServerTaskException(Throwable cause)
    {
        super(cause);
    }

    public void addError(String error)
    {
        errors.add(error);
    }

    public void setCause(Exception cause)
    {
        this.cause = cause;
    }

    public ServerTaskException getServerTaskException()
    {
        if (cause == null && errors.size() == 0)
            return this;

        StringBuilder msg = new StringBuilder();
        String nl = System.getProperty("line.separator");

        for (String error : errors)
        {
            msg.append(error).append(nl);
        }

        //noinspection ThrowableInstanceNeverThrown
        return new ServerTaskException(msg.toString(), cause);
    }
}
