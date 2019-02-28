package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import java.io.Serializable;

public class ListEntryQName implements Serializable {
    private String keyModuleName;
    private String keyLocalName;

    public ListEntryQName() {
    }

    public ListEntryQName(String keyModuleName, String keyLocalName) {
        this.keyModuleName = keyModuleName;
        this.keyLocalName = keyLocalName;
    }

    public String getKeyModuleName() {
        return keyModuleName;
    }

    public String getKeyLocalName() {
        return keyLocalName;
    }

    public void setKeyModuleName(String keyModuleName) {
        this.keyModuleName = keyModuleName;
    }

    public void setKeyLocalName(String keyLocalName) {
        this.keyLocalName = keyLocalName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ListEntryQName that = (ListEntryQName) o;

        if (keyModuleName != null ? !keyModuleName.equals(that.keyModuleName) : that.keyModuleName != null)
            return false;
        return keyLocalName != null ? keyLocalName.equals(that.keyLocalName) : that.keyLocalName == null;
    }

    @Override
    public int hashCode() {
        int result = keyModuleName != null ? keyModuleName.hashCode() : 0;
        result = 31 * result + (keyLocalName != null ? keyLocalName.hashCode() : 0);
        return result;
    }
}
