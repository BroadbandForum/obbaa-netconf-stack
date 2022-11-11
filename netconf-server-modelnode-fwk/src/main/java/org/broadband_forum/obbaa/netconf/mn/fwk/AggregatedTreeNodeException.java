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

package org.broadband_forum.obbaa.netconf.mn.fwk;

import java.util.Map;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;

public class AggregatedTreeNodeException extends IllegalArgumentException{

    private static final long serialVersionUID = 1L;
    private String m_errorPath;
    private Map<String, String> m_errorPathNsByPrefix;
    
    public AggregatedTreeNodeException(String message, ModelNodeId modelNodeId, SchemaRegistry schemaRegistry) {
        super(message);
        m_errorPath = modelNodeId.xPathString(schemaRegistry);
        m_errorPathNsByPrefix = modelNodeId.xPathStringNsByPrefix(schemaRegistry);
    }

    public String getErrorPath() {
        return m_errorPath;
    }

    public Map<String, String> getErrorPathNsByPrefix() {
        return m_errorPathNsByPrefix;
    }
    
}
