package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore;

import org.opendaylight.yangtools.yang.common.QName;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class ModelNodeKey {
    public static final ModelNodeKey EMPTY_KEY = new ModelNodeKey(Collections.<QName, String>emptyMap());
    private final Map<QName, String> m_keys;

    public ModelNodeKey(Map<QName, String> keys) {
        m_keys = keys;
    }

    public Map<QName, String> getKeys() {
        return m_keys;
    }

    public boolean isEmpty() {
        return m_keys.isEmpty();
    }

    public Set<Map.Entry<QName, String>> entrySet() {
        return m_keys.entrySet();
    }

    @Override
    public String toString() {
        return "ModelNodeKey{" +
                "m_keys=" + m_keys +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ModelNodeKey that = (ModelNodeKey) o;

        return m_keys != null ? m_keys.equals(that.m_keys) : that.m_keys == null;

    }

    @Override
    public int hashCode() {
        return m_keys != null ? m_keys.hashCode() : 0;
    }
}
