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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.LoggerFactory;

public class RequestScopeXmlDSMCache implements XmlDSMCache {
    private static final AdvancedLogger LOGGER = LoggerFactory.getLogger(RequestScopeXmlDSMCache.class,
            "netconf-server-datastore", "DEBUG", "GLOBAL");
    private static final String REQUEST_SCOPE_DSM_CACHE = "RequestScopeXmlDSMCache-";
    private static AtomicInteger c_instanceCounter = new AtomicInteger(0);
    private final String m_instanceId;

    public RequestScopeXmlDSMCache() {
        m_instanceId = REQUEST_SCOPE_DSM_CACHE + getInstanceId();
        LOGGER.debug("Creating RequestScopeXmlDSMCache with instanceId {}", m_instanceId);
    }

    private static String getInstanceId() {
        return String.valueOf(c_instanceCounter.incrementAndGet());
    }

    @Override
    public void putInCache(SchemaPath nodeType, ModelNodeId nodeId, XmlModelNodeImpl node) {
        LOGGER.debug("Cache-instance {}, node added to cache, nodeType {} , nodeId {}", m_instanceId, nodeType, nodeId);
        Map<ModelNodeId, XmlModelNodeImpl> nodesOfType = getNodesOfGivenType(nodeType);
        nodesOfType.put(nodeId, node);
    }

    private Map<ModelNodeId, XmlModelNodeImpl> getNodesOfGivenType(SchemaPath nodeType) {
        Map<SchemaPath, Map<ModelNodeId, XmlModelNodeImpl>> cache = getThreadLocalCache();
        Map<ModelNodeId, XmlModelNodeImpl> nodesOfType = cache.get(nodeType);
        if (nodesOfType == null) {
            nodesOfType = new HashMap<>();
            cache.put(nodeType, nodesOfType);
        }
        return nodesOfType;
    }

    private Map<SchemaPath, Map<ModelNodeId, XmlModelNodeImpl>> getThreadLocalCache() {
        RequestScope currentScope = RequestScope.getCurrentScope();
        Map<SchemaPath, Map<ModelNodeId, XmlModelNodeImpl>> cache =
                (Map<SchemaPath, Map<ModelNodeId, XmlModelNodeImpl>>) currentScope.getFromCache(m_instanceId);
        if (cache == null) {
            cache = new HashMap<>();
            currentScope.putInCache(m_instanceId, cache);
        }
        return cache;
    }

    @Override
    public XmlModelNodeImpl getFromCache(SchemaPath nodeType, ModelNodeId nodeId) {
        XmlModelNodeImpl xmlModelNode = getNodesOfGivenType(nodeType).get(nodeId);
        LOGGER.debug("Cache-instance {}, node being accessed from cache, nodeType {}, nodeId {}, node {}",
                m_instanceId, nodeType, nodeId, xmlModelNode);
        return xmlModelNode;
    }

    @Override
    public void markNodeToBeUpdated(SchemaPath nodeType, ModelNodeId nodeId) {
        XmlModelNodeImpl xmlModelNode = getFromCache(nodeType, nodeId);
        LOGGER.debug("Cache-instance {}, node being marked to be updated from cache, nodeType {}, nodeId {}, node {}",
                m_instanceId, nodeType, nodeId, xmlModelNode);
        if (xmlModelNode != null) {
            xmlModelNode.toBeUpdated();
        }
    }

    @Override
    public List<XmlModelNodeImpl> getNodesToBeUpdated() {
        List<XmlModelNodeImpl> nodesToBeUpdated = new ArrayList<>();
        Map<SchemaPath, Map<ModelNodeId, XmlModelNodeImpl>> cache = getThreadLocalCache();
        for (Map<ModelNodeId, XmlModelNodeImpl> nodesOfType : cache.values()) {
            for (XmlModelNodeImpl node : nodesOfType.values()) {
                if (node.isToBeUpdated()) {
                    nodesToBeUpdated.add(node);
                }
            }
        }
        LOGGER.debug("Cache-instance {}, number of cached nodes to be updated {}", m_instanceId, nodesToBeUpdated
                .size());
        return nodesToBeUpdated;
    }

    @Override
    public void removeFromCache(ModelNodeId nodeId) {
        LOGGER.debug("Cache-instance {}, Trying to remove node from cache, nodeId {}", m_instanceId, nodeId);
        Map<SchemaPath, Map<ModelNodeId, XmlModelNodeImpl>> cache = getThreadLocalCache();

        Iterator<Map.Entry<SchemaPath, Map<ModelNodeId, XmlModelNodeImpl>>> mapIterator = cache.entrySet().iterator();
        while (mapIterator.hasNext()) {
            Map.Entry<SchemaPath, Map<ModelNodeId, XmlModelNodeImpl>> map = mapIterator.next();
            Map<ModelNodeId, XmlModelNodeImpl> modelNodeIdMap = map.getValue();
            Iterator<Map.Entry<ModelNodeId, XmlModelNodeImpl>> modelNodeIdIterator = modelNodeIdMap.entrySet()
                    .iterator();
            while (modelNodeIdIterator.hasNext()) {
                ModelNodeId modelNodeId = modelNodeIdIterator.next().getKey();
                if (modelNodeId.beginsWith(nodeId)) {
                    modelNodeIdIterator.remove();
                    LOGGER.debug("Cache-instance {}, node successfully removed from cache, nodeId {}", m_instanceId,
                            modelNodeId);
                }
            }
        }
    }
}
