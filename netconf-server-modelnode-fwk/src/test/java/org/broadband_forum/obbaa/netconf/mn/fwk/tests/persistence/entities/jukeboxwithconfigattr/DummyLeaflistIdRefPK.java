/*
 * Copyright 2018 Broadband Forum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
