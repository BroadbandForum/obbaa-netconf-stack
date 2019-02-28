package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore;

import org.opendaylight.yangtools.yang.common.QName;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by keshava on 12/8/15.
 */
public class ModelNodeKeyBuilder {
    private Map<QName, String> m_keys = new LinkedHashMap<>();

    public ModelNodeKey build() {
        return new ModelNodeKey(m_keys);
    }

    public ModelNodeKeyBuilder appendKey(QName qName, String value) {
        m_keys.put(qName, value);
        return this;
    }
}
