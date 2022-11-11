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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.dom;

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.dom.EncryptDecryptUtil.getChildrenOfType;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.EditTreeBuilder.getQName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.common.base.Stopwatch;

public abstract class SchemaAwareDOMVisitor {
    protected static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(SchemaAwareDOMVisitor.class, LogAppNames.NETCONF_STACK);

    private final Element m_root;
    protected final SchemaRegistry m_schemaRegistry;
    private final DataSchemaNode m_rootSN;

    public SchemaAwareDOMVisitor(Element root, SchemaRegistry schemaRegistry) {
        m_root = root;
        m_schemaRegistry = schemaRegistry;
        m_rootSN = findRootSN(root);
    }

    public SchemaAwareDOMVisitor(Element root, SchemaRegistry schemaRegistry, DataSchemaNode rootSchemaNode) {
    	 m_root = root;
         m_schemaRegistry = schemaRegistry;
         m_rootSN = rootSchemaNode;
	}



	public abstract void visit(DataSchemaNode schemaNode, List<Element> elementOfType);

    public Element traverse() {
        Stopwatch stopWatch = Stopwatch.createStarted();
        visitInternal(m_root, m_rootSN);
        stopWatch.stop();
        LOGGER.debug("Time taken to traverse dom is" + stopWatch.elapsed(TimeUnit.MILLISECONDS));
        return m_root;
    }

    private void visitInternal(Element subtreeRoot, DataSchemaNode subtreeRootSN) {
        visit(subtreeRootSN, Arrays.asList(subtreeRoot));

        Map<DataSchemaNode, List<Element>> childrenOfType = getChildren(subtreeRoot, subtreeRootSN);
        childrenOfType.forEach((sn, elementList) -> visit(sn, elementList));

        childrenOfType.forEach((sn, elementList) -> elementList.forEach(childOfType -> {
            List<Element> childNodes = DocumentUtils.getChildElements(childOfType);
            childNodes.forEach(child -> {
                DataSchemaNode childSN = m_schemaRegistry.getNonChoiceChild(sn.getPath(),getQName(m_schemaRegistry,child));
                visitInternal(child,childSN);
            });
        }));
    }

    private Map<DataSchemaNode, List<Element>> getChildren(Element currentNode, DataSchemaNode currentSN) {
        Map<DataSchemaNode, List<Element>> result = new HashMap<>();

        Map<QName, List<Element>> qNameToChildMap = getChildrenOfType(currentNode);
        Collection<DataSchemaNode> childSchemaNodes = m_schemaRegistry.getNonChoiceChildren(currentSN.getPath());
        qNameToChildMap
                .forEach((qname, childNodesOfType) -> {
                    DataSchemaNode childSN = getChildSN(qname, childSchemaNodes);
                    if (childSN != null) {
                        List<Element> existingList = result.get(childSN);
                        if (existingList == null) {
                            existingList = new ArrayList<>();
                            result.put(childSN, existingList);
                        }
                        existingList.addAll(childNodesOfType);
                    }
                });
        return result;
    }

    private DataSchemaNode getChildSN(QName childQName, Collection<DataSchemaNode> childSchemaNodes) {
        return childSchemaNodes.stream().filter(sn -> nsLocalNameMatch(childQName, sn)).findFirst().orElse(null);
    }

    private DataSchemaNode findRootSN(Node rootNode) {
        return findSN(m_schemaRegistry.getRootDataSchemaNodes(), (Element) rootNode);
    }

    private boolean nsLocalNameMatch(QName qName, DataSchemaNode schemaNode) {
        return schemaNode.getQName().getNamespace().toString().equals(qName.getNamespace().toString()) &&
                schemaNode.getQName().getLocalName().equals(qName.getLocalName());
    }

    private DataSchemaNode findSN(Collection<DataSchemaNode> childSchemaNodes, Element childNode) {
        QName childQName = getQName(m_schemaRegistry, childNode);
        return childSchemaNodes
                .stream()
                .filter(sn -> nsLocalNameMatch(childQName, sn))
                .findFirst().orElse(null);
    }
}
