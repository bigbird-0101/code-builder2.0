package com.fpp.code.common;

import java.io.File;
import java.util.*;

/**
 * key的顺序按照文件顺序
 *
 * @author fpp
 * @date 2020-5-15
 */
public class OrderedProperties extends Properties {

    private static final long serialVersionUID = -4627607243846121965L;
    private File file;
    private final LinkedHashSet<Object> keys = new LinkedHashSet<Object>();

    public OrderedProperties() {
    }

    public OrderedProperties(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public Enumeration<Object> keys() {
        return Collections.enumeration(keys);
    }

    @Override
    public Object put(Object key, Object value) {
        keys.add(key);
        return super.put(key, value);
    }

    @Override
    public Set<Object> keySet() {
        return keys;
    }

    @Override
    public Set<String> stringPropertyNames() {
        Set<String> set = new LinkedHashSet<String>();
        for (Object key : this.keys) {
            set.add((String) key);
        }
        return set;
    }
}