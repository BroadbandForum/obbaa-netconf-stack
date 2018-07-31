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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import org.w3c.dom.Element;

public class CopyConfigChangeNotification implements ChangeNotification {

    private String m_target;
    private String m_source;
    private ModelNode m_modelNode;
    private Element m_sourceConfigElement;

    public CopyConfigChangeNotification(String source, String target, Element sourceConfigElement, ModelNode node) {
        m_source = source;
        m_target = target;
        m_modelNode = node;
        m_sourceConfigElement = sourceConfigElement;
    }

    @Override
    public ModelNodeId getModelNodeId() {
        return m_modelNode.getModelNodeId();
    }

    @Override
    public String toString(boolean printChangeSource) {
        return "CopyConfigChangeNotification [m_target=" + m_target + ", m_source=" + m_source + ", m_modelNode=" +
                m_modelNode + "]";
    }

    @Override
    public String toString() {
        return toString(false);
    }

    @Override
    public ChangeType getType() {
        return ChangeType.copyConfig;
    }

    public String getTarget() {
        return m_target;
    }

    public CopyConfigChangeNotification setTarget(String target) {
        m_target = target;
        return this;
    }

    public String getSource() {
        return m_source;
    }

    public Element getSourceConfigElement() {
        return m_sourceConfigElement;
    }

    public CopyConfigChangeNotification setSource(String source) {
        m_source = source;
        return this;
    }

    public ModelNode getModelNode() {
        return m_modelNode;
    }

}
