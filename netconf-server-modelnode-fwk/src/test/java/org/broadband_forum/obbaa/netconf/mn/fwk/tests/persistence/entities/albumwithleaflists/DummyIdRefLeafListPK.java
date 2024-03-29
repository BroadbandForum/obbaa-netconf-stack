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

package org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.albumwithleaflists;

import java.io.Serializable;

public class DummyIdRefLeafListPK implements Serializable {

	private static final long serialVersionUID = -1590041170827389396L;
	String dummyLeafListIdRef;
	String parentId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DummyIdRefLeafListPK that = (DummyIdRefLeafListPK) o;

        if (parentId != null ? !parentId.equals(that.parentId) : that.parentId != null) return false;
        return dummyLeafListIdRef != null ? dummyLeafListIdRef.equals(that.dummyLeafListIdRef) : that.dummyLeafListIdRef == null;

    }

    @Override
    public int hashCode() {
        int result = parentId != null ? parentId.hashCode() : 0;
        result = 31 * result + (dummyLeafListIdRef != null ? dummyLeafListIdRef.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "dummyLeafListIdRefPK{" +
                "dummyLeafListIdRef='" + dummyLeafListIdRef + '\'' +
                ", parentId='" + parentId + '\'' +
                '}';
    }
}
