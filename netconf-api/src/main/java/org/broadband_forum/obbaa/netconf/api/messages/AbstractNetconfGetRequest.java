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

package org.broadband_forum.obbaa.netconf.api.messages;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendaylight.yangtools.yang.common.QName;

import org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams;

public abstract class AbstractNetconfGetRequest extends AbstractNetconfRequest {

    protected int m_depth = NetconfQueryParams.UNBOUNDED;
    protected Map<String, List<QName>> m_fieldValues= new HashMap<>();
 
    public Map<String, List<QName>> getFieldValues() {
        return m_fieldValues;
    }

    public void setFieldValues(Map<String, List<QName>> fieldValues) {
        this.m_fieldValues = fieldValues;
    }

    public int getDepth() {
        return m_depth;
    }

    public void setDepth(int depth) {
        m_depth = depth;
    }

    public abstract NetconfFilter getFilter();

}
