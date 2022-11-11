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

package org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util;

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil.excludeFirstStep;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.jxpath.ri.compiler.Expression;
import org.apache.commons.jxpath.ri.compiler.LocationPath;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.Step;
import org.broadband_forum.obbaa.netconf.api.parser.FileYangSource;
import org.broadband_forum.obbaa.netconf.api.parser.YangParserUtil;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.ModuleIdentifier;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaMountRegistryProvider;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.ValidationHint;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.AnvExtensions;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.MountProviderInfo;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.SchemaContextUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.HelperDrivenModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeGetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DSValidationMountContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationPathBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.jxpath.JXPathUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.ReadWriteLockService;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.dom4j.dom.DOMNodeHelper;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ElementCountConstraint;
import org.opendaylight.yangtools.yang.model.api.ElementCountConstraintAware;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.MustConstraintAware;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.model.api.WhenConditionAware;
import org.opendaylight.yangtools.yang.model.api.meta.ModelStatement;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SchemaRegistryUtil {
    private static final String EQUAL_TO = "=";
    private static final String EQUAL_WITH_SPACES = " = ";
    private static final String SLASH = "/";
    public static final String YANG_EXTENSION = ".yang";
    private static final String COLON = ":";
    public static final String SINGLE_QUOTE = "'";
    public static final String APOSTROPHE_XML_ENTITY = "&apos;";
    public static final String SINGLE_QUOTE_XML_ENTITY = "&quot;";

    public static final String MOUNT_CONTEXT_SUBSYSTEM_REGISTRY = "MOUNT_CONTEXT_SUBSYSTEM_REGISTRY";
    public static final String MOUNT_CONTEXT_PROVIDER = "MOUNT_CONTEXT_PROVIDER";
    public static final String MOUNT_PATH = "MOUNT_PATH";
    public static final String WILDCARD = "*";
    public static final String ARROW_OPERATOR = "->";
    private static final String VALIDATION_HINTS_REGEX_PATTERN = "([^\\s]+(\\s)*->(\\s)*SKIP_IMPACT_ON_CREATE|[^\\s]+(\\s)*->(\\s)*SKIP_VALIDATION|[^\\s]+(\\s)*->(\\s)*SKIP_IMPACT_VALIDATION)+";

    public static QName getChildQname(Node dataNode, DataSchemaNode schemaNode, SchemaRegistry schemaRegistry) {
        DataSchemaNode childSchemaNode = getChildSchemaNode(dataNode, schemaNode.getPath(), schemaRegistry);
        if (childSchemaNode != null) {
            return childSchemaNode.getQName();
        }

        return null;
    }

    public static DataSchemaNode getChildSchemaNode(Node dataNode, SchemaPath path, SchemaRegistry schemaRegistry) {
        return getChildSchemaNode(dataNode.getNamespaceURI(), dataNode.getLocalName(), path, schemaRegistry);
    }

    public static NotificationDefinition getChildNotificationDefinition(String namespaceURI, String localName, SchemaPath path, SchemaRegistry schemaRegistry) {
        Set<NotificationDefinition> notifications = schemaRegistry.retrieveAllNotificationDefinitions();
        for (NotificationDefinition notification : notifications) {
            QName childQname = notification.getQName();
            if (localName.equals(childQname.getLocalName()) && namespaceURI.equals(childQname.getNamespace().toString()) && path.equals(notification.getPath().getParent())) {
                return notification;
            }
        }
        return null;
    }

    public static DataSchemaNode getChildSchemaNode(String childNs, String childLocalName, SchemaPath path, SchemaRegistry schemaRegistry) {
        Collection<DataSchemaNode> children = ChoiceCaseNodeUtil.getChildrenUnderChoice(schemaRegistry, path);
        if(SchemaPath.ROOT.equals(path)){
            children = schemaRegistry.getRootDataSchemaNodes();
        }
        for (DataSchemaNode childSchemaNode : children) {
            QName childQname = childSchemaNode.getQName();
            if (childLocalName.equals(childQname.getLocalName()) && childNs.equals(childQname.getNamespace().toString())) {
                return childSchemaNode;
            }
        }

        return null;
    }

    public static SchemaPath getSchemaPath(SchemaRegistry schemaRegistry, String xPath, String defaultNs, String moduleName) {
        if (xPath.contains(DataStoreValidationUtil.CURRENT_PATTERN)) {
            return null;
        }
        SchemaPath schemaPath = null;
        String[] split = xPath.split(SLASH);
        Collection<QName> qnames = new LinkedList<QName>();
        for (String path : split) {
            QName qName = null;
            String[] qnameValue = path.split(DataStoreValidationUtil.COLON);
            if (path.contains(DataStoreValidationUtil.COLON)) {
                String namespaceURI = getNamespaceFromModule(schemaRegistry, moduleName, qnameValue[0]);
                qName = schemaRegistry.lookupQName(namespaceURI, qnameValue[1]);
            } else if (!path.isEmpty()) {
                qName = schemaRegistry.lookupQName(defaultNs, path);
            }
            if (qName != null) {
                qnames.add(qName);
            }
        }
        if (qnames.size() > 0) {
            schemaPath = SchemaPath.create(qnames, true);
        }
        return schemaPath;
    }

    public static String getAbsolutePath(Node element, DataSchemaNode schemaNode, SchemaRegistry schemaRegistry) {
        if (schemaNode instanceof LeafListSchemaNode || schemaNode instanceof LeafSchemaNode) {
            String childName = schemaNode.getQName().getLocalName();
            DataSchemaNode parentSchemaNode = schemaRegistry.getNonChoiceParent(schemaNode.getPath());
            return getPathWithPrefix(element.getParentNode(), parentSchemaNode, schemaRegistry, childName);
        } else {
            return getPathWithPrefix(element, schemaNode, schemaRegistry, null);
        }
    }

    public static DataSchemaNode getSchemaNodeForXpath(SchemaRegistry registry, SchemaPath contextSchemaPath,LocationPath path){
        Collection<DataSchemaNode> children = registry.getChildren(contextSchemaPath);
        Step[] steps = path.getSteps();
        for(DataSchemaNode child : children) {
            if(steps[0] != null) {
                NodeNameTest node = (NodeNameTest) steps[0].getNodeTest();
                String stepName = node.getNodeName().getName();
                if(child.getQName().getLocalName().equals(stepName)){
                    if(steps.length == 1) {
                        return child;
                    }
                    path = excludeFirstStep(path);
                    return getSchemaNodeForXpath(registry, child.getPath(), path);
                }
            }
        }
        return null;        
    }

    public static String getPathWithPrefix(Node element, DataSchemaNode schemaNode, SchemaRegistry schemaRegistry, String childName) {
        if (schemaNode == null) {
            return "";
        }
        String prefix = getPrefix(element, schemaNode, schemaRegistry);
        StringBuilder errorPath = new StringBuilder();
        errorPath.append(buildPathWithPrefix(element, schemaNode, schemaRegistry));

        DataSchemaNode parentSchemaNode = schemaRegistry.getNonChoiceParent(schemaNode.getPath());
        Node parentNode = element.getParentNode();
        while (parentSchemaNode != null) {
            String parentErrorItem = buildPathWithPrefix(parentNode, parentSchemaNode, schemaRegistry);
            errorPath.insert(0, parentErrorItem);
            parentSchemaNode = schemaRegistry.getNonChoiceParent(parentSchemaNode.getPath());
            parentNode = parentNode.getParentNode();
        }
        if (childName != null && !childName.isEmpty()) {
            errorPath.append(SLASH).append(prefix + COLON).append(childName);
        }

        return errorPath.toString();
    }

    private static String getPrefix(Node element, DataSchemaNode dataSchemaNode, SchemaRegistry schemaRegistry) {
        String prefix = element.getPrefix();
        if (prefix == null) {
            prefix = schemaRegistry.getPrefix(dataSchemaNode.getQName().getNamespace().toString());
        }
        return prefix;
    }

    private static String buildPathWithPrefix(Node element, DataSchemaNode dataSchemaNode, SchemaRegistry schemaRegistry) {
        StringBuilder errorItem = new StringBuilder();
        String prefix = getPrefix(element, dataSchemaNode, schemaRegistry);
        if (dataSchemaNode instanceof ListSchemaNode) {
            errorItem.append(SLASH).append(prefix + COLON).append(element.getLocalName());
            List<QName> keys = ((ListSchemaNode) dataSchemaNode).getKeyDefinition();
            NodeList childNodes = element.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node child = childNodes.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    DataSchemaNode childSchemaNode = getChildSchemaNode(child, dataSchemaNode.getPath(), schemaRegistry);
                    if (childSchemaNode != null) {
                        QName childQName = childSchemaNode.getQName();
                        if (keys.contains(childQName)) {
                            String keyValue = child.getTextContent().replace(APOSTROPHE_XML_ENTITY, SINGLE_QUOTE).replace(SINGLE_QUOTE_XML_ENTITY, SINGLE_QUOTE);
                            String childPrefix = getPrefix(child, childSchemaNode, schemaRegistry);
                            if (!keyValue.startsWith(SINGLE_QUOTE) && !keyValue.endsWith(SINGLE_QUOTE)) {
                                errorItem.append("[").append(childPrefix + COLON).append(child.getLocalName()).append(EQUAL_WITH_SPACES).append(SINGLE_QUOTE).append(keyValue).append(SINGLE_QUOTE).append("]");
                            } else {
                                errorItem.append("[").append(childPrefix + COLON).append(child.getLocalName()).append(EQUAL_WITH_SPACES).append(keyValue).append("]");
                            }
                        }
                    }
                }
            }
        } else if (dataSchemaNode instanceof ContainerSchemaNode) {
            errorItem.append(SLASH).append(prefix + COLON).append(element.getLocalName());
        }

        return errorItem.toString();
    }

    public static Pair<String, Map<String, String>> getErrorPath(Node element, DataSchemaNode schemaNode, SchemaRegistry schemaRegistry, Element childElement) {
        Map<String, String> prefixToNSMap = new HashMap<>();
        StringBuilder errorPath = new StringBuilder();
        if (schemaNode != null) {
            errorPath.append(buildErrorPath(element, schemaNode, schemaRegistry, prefixToNSMap));

            DataSchemaNode parentSchemaNode = schemaRegistry.getNonChoiceParent(schemaNode.getPath());
            Node parentNode = element.getParentNode();
            while (parentSchemaNode != null && parentNode != null) {
                String parentErrorItem = buildErrorPath(parentNode, parentSchemaNode, schemaRegistry, prefixToNSMap);
                errorPath.insert(0, parentErrorItem);
                parentNode = parentNode.getParentNode();
                parentSchemaNode = schemaRegistry.getNonChoiceParent(parentSchemaNode.getPath());
                if(parentSchemaNode == null && parentNode != null && schemaRegistry.getParentRegistry() != null) {
                    parentSchemaNode =  schemaRegistry.getParentRegistry().getDataSchemaNode(schemaRegistry.getMountPath());
                    schemaRegistry = schemaRegistry.getParentRegistry();
                }
            }
        }

        if (childElement != null) {
            String childNodeName = fillPrefixToNSMapAndReturnNodeName((Element)childElement, schemaRegistry, prefixToNSMap);
            errorPath.append(SLASH).append(childNodeName);
        }

        return new Pair<String, Map<String, String>>(errorPath.toString(), prefixToNSMap);
    }

    public static Pair<String, Map<String, String>> getErrorPath(String errorPath, Node element, DataSchemaNode schemaNode, SchemaRegistry schemaRegistry) {
        Map<String, String> prefixToNSMap = new HashMap<>();
        return getErrorPath(new Pair<String, Map<String, String>>(errorPath, prefixToNSMap), element, schemaNode, schemaRegistry);
    }

    public static Pair<String, Map<String, String>> getErrorPath(Pair<String, Map<String, String>> existingErrorPathPair, Node element, DataSchemaNode schemaNode, SchemaRegistry schemaRegistry) {
        StringBuilder errorPathSB = new StringBuilder(existingErrorPathPair.getFirst());
        if (schemaNode != null) {
            errorPathSB.append(buildErrorPath(element, schemaNode, schemaRegistry, existingErrorPathPair.getSecond()));
        }

        return new Pair<String, Map<String, String>>(errorPathSB.toString(), existingErrorPathPair.getSecond());
    }

    private static String buildErrorPath(Node element, DataSchemaNode dataSchemaNode, SchemaRegistry schemaRegistry, Map<String, String> prefixToNSMap) {
        StringBuilder errorItem = new StringBuilder();
        String elementName = dataSchemaNode.getQName().getLocalName();
        if (element != null && elementName.equals(element.getLocalName())) {
            elementName = fillPrefixToNSMapAndReturnNodeName((Element)element, schemaRegistry, prefixToNSMap);
        } else {
            String ns = dataSchemaNode.getQName().getNamespace().toString();
            elementName = fillPrefixToNsMapAndReturnNodeName(schemaRegistry, prefixToNSMap, elementName, ns);
        }
        errorItem.append(SLASH).append(elementName);
        if (dataSchemaNode instanceof ListSchemaNode) {
            List<QName> keys = ((ListSchemaNode) dataSchemaNode).getKeyDefinition();
            NodeList childNodes = (element == null ? DOMNodeHelper.EMPTY_NODE_LIST : element.getChildNodes());
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node child = childNodes.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    DataSchemaNode childSchemaNode = getChildSchemaNode(child, dataSchemaNode.getPath(), schemaRegistry);
                    if (childSchemaNode != null) {
                        QName childQName = childSchemaNode.getQName();
                        if (keys.contains(childQName)) {
                            String keyValue = child.getTextContent().replace(APOSTROPHE_XML_ENTITY, SINGLE_QUOTE).replace(SINGLE_QUOTE_XML_ENTITY, SINGLE_QUOTE);
                            String childNodeName = fillPrefixToNSMapAndReturnNodeName((Element) child, schemaRegistry, prefixToNSMap);
                            if (!keyValue.startsWith(SINGLE_QUOTE) && !keyValue.endsWith(SINGLE_QUOTE)) {
                                errorItem.append("[").append(childNodeName).append(EQUAL_TO).append(SINGLE_QUOTE).append(keyValue).append(SINGLE_QUOTE).append("]");
                            } else {
                                errorItem.append("[").append(childNodeName).append(EQUAL_TO).append(keyValue).append("]");
                            }
                        }
                    }
                }
            }
        }
        return errorItem.toString();
    }

    private static void fillPrefixToNSMap(String prefix, String ns,  Map<String, String> prefixToNSMap) {
        if(prefixToNSMap.get(prefix) == null){
            prefixToNSMap.put(prefix, ns);
        }
    }

    private static String fillPrefixToNSMapAndReturnNodeName(Element element, SchemaRegistry schemaRegistry, Map<String, String> prefixToNSMap){
        String elementName = element.getNodeName();
        String[] splits = elementName.split(COLON);

        if(splits.length == 2){
            String prefix = elementName.split(COLON)[0];
            String ns = element.lookupNamespaceURI(prefix);
            fillPrefixToNSMap(prefix, ns, prefixToNSMap);
        } else {
            String ns = element.getNamespaceURI();
            elementName = fillPrefixToNsMapAndReturnNodeName(schemaRegistry, prefixToNSMap, elementName, ns);
        }
        return elementName;
    }

    private static String fillPrefixToNsMapAndReturnNodeName(SchemaRegistry schemaRegistry,
            Map<String, String> prefixToNSMap, String elementName, String ns) {
        String prefix = schemaRegistry.getPrefix(ns);
        if(prefix != null && ns != null) {
            elementName = prefix + COLON +elementName;
            fillPrefixToNSMap(prefix, ns, prefixToNSMap);
        }
        return elementName;
    }

    /**
     * Returns all the children of type ContainerSchemaNode and are not configuration nodes.
     *
     * @param modelNode
     * @param nodeSchemaPath
     * @return
     */
    public static List<QName> getStateChildContainers(HelperDrivenModelNode modelNode, SchemaPath nodeSchemaPath) {
        SchemaRegistry schemaRegistry = modelNode.getSchemaRegistryForParent();
        List<QName> stateContainerChildren = new ArrayList<>();
        Set<DataSchemaNode> childNodes = new HashSet<>();
        DataSchemaNode node = schemaRegistry.getDataSchemaNode(nodeSchemaPath);
        if (node instanceof DataNodeContainer) {
            DataNodeContainer container = (DataNodeContainer) node;
            childNodes.addAll(container.getChildNodes());
            childNodes.addAll(getMountPointChildNodes(modelNode.getSchemaRegistry(), nodeSchemaPath, node));
            for (DataSchemaNode child : childNodes) {
                if (child instanceof ContainerSchemaNode && !child.isConfiguration()) {
                    stateContainerChildren.add(child.getQName());
                }
                if (child instanceof ChoiceSchemaNode) {
                    stateContainerChildren.addAll(getStateChildCases(schemaRegistry, (ChoiceSchemaNode) child));
                }
            }
        }
        return stateContainerChildren;
    }

    /**
     * Returns all the children of type ListSchemaNode and are not configuration nodes.
     *
     * @param modelNode
     * @param nodeSchemaPath
     * @return
     */
    public static List<QName> getStateChildLists(HelperDrivenModelNode modelNode, SchemaPath nodeSchemaPath) {
        SchemaRegistry schemaRegistry = modelNode.getSchemaRegistryForParent();
        List<QName> stateListChildren = new ArrayList<>();
        Set<DataSchemaNode> childNodes = new HashSet<>();
        DataSchemaNode node = schemaRegistry.getDataSchemaNode(nodeSchemaPath);
        if (node instanceof DataNodeContainer) {
            DataNodeContainer container = (DataNodeContainer) node;
            childNodes.addAll(container.getChildNodes());
            childNodes.addAll(getMountPointChildNodes(modelNode.getSchemaRegistry(), nodeSchemaPath, node));
            for (DataSchemaNode child : childNodes) {
                if (child instanceof ListSchemaNode && !child.isConfiguration()) {
                    stateListChildren.add(child.getQName());
                }
            }
        }
        return stateListChildren;
    }

    private static Collection<DataSchemaNode> getMountPointChildNodes(SchemaRegistry mountRegistry, SchemaPath nodeSchemaPath, DataSchemaNode node) {
        Collection<DataSchemaNode> children = new HashSet<>();
        if (AnvExtensions.MOUNT_POINT.isExtensionIn(node)) {
            if (mountRegistry != null) {
                children = mountRegistry.getChildren(nodeSchemaPath);
            }
        }
        return children;
    }

    public static Set<QName> getStateAttributes(SchemaPath nodeSchemaPath, SchemaRegistry registry) {
        Set<QName> stateAttrs = new TreeSet<>();
        Collection<DataSchemaNode> children = registry.getChildren(nodeSchemaPath);
        for (DataSchemaNode child : children) {
            if (child instanceof ChoiceSchemaNode) {
                Collection<CaseSchemaNode> cases = ((ChoiceSchemaNode) child).getCases().values();
                List<DataSchemaNode> schemaNodes = ChoiceCaseNodeUtil.getAllNodesFromCases(cases);
                for (DataSchemaNode childNode : schemaNodes) {
                    if (!childNode.isConfiguration() && !(childNode instanceof DataNodeContainer)) {
                        stateAttrs.add(childNode.getQName());
                    }
                }
            }
            if (!child.isConfiguration() && !(child instanceof DataNodeContainer) && !(child instanceof ChoiceSchemaNode)) {
                stateAttrs.add(child.getQName());
            }
        }
        return stateAttrs;
    }

    public static Set<QName> getStateLeafListAttributes(SchemaPath nodeSchemaPath, SchemaRegistry registry) {
        Set<QName> stateAttrs = new TreeSet<>();
        Collection<DataSchemaNode> children = registry.getChildren(nodeSchemaPath);
        for(DataSchemaNode child :children){
            if(!child.isConfiguration() && (child instanceof LeafListSchemaNode) ){
                stateAttrs.add(child.getQName());
            }
        }
        return stateAttrs;
    }

    public static List<QName> getStateChildCases(SchemaRegistry schemaRegistry, ChoiceSchemaNode choiceSchemaNode) {
        List<QName> stateContainerCasesChildren = new ArrayList<>();
        for (CaseSchemaNode choiceNode : choiceSchemaNode.getCases().values()) {
            for (DataSchemaNode node : choiceNode.getChildNodes()) {
                if (node instanceof ContainerSchemaNode && !node.isConfiguration()) {
                    stateContainerCasesChildren.add(node.getQName());
                }
            }
        }
        return stateContainerCasesChildren;
    }

    public static List<SchemaPath> getSchemaPathFromCases(Collection<DataSchemaNode> nodes) {
        List<SchemaPath> casePaths = new ArrayList<>();
        for (DataSchemaNode node : nodes) {
            if (node instanceof ChoiceSchemaNode) {
                for (CaseSchemaNode caseNode : ((ChoiceSchemaNode) node).getCases().values()) {
                    for (DataSchemaNode caseChild : caseNode.getChildNodes()) {
                        casePaths.add(caseChild.getPath());
                    }
                }
            }
        }
        return casePaths;
    }

    public static SchemaPath getSchemaPathForElement(Element element, Collection<SchemaPath> availableSchemaPaths) {
        for (SchemaPath schemaPath : availableSchemaPaths) {
            QName lastComponent = schemaPath.getLastComponent();
            if (lastComponent.getNamespace().toString().equals(element.getNamespaceURI()) && lastComponent.getLocalName().equals(element.getLocalName())) {
                return schemaPath;
            }
        }
        return null;
    }

    public static Collection<SchemaPath> getSchemaPathsForNodes(Collection<DataSchemaNode> nodes) {
        List<SchemaPath> schemaPaths = new ArrayList<>();
        for (DataSchemaNode node : nodes) {
            schemaPaths.add(node.getPath());
        }
        return schemaPaths;
    }

    public static Set<IdentitySchemaNode> getAllIdentities(SchemaRegistry schemaRegistry) {
        Set<IdentitySchemaNode> identifiers = new HashSet<IdentitySchemaNode>();
        Set<ModuleIdentifier> moduleIds = schemaRegistry.getAllModuleIdentifiers();
        for (ModuleIdentifier moduleId : moduleIds) {
            Optional<Module> module = schemaRegistry.getModule(moduleId.getName(), moduleId.getRevision().orElse(null));
            identifiers.addAll(module.get().getIdentities());
        }
        return identifiers;
    }

    public static List<String> getAllModulesNamespaceAsString(SchemaRegistry schemaRegistry) {
        List<String> namespaces = new ArrayList<String>();
        Set<ModuleIdentifier> modules = schemaRegistry.getAllModuleIdentifiers();
        for (ModuleIdentifier module : modules) {
            namespaces.add(module.getNamespace().toString());
        }
        return namespaces;
    }


    public static List<SchemaPath> getModuleSubtreeRoots(SchemaRegistry schemaRegistry, String moduleName, String moduleRevision) {
        Optional<Module> module = schemaRegistry.getModule(moduleName, moduleRevision == null || moduleRevision.isEmpty() ? null : Revision.of(moduleRevision));
        List<SchemaPath> subtreeRoots = new ArrayList<>();
        if (module.isPresent()) {
            for (DataSchemaNode root : module.get().getChildNodes()) {
                subtreeRoots.add(root.getPath());
            }
            for (AugmentationSchemaNode augmentationSchema : module.get().getAugmentations()) {
                for (DataSchemaNode augmentingNode : augmentationSchema.getChildNodes()) {
                    subtreeRoots.add(augmentingNode.getPath());
                }
            }
        }
        return subtreeRoots;
    }

    public static SchemaPath getDataParentSchemaPath(SchemaRegistry schemaRegistry, SchemaPath schemaPath) {
        DataSchemaNode parentNode = getDataParent(schemaRegistry, schemaPath);
        if (parentNode == null) {
            return null;
        }
        return parentNode.getPath();
    }

    public static DataSchemaNode getDataParent(SchemaRegistry schemaRegistry, SchemaPath schemaPath) {
        SchemaPath parentSchemaPath = schemaPath.getParent();
        if (parentSchemaPath == null) {
            return null;
        }

        DataSchemaNode parentSchemaNode = schemaRegistry.getDataSchemaNode(parentSchemaPath);
        while (parentSchemaNode instanceof CaseSchemaNode || parentSchemaNode instanceof ChoiceSchemaNode) {
            parentSchemaPath = parentSchemaPath.getParent();
            if (parentSchemaPath == null) {
                return null;
            }
            parentSchemaNode = schemaRegistry.getDataSchemaNode(parentSchemaPath);
        }
        return parentSchemaNode;
    }

    /**
     * Given a module name and prefix, getNamespaceFromModule returns namespace corresponding to the prefix by searching
     * current module and its imports
     *
     * @param schemaRegistry
     * @param moduleName
     * @param prefix
     * @return
     */
    public static String getNamespaceFromModule(SchemaRegistry schemaRegistry, String moduleName, String prefix) {
        Optional<Module> optModule = schemaRegistry.getModule(moduleName);
        if (optModule.isPresent()) {
            Module module = optModule.get();
            if (module.getPrefix().equals(prefix)) {
                return module.getNamespace().toString();
            }

            Iterator<ModuleImport> importedModules = YangParserUtil.getAllModuleImports(module).iterator();
            while (importedModules.hasNext()) {
                ModuleImport moduleImport = importedModules.next();
                if (moduleImport.getPrefix().equals(prefix)) {
                    Optional<Module> importedModule = schemaRegistry.getModule(moduleImport.getModuleName(), moduleImport.getRevision().orElse(null));
                    if (importedModule.isPresent()) {
                        return importedModule.get().getNamespace().toString();
                    }
                }
            }
        }
        return null;
    }

    public static SchemaRegistry createSchemaRegistry(Collection<String> resourcesDirs, Set<QName> supportedFeatures, Map<QName, Set<QName>> supportedDeviations, boolean isYangLibrarySupportedInHelloMessage, ReadWriteLockService readWriteLockService) throws SchemaBuildException {
        List<YangTextSchemaSource> yangSchemaSources = getYangSchemaSources(resourcesDirs);
        return new SchemaRegistryImpl(yangSchemaSources, supportedFeatures, supportedDeviations, isYangLibrarySupportedInHelloMessage, readWriteLockService);
    }

    public static List<YangTextSchemaSource> getYangSchemaSources(Collection<String> resourcesDirs){
        Set<File> files = new HashSet<>();
        for (String resourcesDir : resourcesDirs) {
            File folder = new File(resourcesDir);
            listFilesForFolder(folder, files, YANG_EXTENSION);
        }
        return getYtss(files);
    }
    
    public static void listFilesForFolder(final File folder, Set<File> matchingFiles, String extension) {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry, matchingFiles, extension);
            } else {
                if (extension == null) {
                    matchingFiles.add(fileEntry);
                } else if (fileEntry.getName().endsWith(extension)) {
                    matchingFiles.add(fileEntry);
                }

            }
        }
    }

    public static List<YangTextSchemaSource> getYtss(Set<File> files) {
        List<YangTextSchemaSource> list = new ArrayList<YangTextSchemaSource>();
        for (File file : files) {
            list.add(new FileYangSource(file));
        }
        return list;
    }

    public static boolean containsWhen(DataSchemaNode child) {
        if (child != null) {
            Optional<RevisionAwareXPath> optWhenCondition = child.getWhenCondition();
            if (optWhenCondition != null && optWhenCondition.isPresent()) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsAugmentWhen(DataSchemaNode child, SchemaRegistry registry) {
        Optional<RevisionAwareXPath> xpath = getWhenOnAugment(child, registry);
        if(xpath.isPresent()){
            return true;
        }
        return false;
    }

    private static Optional<RevisionAwareXPath> getWhenOnAugment(DataSchemaNode child, SchemaRegistry registry) {
        if (child != null && child.isAugmenting()) {
            DataSchemaNode parentSchemaNode = registry.getNonChoiceParent(child.getPath());
            if (parentSchemaNode != null) {
                Pair<AugmentationSchemaNode, SchemaPath> augmentNodeAndItsResidingNode = DataStoreValidationUtil.getAugmentationSchema(registry, parentSchemaNode, child);
                AugmentationSchemaNode augSchema = augmentNodeAndItsResidingNode == null ? null : augmentNodeAndItsResidingNode.getFirst();
                if (augSchema != null) {
                    Optional<RevisionAwareXPath> xpath = augSchema.getWhenCondition();
                    if (xpath.isPresent()) {
                        return xpath;
                    }
                    //else try the parent.
                }
            }
        }
        return Optional.empty();
    }

    public static boolean containsUsesWhen(DataSchemaNode child, SchemaRegistry registry) {
        if (child != null && child.isAddedByUses()) {
            DataSchemaNode parentSchemaNode = registry.getNonChoiceParent(child.getPath());
            if(parentSchemaNode != null){
                Pair<UsesNode,SchemaPath> usesNodeAndItsResidingNode = SchemaContextUtil.getUsesSchema(registry, parentSchemaNode, child);
                UsesNode usesNode = usesNodeAndItsResidingNode == null? null : usesNodeAndItsResidingNode.getFirst();
                if (usesNode != null) {
                    Optional<RevisionAwareXPath> xpath = usesNode.getWhenCondition();
                    if (xpath.isPresent()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean hasDefaults(DataSchemaNode node) {
        if ( !AnvExtensions.IGNORE_DEFAULT.isExtensionIn(node)) {
            if (node instanceof LeafSchemaNode && ((LeafSchemaNode) node).getType().getDefaultValue().isPresent()) {
                return true;
            }
        }
        return false;
    }

    public static boolean needsMMNCheck(DataSchemaNode node) {
        if (node instanceof LeafSchemaNode) {
            if ( ((LeafSchemaNode) node).isMandatory() || hasDefaults(node)) {
                return true;
            }
        } else if (node instanceof ChoiceSchemaNode) {
            if ( ((ChoiceSchemaNode) node).isMandatory() || ((ChoiceSchemaNode) node).getDefaultCase().isPresent()) {
                return true;
            }
        } else if (node instanceof ElementCountConstraintAware){
            Optional<ElementCountConstraint> optElementCountConstraint = ((ElementCountConstraintAware) node).getElementCountConstraint();
            if (optElementCountConstraint.isPresent()) {
                ElementCountConstraint elementCountConstraint = optElementCountConstraint.get();
                if (elementCountConstraint.getMinElements() != null && elementCountConstraint.getMinElements() > 0) {
                    return true;
                }
            }
        } else if ( node instanceof ContainerSchemaNode && !((ContainerSchemaNode) node).isPresenceContainer()){
            for ( DataSchemaNode childNode  : ((ContainerSchemaNode) node).getChildNodes()){
                if ( needsMMNCheck(childNode)){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if the give node is a choice/case and returns the next proper DataSchemaNode which is not a case/choice
     * If the input is not a choice/case, returns the same
     */
    public static DataSchemaNode getEffectiveParentNode(SchemaNode schemaNode, SchemaRegistry schemaRegistry) {
        if(schemaNode == null){
            return null;
        }
        DataSchemaNode returnNode = SchemaRegistryUtil.getDataSchemaNode(schemaRegistry, schemaNode.getPath().getParent()).getDataSchemaNode();
        if (returnNode == null && schemaNode.getPath().getParent().getLastComponent() == null && schemaRegistry.getMountPath() != null) {
            SchemaPath mountPath = schemaRegistry.getMountPath();
            if (mountPath != null) {
                SchemaNode rootNode = schemaRegistry.getChild(mountPath, schemaNode.getQName());
                if (rootNode != null && rootNode.equals(schemaNode)) {
                    return schemaRegistry.getParentRegistry().getDataSchemaNode(mountPath);
                }
            }
        }
        // If parent schemaNode is Action Definition, then get parent data schema node of action definition
        if (returnNode == null) {
            ActionDefinition actionDefinition = schemaRegistry
                    .getActionDefinitionNode(DataPathUtil.convertToDataPath(schemaNode.getPath().getParent()));
            if (actionDefinition != null) {
                SchemaPath parentDataSchemaPath = schemaNode.getPath().getParent().getParent();
                DataSchemaNode parentSchemaNode = schemaRegistry.getDataSchemaNode(parentDataSchemaPath);
                returnNode = getEffectiveParentNode(parentSchemaNode, schemaRegistry);
            }
        }

        if (returnNode instanceof ChoiceSchemaNode || returnNode instanceof CaseSchemaNode) {
            returnNode = getEffectiveParentNode(returnNode, schemaRegistry);
        }
        return returnNode;
    }

    public static void resetCache(){
        RequestScope scope = RequestScope.getCurrentScope();
        scope.putInCache(SchemaRegistry.CHILD_NODE_CACHE, null);
        scope.putInCache(SchemaRegistry.CHILD_NODE_INDEX_CACHE, null);
        scope.putInCache(SchemaRegistry.SCHEMAPATH_SCHEMANODE_CACHE, null);
    }



    public static DataSchemaNode getSchemaNode(SchemaRegistry registry, ModelNodeId nodeId) {
        List<ModelNodeRdn> rdns = nodeId.getRdnsReadOnly();

        if(rdns.isEmpty()){
            return null;
        }

        Iterator<ModelNodeRdn> rdnIterator = rdns.iterator();
        Collection<DataSchemaNode> rootDataSchemaNodes = registry.getRootDataSchemaNodes();

        DataSchemaNode matchingNode = getMatchingSchemaNode(rootDataSchemaNodes, rdnIterator.next());
        if(matchingNode != null){
            while (rdnIterator.hasNext()){
                if(matchingNode instanceof ListSchemaNode){
                    //skip list keys
                    int size = ((ListSchemaNode) matchingNode).getKeyDefinition().size();
                    for(int i = 0; i < size;i++){
                        if(rdnIterator.hasNext()){
                            rdnIterator.next();
                        }else {
                            return matchingNode;
                        }
                    }
                }
                if(rdnIterator.hasNext()){
                    ModelNodeRdn containerRdn = rdnIterator.next();
                    matchingNode = getMatchingSchemaNode(registry.getNonChoiceChildren(matchingNode.getPath()), containerRdn);
                }
            }
        }

        return matchingNode;
    }

    public static SchemaPath getSchemaPath(SchemaRegistry registry, ModelNodeId nodeId) {
        DataSchemaNode schemaNode = getSchemaNode(registry, nodeId);
        if(schemaNode == null){
            return SchemaPath.ROOT;
        }
        return schemaNode.getPath();
    }

    public static SchemaRegistry getSchemaRegistry(ModelNode modelNode, SchemaRegistry schemaRegistry){    	
        SchemaRegistry registry = schemaRegistry;
        if(modelNode.hasSchemaMount() || modelNode.isSchemaMountImmediateChild()){
            registry = modelNode.getMountRegistry();
        } else {
            ModelNode grandParent = modelNode.getParent();
            while(grandParent != null){
                if(grandParent.hasSchemaMount() || grandParent.isSchemaMountImmediateChild()){
                    registry = modelNode.getMountRegistry();
                    break;
                }
                grandParent = grandParent.getParent();
            }
        }
        return registry;
    }

    private static DataSchemaNode getMatchingSchemaNode(Collection<DataSchemaNode> dataSchemaNodes, ModelNodeRdn rdn) {
        for(DataSchemaNode node : dataSchemaNodes){
            if(nodesMatch(node.getQName(), rdn)){
                return node;
            }
        }
        return null;
    }

    private static boolean nodesMatch(QName qname, ModelNodeRdn rdn) {
        return qname.getNamespace().toString().equals(rdn.getNamespace()) && qname.getLocalName().equals(rdn.getRdnValue());
    }

    public static DSValidationMountContext getDataSchemaNode(SchemaRegistry schemaRegistry, SchemaPath schemaPath) {
        DataSchemaNode schemaNode = schemaRegistry.getDataSchemaNode(schemaPath);
        if ( schemaNode == null && schemaRegistry.getParentRegistry() != null){
            schemaNode = schemaRegistry.getParentRegistry().getDataSchemaNode(schemaPath);
        }
        DSValidationMountContext context = new DSValidationMountContext();
        context.setSchemaRegistry(schemaRegistry);
        context.setDataSchemaNode(schemaNode);
        context.setSchemaPath(schemaNode != null ? schemaNode.getPath() : null);
        return context;
    }

    public static SchemaRegistry getMountRegistryFromXmlRequest(Element request, SchemaRegistry globalRegistry) {
        MountProviderInfo info = getMountProviderInfo(request, globalRegistry);
        if ( info != null){
            return info.getMountedRegistry();
        }
        return null;
    }

    public static MountProviderInfo getMountProviderInfo(Element request, SchemaRegistry globalRegistry) {
        if (request != null) {
            QName rootQname = QName.create(request.getNamespaceURI(), request.getLocalName());
            Collection<DataSchemaNode> rootNodes = globalRegistry.getRootDataSchemaNodes();
            DataSchemaNode rootDsn = null;
            for ( DataSchemaNode rootNode : rootNodes){
                if ( rootNode.getQName().getNamespace().equals(rootQname.getNamespace()) && rootNode.getQName().getLocalName().equals(rootQname.getLocalName())){
                    rootDsn = rootNode;
                    break;
                }
            }
            if ( globalRegistry.getMountRegistry() != null && rootDsn != null){
                SchemaMountRegistryProvider mrp = globalRegistry.getMountRegistry().getProvider(rootDsn.getPath());
                return getMountRegistryProviderRecursively(request, mrp, rootDsn, globalRegistry);
            }
        }
        return null;
    }

    private static MountProviderInfo getMountRegistryProviderRecursively(Node request, SchemaMountRegistryProvider mountRegistryProvider, 
            DataSchemaNode parentDataSchemaNode, SchemaRegistry globalRegistry) {
        if ( parentDataSchemaNode instanceof DataNodeContainer){
            for ( DataSchemaNode childDataSchemaNode : ((DataNodeContainer) parentDataSchemaNode).getChildNodes()){
                if ( childDataSchemaNode instanceof DataNodeContainer){
                    NodeList childNodes = request.getChildNodes();
                    for ( int index=0; index<childNodes.getLength(); index++){
                        Node childNode = childNodes.item(index);
                        if ( childNode.getNodeType()==Node.ELEMENT_NODE){
                            if ( childDataSchemaNode.getQName().getNamespace().toString().equals(childNode.getNamespaceURI()) &&
                                    childDataSchemaNode.getQName().getLocalName().equals(childNode.getLocalName())){
                                mountRegistryProvider = globalRegistry.getMountRegistry().getProvider(childDataSchemaNode.getPath());
                                if ( mountRegistryProvider != null){
                                    return new MountProviderInfo(mountRegistryProvider, (Element)childNode, childDataSchemaNode);
                                } else {
                                    MountProviderInfo mountRegistryProviderRecursively = getMountRegistryProviderRecursively(childNode, mountRegistryProvider, childDataSchemaNode, globalRegistry);
                                    if ( mountRegistryProviderRecursively != null ){
                                        return mountRegistryProviderRecursively;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public static Map<SchemaPath, HintDetails> getHintDetails(Module module, SchemaRegistry schemaRegistry, DataStoreValidationPathBuilder pathBuilder){
        Map<SchemaPath, HintDetails> referringSPHintDetails = new HashMap<>();
        Set<HintDetails> hintDetailsSet = new HashSet<>();
        List<UnknownSchemaNode> unknownNodes = module.getUnknownSchemaNodes();
        for(UnknownSchemaNode unknownNode : unknownNodes){
            if(AnvExtensions.VALIDATION_HINTS.isExtensionIn(unknownNode.getExtensionDefinition())){
                DataSchemaNode schemaNode = null;
                Expression targetNodeExpr = null;
                if(unknownNode instanceof ModelStatement) {
                    String targetNode = ((ModelStatement)unknownNode).argument().toString();
                    schemaNode = getDataSchemaNode(targetNode, module, schemaRegistry);
                    if(schemaNode == null){
                        throw new RuntimeException(String.format("Invalid targetNode '%s' in the validationHint Extension", targetNode));
                    }
                }
                List<UnknownSchemaNode> unknownChildExtns = unknownNode.getUnknownSchemaNodes();
                UnknownSchemaNode expressionNode = getExpressionExtension(unknownChildExtns);
                RevisionAwareXPath matchedXpath = null;
                boolean isExpressionMatched = false;
                if(expressionNode != null && expressionNode instanceof ModelStatement){
                    String expression = ((ModelStatement)expressionNode).argument().toString();
                    targetNodeExpr = JXPathUtils.getExpression(expression);
                    if (schemaNode instanceof WhenConditionAware) {
                        Optional<RevisionAwareXPath> optWhenCondition = schemaNode.getWhenCondition();
                        matchedXpath = compareExpressionWithXPath(targetNodeExpr, optWhenCondition);
                        if(matchedXpath != null) {
                            isExpressionMatched = true;
                        }
                    }
                    if (!isExpressionMatched && schemaNode instanceof MustConstraintAware) {
                        Collection<MustDefinition> mustConstraints = ((MustConstraintAware) schemaNode).getMustConstraints();
                        for(MustDefinition mustConstraint : mustConstraints) {
                            RevisionAwareXPath mustCondition = mustConstraint.getXpath();
                            String xpath = mustCondition.getOriginalString();
                            Expression mustExpression = JXPathUtils.getExpression(xpath);
                            if(targetNodeExpr.toString().equals(mustExpression.toString())){
                                isExpressionMatched = true;
                                matchedXpath = mustCondition;
                            }
                        }
                    }
                    if(!isExpressionMatched && schemaNode.isAugmenting()) {
                        Optional<RevisionAwareXPath> optWhenCondition = getWhenOnAugment(schemaNode, schemaRegistry);
                        matchedXpath = compareExpressionWithXPath(targetNodeExpr, optWhenCondition);
                        if(matchedXpath != null) {
                            isExpressionMatched = true;
                        }
                    }
                }
                if(!isExpressionMatched){
                    throw new RuntimeException(String.format("Expression %s not present in the target schemapath %s " , targetNodeExpr.toString(), ((ModelStatement)unknownNode).argument().toString()));
                }
                SchemaPath referringSP = schemaNode.getPath();
                UnknownSchemaNode hintsNode = getHintsExtension(unknownChildExtns);
                Map<SchemaPath, ValidationHint> parsedHints = null;
                boolean isSkipValidation = false;
                if(hintsNode != null && hintsNode instanceof ModelStatement){
                    String hints = ((ModelStatement)hintsNode).argument().toString();
                    Map<SchemaPath,ArrayList<String>> constraints = pathBuilder.getSchemaPathsFromXPath(schemaNode, matchedXpath.getOriginalString(), null);
                    Pair<Map<SchemaPath, ValidationHint>, Boolean> hintsPair = parseHints(constraints.keySet(), hints, schemaRegistry, module);
                    parsedHints = hintsPair.getFirst();
                    isSkipValidation = hintsPair.getSecond();
                }
                HintDetails hintDetails = new HintDetails();
                hintDetails.setReferringSP(referringSP);
                hintDetails.setTargetNodeExpression(targetNodeExpr);
                hintDetails.setReferredSPToHints(parsedHints);
                hintDetails.setIsSkipValidation(isSkipValidation);
                hintDetailsSet.add(hintDetails);
                referringSPHintDetails.put(referringSP, hintDetails);
            }
        }
        return referringSPHintDetails;
    }

    private static RevisionAwareXPath compareExpressionWithXPath(Expression targetNodeExpr, Optional<RevisionAwareXPath> condition) {
        RevisionAwareXPath matchedXpath = null;
        if (condition != null && condition.isPresent()) {
            String xpath = condition.get().getOriginalString();
            Expression whenExpression = JXPathUtils.getExpression(xpath);
            if(targetNodeExpr.toString().equals(whenExpression.toString())){
                matchedXpath = condition.get();
            }
        }
        return matchedXpath;
    }

    private static DataSchemaNode getDataSchemaNode(String node, Module module, SchemaRegistry schemaRegistry){
        List<QName> qnames = new ArrayList<>();
        String[] paths = node.trim().split(SLASH);
        for(String path : paths){
            if(!path.isEmpty()) {
                QName qname = buildQName(path, module, schemaRegistry);
                qnames.add(qname);
            }
        }
        return schemaRegistry.getDataSchemaNode(SchemaPath.create(qnames, true));
    }

    private static QName buildQName(String name, Module module, SchemaRegistry schemaRegistry){
        QName qname = null;
        String[] nameWithPrefix = name.split(COLON);
        String localName = null;
        if(nameWithPrefix.length == 2){
            Module qnameModule = null;
            if(module.getPrefix().equals(nameWithPrefix[0])){
                qnameModule = module;
            }else {
                Set<ModuleImport> moduleImports = module.getImports();
                for (ModuleImport moduleImport : moduleImports) {
                    if (moduleImport.getPrefix().equals(nameWithPrefix[0])) {
                        String moduleName = moduleImport.getModuleName();
                        qnameModule = schemaRegistry.getModule(moduleName).get();
                        break;
                    }
                }
            }
            localName = nameWithPrefix[1];
            qname = QName.create(qnameModule.getNamespace(), qnameModule.getRevision(), localName);            
        } else {
            qname = QName.create(module.getNamespace(), module.getRevision(), localName);            
        }
        return qname;
    }

    private static UnknownSchemaNode getExpressionExtension(Collection<UnknownSchemaNode> unknownNodes){
        for(UnknownSchemaNode unknownNode : unknownNodes){
            if(AnvExtensions.EXPRESSION.isExtensionIn(unknownNode.getExtensionDefinition())){
                return unknownNode;
            }
        }
        return null;
    }

    private static UnknownSchemaNode getHintsExtension(Collection<UnknownSchemaNode> unknownNodes){
        for(UnknownSchemaNode unknownNode : unknownNodes){
            if(AnvExtensions.HINTS.isExtensionIn(unknownNode.getExtensionDefinition())){
                return unknownNode;
            }
        }
        return null;
    }

    public static Pair<Map<SchemaPath, ValidationHint>, Boolean> parseHints(Set<SchemaPath> allReferredSPs, String hintStr, SchemaRegistry schemaRegistry, Module module) {
    	Map<SchemaPath, ValidationHint> hints = new HashMap<>();
    	boolean isSkipValidation = false;

        Pattern pattern = Pattern.compile(VALIDATION_HINTS_REGEX_PATTERN);
        Matcher matcher = pattern.matcher(hintStr);
        List<String> hintsForSps = new ArrayList();
        String unMatchedHintString = hintStr;
        while(matcher.find()) {
            String matchedString = hintStr.substring(matcher.start(), matcher.end());
            hintsForSps.add(matchedString);
            unMatchedHintString = unMatchedHintString.replace(matchedString, "");
        }
        if(hintsForSps.isEmpty()){
            throw new IllegalArgumentException("Hints should not be empty");
        }
        if(unMatchedHintString.trim().length() > 0){
            throw new IllegalArgumentException("Cannot parse part of hint : "+hintStr);
        }
    	for(String hintForSp : hintsForSps){
    		if(hintForSp.isEmpty()){
    			throw new IllegalArgumentException("Hints should not be empty");
    		} else {
    			String [] split = hintForSp.trim().split(ARROW_OPERATOR);
    			if(split.length > 2){
    				throw new IllegalArgumentException("Cannot parse part of extension argument: "+hintForSp);
    			}
    			String spStr = split[0].trim();
    			String hint = split[1].trim();
    			ValidationHint validationHint = ValidationHint.valueOf(hint);
    			if(!isSkipValidation && validationHint.equals(ValidationHint.SKIP_VALIDATION)){
    			    isSkipValidation = true;
    			}
    			if(WILDCARD.equals(spStr)){
    				hints.clear();
    				for (SchemaPath referredSP : allReferredSPs) {
    					hints.put(referredSP, validationHint);
    				}
    				break;
    			}
    			boolean isReferredSPExist = true;
    			try {
                    DataSchemaNode referredSchemaNode = getDataSchemaNode(spStr, module,
                        schemaRegistry);
                    if (referredSchemaNode == null) {
                        isReferredSPExist = false;
                    } else {
                        SchemaPath sp = referredSchemaNode.getPath();
                        hints.put(sp, validationHint);
                    }
                }
    			catch (IllegalArgumentException e) {
                    isReferredSPExist = false;
                }
    			if(!isReferredSPExist) {
                    throw new IllegalArgumentException("ReferredSP does not exists for hint: "+spStr);
                }
    		}
    	}
    	return new Pair<Map<SchemaPath,ValidationHint>, Boolean>(hints, isSkipValidation);
    }

    public static SchemaPath getSchemaPathFromSchemaNodeId(SchemaRegistry schemaRegistry, Map<String, String> prefixToNsMap, String pathStr){
        List<QName> qnames = new ArrayList<>();
        String[] paths = pathStr.split(DocumentUtils.SEPARATED);
        for(String path : paths){
            if(!path.isEmpty()) {
                String prefix = null;
                String namespace = null;
                String localName = null;
                Module module = null;
                QName qname = null;
                Revision revision = null;
                SchemaRegistry coreSchemaRegistry = null;
                String[] pathWithPrefix = path.split(DocumentUtils.COLON);
                if(pathWithPrefix.length > 2){
                    throw new RuntimeException(String.format("Invalid path %s specified in the schemapath string %s", path, pathStr));
                }else if(pathWithPrefix.length == 2){
                    prefix = pathWithPrefix[0];
                    localName = pathWithPrefix[1];
                } else {
                    localName = pathWithPrefix[0];
                    prefix = DocumentUtils.XMLNS;
                }
                namespace = prefixToNsMap.get(prefix);
                if(namespace != null) {
                   module = schemaRegistry.getModuleByNamespace(namespace);
                   if(module == null) {
                       coreSchemaRegistry  = schemaRegistry.getParentRegistry();
                       if(coreSchemaRegistry == null) {
                           throw new RuntimeException(String.format("Could not find the module for the namespace %s because of missing parent registry for the plugSchemaRegistry %s", namespace, schemaRegistry.getName()));
                       }
                       module = coreSchemaRegistry.getModuleByNamespace(namespace);
                   }
                   if(module == null){
                       throw new RuntimeException(String.format("Could not find the module for the namespace %s in the plugSchemaRegistry %s and in the parentSchemaRegistry %s", namespace, schemaRegistry.getName(), coreSchemaRegistry.getName()));
                   }
                   revision = module.getRevision().get();
                   if(revision != null) {
                       qname = QName.create(namespace, localName, revision);
                       qnames.add(qname);
                   } else {
                       qname = QName.create(namespace, localName);
                       qnames.add(qname);
                   }
                } else {
                    throw new RuntimeException(String.format("Could not find the namespace for the prefix %s in the pathStr %s", prefix, pathStr));
                }
            }
        }
        return SchemaPath.create(qnames, true);
    }

    public static boolean hasDefaultOrHasChildWithDefaults(SchemaRegistry schemaRegistry, DataSchemaNode dataSchemaNode) {
        return getSubtreeNodeWithDefaults(schemaRegistry, dataSchemaNode) != null;
    }

    public static DataSchemaNode getSubtreeNodeWithDefaults(SchemaRegistry schemaRegistry, DataSchemaNode dataSchemaNode) {
        if(hasDefaults(dataSchemaNode)){
            return dataSchemaNode;
        }
        Collection<DataSchemaNode> children = schemaRegistry.getNonChoiceChildren(dataSchemaNode.getPath());
        for (DataSchemaNode child : children) {
            // we don't auto-instantiate list entries, so we need not go deep once we hit a list entry
            if (!(child instanceof ListSchemaNode)&& hasDefaultOrHasChildWithDefaults(schemaRegistry, child)){
                return child;
            }
        }

        return null;
    }


    public static boolean isLeafListOrderedByUser(QName qName, SchemaPath parentSP, SchemaRegistry schemaRegistry) {
        LeafListSchemaNode leafListSchemaNode = getLeafListSchemaNode(qName, parentSP, schemaRegistry);
        return leafListSchemaNode.isUserOrdered();
    }

    private static LeafListSchemaNode getLeafListSchemaNode(QName qName, SchemaPath parentSP, SchemaRegistry schemaRegistry) {
        Collection<DataSchemaNode> schemaNodes = schemaRegistry.getChildren(parentSP);
        LeafListSchemaNode leafListSchemaNode = null;
        for (DataSchemaNode dataSchemaNode : schemaNodes) {
            if (dataSchemaNode instanceof LeafListSchemaNode) {
                if (dataSchemaNode.getQName().equals(qName)) {
                    leafListSchemaNode = (LeafListSchemaNode) dataSchemaNode;
                    break;
                }
            }else if (dataSchemaNode instanceof ChoiceSchemaNode) {
                LeafListSchemaNode leafListFromNestedChoice = ChoiceCaseNodeUtil.getLeafListSchemaNodeFromNestedChoice(dataSchemaNode, qName);
                if (leafListFromNestedChoice != null) {
                    return leafListFromNestedChoice;
                }
            }
        }
        if (leafListSchemaNode == null) {
            throw new RuntimeException(String.format("Cannot get the schema node for '%s'", qName));
        }
        return leafListSchemaNode;
    }

    public static boolean isListOrderedByUser(QName qName, SchemaPath parentSP, SchemaRegistry schemaRegistry) throws ModelNodeGetException {
        boolean isOrderedByUser = false;
        ListSchemaNode listSchemaNode = getListSchemaNode(qName, parentSP, schemaRegistry);
        if (listSchemaNode != null) {
            isOrderedByUser = listSchemaNode.isUserOrdered();
        }
        return isOrderedByUser;
    }

    public static ListSchemaNode getListSchemaNode(QName qName, SchemaPath parentSP, SchemaRegistry schemaRegistry) throws ModelNodeGetException {
        Collection<DataSchemaNode> schemaNodes = schemaRegistry.getChildren(parentSP);
        ListSchemaNode listSchemaNode = null;
        for (DataSchemaNode dataSchemaNode : schemaNodes) {
            if (dataSchemaNode.getQName().equals(qName)) {
                if (dataSchemaNode instanceof ListSchemaNode) {
                    listSchemaNode = (ListSchemaNode)dataSchemaNode;
                    break;
                }
            }
            if (dataSchemaNode instanceof ChoiceSchemaNode) {
                ListSchemaNode nestedListSchemaNode = ChoiceCaseNodeUtil.getListSchemaNodeFromNestedChoice(dataSchemaNode, qName);
                if(nestedListSchemaNode != null){
                    listSchemaNode = nestedListSchemaNode;
                    break;
                }
            }
        }
        if (listSchemaNode == null) {
            throw new ModelNodeGetException(String.format("Cannot get the schema node for '%s'", qName));
        }
        return listSchemaNode;
    }

    public static Set<TypedDataSchemaNode> getConfigLeafsLeafListsInSubtreeOfLeafrefType(DataSchemaNode dataSchemaNode, SchemaRegistry schemaRegistry) {
        Set<TypedDataSchemaNode> children = new HashSet<>();
        for (DataSchemaNode child : schemaRegistry.getNonChoiceChildren(dataSchemaNode.getPath())) {
            if(child.isConfiguration()){
                if(child instanceof LeafSchemaNode || child instanceof LeafListSchemaNode){
                    if(((TypedDataSchemaNode)child).getType() instanceof LeafrefTypeDefinition){
                        children.add((TypedDataSchemaNode) child);
                    }
                } else if(child instanceof DataNodeContainer) {
                    children.addAll(getConfigLeafsLeafListsInSubtreeOfLeafrefType(child, schemaRegistry));
                }
            }
        }
        return children;
    }

    public static SchemaRegistry retrievePossibleMountRegistry(Element element, SchemaPath schemaPath, SchemaRegistry schemaRegistry){
        return retrievePossibleMountRegistry(element, schemaPath, schemaRegistry,false);
    }

    public static SchemaRegistry retrievePossibleMountRegistry(Element element, SchemaPath schemaPath, SchemaRegistry schemaRegistry,
                                                               boolean resetCache) {
        if (AnvExtensions.MOUNT_POINT.isExtensionIn(schemaRegistry.getDataSchemaNode(schemaPath))) {
            SchemaMountRegistryProvider provider = schemaRegistry.getMountRegistry().getProvider(schemaPath);
            if (provider != null) {
                SchemaRegistry mountRegistry = null;
                try {
                    mountRegistry = provider.getSchemaRegistry(element);
                    if(resetCache) {
                        RequestScope.getCurrentScope().putInCache(SchemaRegistry.CHILD_NODE_INDEX_CACHE, null);
                    }
                } catch (GetException e) {
                    throw new ValidationException(e.getRpcError());
                }
                schemaRegistry = mountRegistry != null ? mountRegistry : schemaRegistry;
            }
        }
        return schemaRegistry;
    }
}
