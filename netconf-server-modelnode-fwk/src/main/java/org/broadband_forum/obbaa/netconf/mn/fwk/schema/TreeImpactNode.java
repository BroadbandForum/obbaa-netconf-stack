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

package org.broadband_forum.obbaa.netconf.mn.fwk.schema;

import java.util.HashSet;
import java.util.Set;

public class TreeImpactNode<T> {

    private T m_data;
    private TreeImpactNode<T> m_parent;
    private Set<TreeImpactNode<T>> m_children = new HashSet<>();

    public TreeImpactNode(T data) {
        m_data = data;
    }

    public Set<TreeImpactNode<T>> getChildren(){
        return m_children;
    }

    public synchronized T getData() {
        return m_data;
    }

    public TreeImpactNode<T> getParent() {
        return m_parent;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_data == null) ? 0 : m_data.hashCode());
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TreeImpactNode<T> other = (TreeImpactNode<T>) obj;
        if (m_data == null) {
            if (other.m_data != null)
                return false;
        } else if (!m_data.equals(other.m_data))
            return false;
        return true;
    }

    public synchronized TreeImpactNode<T> addChild(T child) {
        TreeImpactNode<T> childNode = new TreeImpactNode<T>(child);
        childNode.m_parent = this;
        m_children.add(childNode);
        return childNode;
    }

    public void removeChild(T child){
        TreeImpactNode<T> childNode = new TreeImpactNode<T>(child);
        childNode.m_parent = this;
        m_children.remove(childNode);
    }
}
