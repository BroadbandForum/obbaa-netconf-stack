package org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.jukeboxwithconfigattr;

import java.io.Serializable;

/**
 * Created by sgs on 3/6/17.
 */
public class DummyLeaflistIdRefPK implements Serializable {

    String dummyLeaflistIdRef;
    String dummyLeaflistIdRefNs;
    String parentId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DummyLeaflistIdRefPK that = (DummyLeaflistIdRefPK) o;

        if (!dummyLeaflistIdRef.equals(that.dummyLeaflistIdRef)) return false;
        if (!dummyLeaflistIdRefNs.equals(that.dummyLeaflistIdRefNs)) return false;
        return parentId.equals(that.parentId);

    }

    @Override
    public int hashCode() {
        int result = dummyLeaflistIdRef.hashCode();
        result = 31 * result + dummyLeaflistIdRefNs.hashCode();
        result = 31 * result + parentId.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "DummyLeaflistIdRefPK{" +
                "dummyLeaflistIdRef='" + dummyLeaflistIdRef + '\'' +
                ", dummyLeaflistIdRefNsNs='" + dummyLeaflistIdRefNs + '\'' +
                ", parentId='" + parentId + '\'' +
                '}';
    }
}
