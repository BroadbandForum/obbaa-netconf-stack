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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn;

import java.util.HashMap;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;

public class NodeIndex {
    private Map<String, ConfigLeafAttribute> m_attrIndex = new HashMap<>();
    private Map<String, String> m_attrToTypeIndex = new HashMap<>();
    private String m_nodeIndex;

    public void addAttrIndex(String attrXPath, ConfigLeafAttribute value) {
        m_attrIndex.put(attrXPath, value);
    }

    public void addAttrTypeIndex(String attrXPath, String attrTypeXPath) {
        m_attrToTypeIndex.put(attrXPath, attrTypeXPath);
    }

    public void addNodeIndex(String nodeIndex) {
        m_nodeIndex = nodeIndex;
    }

    public void clearAttrIndex() {
        m_attrIndex.clear();
        m_attrToTypeIndex.clear();
    }

    public Map<String, ConfigLeafAttribute> getAttrIndex() {
        return m_attrIndex;
    }

    public Map<String, String> getAttrToAttrTypeIndex() {
        return m_attrToTypeIndex;
    }
}
