package com.enrico.launcher3.util;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A utility map from keys to an ArrayList of values.
 */
public class MultiHashMap<K, V> extends HashMap<K, ArrayList<V>> {

    public MultiHashMap() {
    }

    private MultiHashMap(int size) {
        super(size);
    }

    public void addToList(K key, V value) {
        ArrayList<V> list = get(key);
        if (list == null) {
            list = new ArrayList<>();
            list.add(value);
            put(key, list);
        } else {
            list.add(value);
        }
    }

    @Override
    public MultiHashMap<K, V> clone() {
        super.clone();
        MultiHashMap<K, V> map = new MultiHashMap<>(size());
        for (Entry<K, ArrayList<V>> entry : entrySet()) {
            map.put(entry.getKey(), new ArrayList<V>(entry.getValue()));
        }
        return map;
    }
}
