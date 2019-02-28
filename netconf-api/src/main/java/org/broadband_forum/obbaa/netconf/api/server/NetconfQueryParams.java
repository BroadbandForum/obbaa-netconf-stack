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

package org.broadband_forum.obbaa.netconf.api.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendaylight.yangtools.yang.common.QName;


public class NetconfQueryParams {

    public static int UNBOUNDED = -1;

    public static final NetconfQueryParams NO_PARAMS = new NetconfQueryParams(UNBOUNDED, true);

    private int m_depth;
    private boolean m_includeConfig;
    private Map<String, List<QName>> m_fieldValues= new HashMap<>();
 
    public NetconfQueryParams(int depth, boolean includeConfig) {
        m_depth = depth;
        m_includeConfig = includeConfig;
       
    }
    
    public NetconfQueryParams(int depth, boolean includeConfig, Map<String, List<QName>> fieldValues) {
        m_depth = depth;
        m_includeConfig = includeConfig;
        m_fieldValues = fieldValues;
    }

    public int getDepth() {
        return m_depth;
    }

    public boolean isIncludeConfig() {
        return m_includeConfig;
    }
    
    public Map<String, List<QName>> getFields(){
        return m_fieldValues;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + m_depth;
        result = prime * result + (m_includeConfig ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        NetconfQueryParams other = (NetconfQueryParams) obj;
        if (m_depth != other.m_depth) {
            return false;
        }
        if (m_includeConfig != other.m_includeConfig) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "NetconfQueryParams [m_depth=" + m_depth + ", m_includeConfig=" + m_includeConfig + "]";
    }

}
