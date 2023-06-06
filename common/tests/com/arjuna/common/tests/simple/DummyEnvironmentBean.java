/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
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