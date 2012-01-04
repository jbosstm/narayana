/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2009,
 * @author JBoss, a division of Red Hat.
 */
package com.arjuna.common.tests.simple;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.arjuna.common.internal.util.propertyservice.ConcatenationPrefix;
import com.arjuna.common.internal.util.propertyservice.FullPropertyName;
import com.arjuna.common.internal.util.propertyservice.PropertyPrefix;

/**
 * Basic EnvironmentBean for test purposes
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
@PropertyPrefix(prefix = "myprefix.")
public class DummyEnvironmentBean {
    private int myInt = -1;
    private long myLong = -1;
    @FullPropertyName(name = "my_custom_name")
    private String myString = "default";
    private boolean myFirstBoolean = false;
    private boolean mySecondBoolean = true;

    @ConcatenationPrefix(prefix = "my_concat_prefix")
    private volatile List<String> myList = new ArrayList<String>();

    public Properties getProperties() {
        Properties properties = new Properties();
        properties.setProperty("myprefix.myInt", "1");
        properties.setProperty("myprefix.myLong", "1");
        properties.setProperty("my_custom_name", "not_the_default");
        properties.setProperty("myprefix.myFirstBoolean", "NO");
        properties.setProperty("myprefix.mySecondBoolean", "YES");

        properties.setProperty("my_concat_prefix_one", "one");
        properties.setProperty("my_concat_prefix_two", "two");

        return properties;
    }

    public int getMyInt()
    {
        return myInt;
    }

    public void setMyInt(int myInt)
    {
        this.myInt = myInt;
    }

    public long getMyLong()
    {
        return myLong;
    }

    public void setMyLong(long myLong)
    {
        this.myLong = myLong;
    }

    public String getMyString()
    {
        return myString;
    }

    public void setMyString(String myString)
    {
        this.myString = myString;
    }

    public boolean isMyFirstBoolean()
    {
        return myFirstBoolean;
    }

    public void setMyFirstBoolean(boolean myFirstBoolean)
    {
        this.myFirstBoolean = myFirstBoolean;
    }

    public boolean isMySecondBoolean()
    {
        return mySecondBoolean;
    }

    public void setMySecondBoolean(boolean mySecondBoolean)
    {
        this.mySecondBoolean = mySecondBoolean;
    }

    public List<String> getMyList()
    {
        return myList;
    }

    public void setMyList(List<String> myList)
    {
        this.myList = myList;
    }
}