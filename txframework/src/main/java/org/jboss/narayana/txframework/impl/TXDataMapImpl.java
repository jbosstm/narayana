package org.jboss.narayana.txframework.impl;

import org.jboss.narayana.txframework.api.exception.TransactionDataUnavailableException;
import org.jboss.narayana.txframework.api.management.TXDataMap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @Author paul.robinson@redhat.com 01/11/2012
 */
public class TXDataMapImpl<K, V> implements TXDataMap<K, V> {

    static final ThreadLocal<Map> mapThreadLocal = new ThreadLocal<Map>();

    public static void resume(Map map)
    {
        mapThreadLocal.set(map);
    }

    public static void suspend()
    {
        mapThreadLocal.remove();
    }

    private Map<K, V> getMap()
    {
        if (mapThreadLocal.get() == null)
        {
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




}
