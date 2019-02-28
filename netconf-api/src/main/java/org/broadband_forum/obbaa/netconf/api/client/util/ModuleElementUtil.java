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

package org.broadband_forum.obbaa.netconf.api.client.util;

import static org.broadband_forum.obbaa.netconf.api.client.util.CommonConstants.DEVIATION;
import static org.broadband_forum.obbaa.netconf.api.client.util.CommonConstants.FEATURE;
import static org.broadband_forum.obbaa.netconf.api.client.util.CommonConstants.IETF_YANG_LIBRARY_NS;
import static org.broadband_forum.obbaa.netconf.api.client.util.CommonConstants.MODULES_STATE;
import static org.broadband_forum.obbaa.netconf.api.client.util.CommonConstants.MODULE_PARAM;
import static org.broadband_forum.obbaa.netconf.api.client.util.CommonConstants.NAMESPACE_PARAM;
import static org.broadband_forum.obbaa.netconf.api.client.util.CommonConstants.NAME_PARAM;
import static org.broadband_forum.obbaa.netconf.api.client.util.CommonConstants.QNAME_YANG_LIBRARY;
import static org.broadband_forum.obbaa.netconf.api.client.util.CommonConstants.REVISION_PARAM;
import static org.broadband_forum.obbaa.netconf.api.client.util.CommonConstants.SUB_TREE;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.api.NetconfCapability;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ModuleElementUtil {
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(ModuleElementUtil.class, LogAppNames.NETCONF_LIB);

    public static GetRequest getYangLibraryRequest() {
        GetRequest request = new GetRequest();
        NetconfFilter requestFilter = new NetconfFilter();
        Document document = DocumentUtils.createDocument();
        Element moduleStateElement = document.createElementNS(IETF_YANG_LIBRARY_NS, MODULES_STATE);
        document.appendChild(moduleStateElement);
        requestFilter.setType(SUB_TREE);
        Element filterElement = document.getDocumentElement();
        requestFilter.addXmlFilter(filterElement);
        request.setFilter(requestFilter);
        request.setMessageId("1");
        request.setReplyTimeout(10000);
        return request;
    }

    public static List<NetconfCapability> fetchModulesFeaturesDeviationsFromResponse(Element responseElement) {
        List<NetconfCapability> netconfCapabilities = new ArrayList<>();
        NodeList baseList = responseElement.getChildNodes();
        for (int i = 0; i < baseList.getLength(); i++) {
            Node node = baseList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) node;
                String nodeName = childElement.getLocalName();
                QName qname = QName.create(childElement.getNamespaceURI(), nodeName);
                if (qname.equals(QNAME_YANG_LIBRARY)) {
                    fillModuleFeatureDeviationQNamesOfModuleElement(IETF_YANG_LIBRARY_NS, childElement, netconfCapabilities);
                }
            }
        }
        return netconfCapabilities;
    }

    private static void fillModuleFeatureDeviationQNamesOfModuleElement(String namespace, Element moduleEle, List<NetconfCapability> netconfCapabilities) {
        String nodeName = moduleEle.getLocalName();
        QName moduleElementQName = QName.create(moduleEle.getNamespaceURI(), nodeName);
        QName moduleParamQName = QName.create(namespace, MODULE_PARAM);
        QName nameParamQName = QName.create(namespace, NAME_PARAM);
        QName revisionParamQName = QName.create(namespace, REVISION_PARAM);
        QName namespaceParamQName = QName.create(namespace, NAMESPACE_PARAM);
        QName featureParamQName = QName.create(namespace, FEATURE);
        QName deviationParamQName = QName.create(namespace, DEVIATION);
        QName deviation;

        if (moduleElementQName.equals(moduleParamQName)) {
            NodeList list = moduleEle.getChildNodes();
            String moduleName = null;
            String revision = null;
            String feature = null;
            String deviationText = null;

            for (int index = 0; index < list.getLength(); index++) {
                Node innerNode = list.item(index);
                if (innerNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element innerNodeEle = (Element) innerNode;
                    String innerNodeName = innerNodeEle.getLocalName();
                    QName attributeQName = QName.create(innerNodeEle.getNamespaceURI(), innerNodeName);
                    if (attributeQName.equals(nameParamQName)) {
                        moduleName = innerNodeEle.getTextContent().trim();
                    } else if (attributeQName.equals(revisionParamQName)) {
                        revision = innerNodeEle.getTextContent().trim();
                    } else if (attributeQName.equals(namespaceParamQName)) {
                        namespace = innerNodeEle.getTextContent().trim();
                    } else if (attributeQName.equals(featureParamQName)) {
                        if (feature == null) {
                            feature = innerNodeEle.getTextContent().trim();
                        } else {
                            feature = feature + "," + innerNodeEle.getTextContent().trim();
                        }
                    } else if (attributeQName.equals(deviationParamQName)) {
                        Revision deviationRevision = revision == null || revision.isEmpty() ? null : Revision.of(revision);
                        deviation = getDeviationFromModuleElement(innerNodeEle, namespace, deviationRevision);
                        if (deviationText == null) {
                            deviationText = deviation.getLocalName();
                        } else {
                            deviationText = deviationText + "," + deviation.getLocalName();
                        }
                    }
                }
            }

            if (moduleName != null && namespace != null) {
                if (revision.equals("")) {
                    revision = null;
                }
                if(feature != null || deviationText != null) {
                    netconfCapabilities.add(new NetconfCapability(namespace, moduleName, revision, feature, deviationText));
                }
                netconfCapabilities.add(new NetconfCapability(namespace, moduleName, revision));
            }
        }
    }

    private static QName getDeviationFromModuleElement(Element innerNodeEle, String namespace, Revision revision) {
        NodeList deviationElementList = innerNodeEle.getChildNodes();
        QName nameQName = QName.create(IETF_YANG_LIBRARY_NS, NAME_PARAM);
        String deviationModuleName = null;
        for (int index = 0; index < deviationElementList.getLength(); index++) {
            Node node = deviationElementList.item(index);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                String localName = element.getLocalName();
                QName attrQName = QName.create(element.getNamespaceURI(), localName);
                if (attrQName.equals(nameQName)) {
                    deviationModuleName = element.getTextContent().trim();
                }
            }
        }
        QNameModule qNameModule = formQNameModuleFromNsAndRevision(namespace, revision);
        QName moduleQName = QName.create(qNameModule, deviationModuleName);
        return moduleQName;

    }

    private static QNameModule formQNameModuleFromNsAndRevision(String namespace, Revision revision) {
        URI namespaceUri = null;
        try {
            namespaceUri = new URI(namespace);
        } catch (URISyntaxException e) {
            LOGGER.error("Error while parsing module namespace {} and revision {}" + namespace + revision, e);
        }
        QNameModule qNameModule = QNameModule.create(namespaceUri, revision);
        return qNameModule;
    }

}
