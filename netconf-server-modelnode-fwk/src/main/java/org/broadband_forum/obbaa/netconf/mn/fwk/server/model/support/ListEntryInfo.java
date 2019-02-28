package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class ListEntryInfo implements Serializable {
    private Map<ListEntryQName, String> keys = new LinkedHashMap<>();

    public Map<ListEntryQName, String> getKeys() {
        return keys;
    }

    public void setKeys(Map<ListEntryQName, String> keys) {
        this.keys = keys;
    }
}
