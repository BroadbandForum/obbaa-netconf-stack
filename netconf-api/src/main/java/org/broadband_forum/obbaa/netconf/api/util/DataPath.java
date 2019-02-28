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

package org.broadband_forum.obbaa.netconf.api.util;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.opendaylight.yangtools.concepts.Immutable;

import com.google.common.collect.Iterables;

// This is like SchemaPath, but with choice/case not as a separate level
// So this corresponds to a path in an XML request
// Only absolute paths are supported here
public class DataPath implements Immutable, Iterable<DataPathLevel> {
    
    private final List<DataPathLevel> m_levels;
    private final int m_hash;
    
    public static final DataPath ROOT = new DataPath(null, null);
    
    DataPath(DataPath parent, DataPathLevel child){
        if (parent == null) {
            m_levels = new ArrayList<>();
        }
        else {
            m_levels = new ArrayList<>(parent.m_levels);
        }
        if (child != null){
            m_levels.add(child);
        }
        m_hash = computeHashCode();
    }
    
    DataPath(List<DataPathLevel> levels) {
        m_levels = new ArrayList<>(levels);
        m_hash = computeHashCode();
    }
    
    public static DataPath create(Iterable<DataPathLevel> path) {
        return ROOT.createChild(path);
    }

    public static DataPath create(DataPathLevel... path) {
        return create(Arrays.asList(path));
    }
    
    public DataPath createChild(DataPathLevel element) {
        return new DataPath(this, requireNonNull(element));
    }
    
    public DataPath createChild(Iterable<DataPathLevel> relative) {
        if (Iterables.isEmpty(relative)) {
            return this;
        }
        
        DataPath parentPath = this;
        for (DataPathLevel level: relative) {
            parentPath = parentPath.createChild(level);
        }
        return parentPath;
    }
    
    public DataPath getParent() {
        return new DataPath(m_levels.subList(0, m_levels.size() - 1));
    }
    
    public DataPathLevel getLastComponent() {
        return m_levels.get(m_levels.size()- 1);
    }
    
    public List<DataPathLevel> getPath() {
        return Collections.unmodifiableList(m_levels);
    }
    
    @Override
    public Iterator<DataPathLevel> iterator() {
        return Collections.unmodifiableList(m_levels).iterator();
    }

    @Override
    public int hashCode() {
        return m_hash;
    }

    private int computeHashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_levels == null) ? 0 : m_levels.hashCode());
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
        DataPath other = (DataPath) obj;
        if (m_levels == null) {
            if (other.m_levels != null)
                return false;
        } else if (!m_levels.equals(other.m_levels))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "DataPath [m_levels=" + m_levels + "]";
    }
}
