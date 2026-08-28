/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2010
 * @author JBoss Inc.
 */
package quickstart;


import javax.ws.rs.core.Application;
import java.util.Set;
import java.util.HashSet;
import org.jboss.logging.Logger;

import org.jboss.jbossts.star.provider.NotFoundMapper;
import org.jboss.jbossts.star.resource.RESTRecord;
import org.jboss.jbossts.star.provider.TMUnavailableMapper;
import org.jboss.jbossts.star.provider.TransactionStatusMapper;
import org.jboss.jbossts.star.provider.HttpResponseMapper;

public class TMApplication extends Application
{
    private final static Logger log = Logger.getLogger(TMApplication.class);

    HashSet<Object> singletons = new HashSet<Object>();
    Set<Class<?>> classes = new HashSet<Class<?>> ();

    public TMApplication()
    {
        for (Class cl : resourceClasses)
            classes.add(cl);

        for (Class cl : mappers)
            classes.add(cl);

    }

    @Override
    public Set<Class<?>> getClasses()
    {
        return classes;
    }

    @Override
    public Set<Object> getSingletons()
    {
        return singletons;
    }

    private static Class<?>[] mappers = {
    };
    
    private static Class[] resourceClasses = {
            TransactionAwareResource.class,
    };

    private static Object[] resources = {
            new TransactionAwareResource(),
    };
}
