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

import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaMountRegistryProvider;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;

public class MountProviderInfo {

    private SchemaMountRegistryProvider m_provder = null;
    private Node m_mountedXmlNodeFromRequest = null;
    private DataSchemaNode m_mountedDataSchemaNode = null;
    private SchemaRegistry m_mountedRegistry = null;
    
    public MountProviderInfo(SchemaMountRegistryProvider provder, Element mountedXmlNodeFromRequest, DataSchemaNode mountedDataSchemaNode) {
        m_provder = provder;
        m_mountedXmlNodeFromRequest = mountedXmlNodeFromRequest;
        m_mountedDataSchemaNode = mountedDataSchemaNode;
        m_mountedRegistry = provder.getSchemaRegistry(mountedXmlNodeFromRequest);;
    }

    public SchemaMountRegistryProvider getProvder() {
        return m_provder;
    }

    public Node getMountedXmlNodeFromRequest() {
        return m_mountedXmlNodeFromRequest;
    }

    public DataSchemaNode getMountedDataSchemaNode() {
        return m_mountedDataSchemaNode;
    }
    
    public SchemaRegistry getMountedRegistry(){
        return m_mountedRegistry;
    }
    
}
