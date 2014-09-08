/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.jboss.narayana.txframework.impl;

import org.jboss.narayana.txframework.api.exception.TransactionDataUnavailableException;
import org.jboss.narayana.txframework.api.management.TXDataMap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author paul.robinson@redhat.com 01/11/2012
 */
public class TXDataMapImpl<K, V> implements TXDataMap<K, V> {

    static final ThreadLocal<Map> mapThreadLocal = new ThreadLocal<Map>();

    public static void resume(Map map) {

        mapThreadLocal.set(map);
    }

    public static void suspend() {

        mapThreadLocal.remove();
    }

    private Map<K, V> getMap() {

        if (mapThreadLocal.get() == null) {
            throw new TransactionDataUnavailableException("There is no transaction data associated with this thread");
        }
        return (Map<K, V>) mapThreadLocal.get();
    }


    @Override
    public int size() {

        return getMap().size();
    }

    @Override
    public boolean isEmpty() {

        return getMap().isEmpty();
    }

    @Override
    public boolean containsKey(Object o) {

        return getMap().containsKey(o);
    }

    @Override
    public boolean containsValue(Object o) {

        return getMap().containsValue(o);
    }

    @Override
    public V get(Object key) {

        return getMap().get(key);
    }

    public V put(K k, V v) {

        return (V) getMap().put(k, v);
    }

    @Override
    public V remove(Object o) {

        return getMap().remove(o);
    }

    public void putAll(Map<? extends K, ? extends V> map) {

        getMap().putAll(map);
    }

    @Override
    public void clear() {

        getMap().clear();
    }

    @Override
    public Set<K> keySet() {

        return getMap().keySet();
    }

    @Override
    public Collection<V> values() {

        return getMap().values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {

        return getMap().entrySet();
    }

    @Override
    public boolean equals(Object o) {

        return getMap().equals(o);
    }

    @Override
    public int hashCode() {

        return getMap().hashCode();
    }

    public static Map getState() {

        return mapThreadLocal.get();
    }

    public static boolean isActive() {
        return mapThreadLocal.get() != null;
    }
}
