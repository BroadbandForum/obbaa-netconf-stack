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

package org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.addresses;

import java.io.Serializable;

/**
 * Created by keshava on 4/1/16.
 */
public class AddressPK implements Serializable {

    private String addressName;
    private String parentId;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((addressName == null) ? 0 : addressName.hashCode());
        result = prime * result + ((parentId == null) ? 0 : parentId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AddressPK other = (AddressPK) obj;
        if (addressName == null) {
            if (other.addressName != null)
                return false;
        } else if (!addressName.equals(other.addressName))
            return false;
        if (parentId == null) {
            if (other.parentId != null)
                return false;
        } else if (!parentId.equals(other.parentId))
            return false;
        return true;
    }

}
