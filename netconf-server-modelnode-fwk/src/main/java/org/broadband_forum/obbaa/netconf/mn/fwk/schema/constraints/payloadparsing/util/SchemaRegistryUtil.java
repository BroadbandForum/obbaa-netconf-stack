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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.AnvExtensions;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.dom4j.dom.DOMNodeHelper;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.CaseEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ChoiceEffectiveStatementImpl;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.broadband_forum.obbaa.netconf.api.parser.FileYangSource;
import org.broadband_forum.obbaa.netconf.api.parser.YangParserUtil;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.ReadWriteLockService;

public class SchemaRegistryUtil {
    private static final String EQUAL_TO = "=";
    private static final String EQUAL_WITH_SPACES = " = ";
    private static final String SLASH = "/";
    public static final String YANG_EXTENSION = ".yang";
    private static final String COLON = ":";

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

    public static DataSchemaNode getChildSchemaNode(String childNs, String childLocalName, SchemaPath path,
                                                    SchemaRegistry schemaRegistry) {
        Collection<DataSchemaNode> children = ChoiceCaseNodeUtil.getChildrenUnderChoice(schemaRegistry, path);
        if (SchemaPath.ROOT.equals(path)) {
            children = schemaRegistry.getRootDataSchemaNodes();
        }
        for (DataSchemaNode childSchemaNode : children) {
            QName childQname = childSchemaNode.getQName();
            if (childLocalName.equals(childQname.getLocalName()) && childNs.equals(childQname.getNamespace().toString
                    ())) {
                return childSchemaNode;
            }
        }

        return null;
    }

    public static SchemaPath getSchemaPath(SchemaRegistry schemaRegistry, String xPath, String defaultNs, String
            moduleName) {

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

    public static String getPathWithPrefix(Node element, DataSchemaNode schemaNode, SchemaRegistry schemaRegistry,
                                           String childName) {
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
            errorPath.append("/").append(prefix + ":").append(childName);
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

    private static String buildPathWithPrefix(Node element, DataSchemaNode dataSchemaNode, SchemaRegistry
            schemaRegistry) {
        StringBuilder errorItem = new StringBuilder();
        String prefix = getPrefix(element, dataSchemaNode, schemaRegistry);
        if (dataSchemaNode instanceof ListSchemaNode) {
            errorItem.append(SLASH).append(prefix + ":").append(element.getLocalName());

            List<QName> keys = ((ListSchemaNode) dataSchemaNode).getKeyDefinition();
            NodeList childNodes = element.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node child = childNodes.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    DataSchemaNode childSchemaNode = getChildSchemaNode(child, dataSchemaNode.getPath(),
                            schemaRegistry);
                    if (childSchemaNode != null) {
                        QName childQName = childSchemaNode.getQName();
                        if (keys.contains(childQName)) {
                            TypeDefinition<?> baseType = getBaseType(childSchemaNode);
                            String keyValue = child.getTextContent();
                            String childPrefix = getPrefix(child, childSchemaNode, schemaRegistry);
                            if (baseType instanceof StringTypeDefinition || baseType instanceof EnumTypeDefinition) {
                                keyValue = "'" + keyValue + "'";
                            }
                            errorItem.append("[").append(childPrefix + ":").append(child.getLocalName()).append
                                    (EQUAL_WITH_SPACES).append(keyValue).append("]");
                        }
                    }
                }
            }
        } else if (dataSchemaNode instanceof ContainerSchemaNode) {
            errorItem.append(SLASH).append(prefix + ":").append(element.getLocalName());
        }

        return errorItem.toString();
    }

    public static Pair<String, Map<String, String>> getErrorPath(Node element, DataSchemaNode schemaNode,
                                                                 SchemaRegistry schemaRegistry, Element childElement) {
        Map<String, String> prefixToNSMap = new HashMap<>();
        if (schemaNode == null) {
            return new Pair<String, Map<String, String>>("", prefixToNSMap);
        }
        StringBuilder errorPath = new StringBuilder();
        errorPath.append(buildErrorPath(element, schemaNode, schemaRegistry, prefixToNSMap));

        DataSchemaNode parentSchemaNode = schemaRegistry.getNonChoiceParent(schemaNode.getPath());
        Node parentNode = element.getParentNode();
        while (parentSchemaNode != null && parentNode != null) {
            String parentErrorItem = buildErrorPath(parentNode, parentSchemaNode, schemaRegistry, prefixToNSMap);
            errorPath.insert(0, parentErrorItem);
            parentSchemaNode = schemaRegistry.getNonChoiceParent(parentSchemaNode.getPath());
            parentNode = parentNode.getParentNode();
        }

        if (childElement != null) {
            String childNodeName = fillPrefixToNSMapAndReturnNodeName((Element) childElement, schemaRegistry,
                    prefixToNSMap);
            errorPath.append("/").append(childNodeName);
        }

        return new Pair<String, Map<String, String>>(errorPath.toString(), prefixToNSMap);
    }

    public static Pair<String, Map<String, String>> getErrorPath(String errorPath, Node element, DataSchemaNode
            schemaNode, SchemaRegistry schemaRegistry) {
        Map<String, String> prefixToNSMap = new HashMap<>();
        return getErrorPath(new Pair<String, Map<String, String>>(errorPath, prefixToNSMap), element, schemaNode,
                schemaRegistry);
    }

    public static Pair<String, Map<String, String>> getErrorPath(Pair<String, Map<String, String>>
                                                                         existingErrorPathPair, Node element,
                                                                 DataSchemaNode schemaNode, SchemaRegistry
                                                                         schemaRegistry) {
        StringBuilder errorPathSB = new StringBuilder(existingErrorPathPair.getFirst());
        if (schemaNode != null) {
            errorPathSB.append(buildErrorPath(element, schemaNode, schemaRegistry, existingErrorPathPair.getSecond()));
        }

        return new Pair<String, Map<String, String>>(errorPathSB.toString(), existingErrorPathPair.getSecond());
    }

    private static String buildErrorPath(Node element, DataSchemaNode dataSchemaNode, SchemaRegistry schemaRegistry,
                                         Map<String, String> prefixToNSMap) {
        StringBuilder errorItem = new StringBuilder();
        String elementName = dataSchemaNode.getQName().getLocalName();
        if (element != null && elementName.equals(element.getLocalName())) {
            elementName = fillPrefixToNSMapAndReturnNodeName((Element) element, schemaRegistry, prefixToNSMap);
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
                    DataSchemaNode childSchemaNode = getChildSchemaNode(child, dataSchemaNode.getPath(),
                            schemaRegistry);
                    if (childSchemaNode != null) {
                        QName childQName = childSchemaNode.getQName();
                        if (keys.contains(childQName)) {
                            TypeDefinition<?> baseType = getBaseType(childSchemaNode);
                            String keyValue = child.getTextContent();
                            if (baseType instanceof StringTypeDefinition) {
                                keyValue = "'" + keyValue + "'";
                            }
                            String childNodeName = fillPrefixToNSMapAndReturnNodeName((Element) child,
                                    schemaRegistry, prefixToNSMap);
                            errorItem.append("[").append(childNodeName).append(EQUAL_TO).append(keyValue).append("]");
                        }
                    }
                }
            }
        }

        return errorItem.toString();
    }

    private static void fillPrefixToNSMap(String prefix, String ns, Map<String, String> prefixToNSMap) {
        if (prefixToNSMap.get(prefix) == null) {
            prefixToNSMap.put(prefix, ns);
        }
    }

    private static String fillPrefixToNSMapAndReturnNodeName(Element element, SchemaRegistry schemaRegistry,
                                                             Map<String, String> prefixToNSMap) {
        String elementName = element.getNodeName();
        String[] splits = elementName.split(COLON);

        if (splits.length == 2) {
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
                                                             Map<String, String> prefixToNSMap, String elementName,
                                                             String ns) {
        String prefix = schemaRegistry.getPrefix(ns);
        if (prefix != null && ns != null) {
            elementName = prefix + COLON + elementName;
            fillPrefixToNSMap(prefix, ns, prefixToNSMap);
        }
        return elementName;
    }

    private static TypeDefinition<?> getBaseType(DataSchemaNode childSchemaNode) {
        TypeDefinition<?> type = null;
        if (childSchemaNode instanceof LeafSchemaNode) {
            type = ((LeafSchemaNode) childSchemaNode).getType();
        } else if (childSchemaNode instanceof LeafListSchemaNode) {
            type = ((LeafListSchemaNode) childSchemaNode).getType();
        }
        return type;
    }

    /**
     * Returns all the children of type ContainerSchemaNode and are not configuration nodes.
     *
     * @param schemaRegistry
     * @param nodeSchemaPath
     * @return
     */
    public static List<QName> getStateChildContainers(SchemaRegistry schemaRegistry, SchemaPath nodeSchemaPath) {
        List<QName> stateContainerChildren = new ArrayList<>();
        DataSchemaNode node = schemaRegistry.getDataSchemaNode(nodeSchemaPath);
        if (node instanceof DataNodeContainer) {
            DataNodeContainer container = (DataNodeContainer) node;
            for (DataSchemaNode child : container.getChildNodes()) {
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
     * @param schemaRegistry
     * @param nodeSchemaPath
     * @return
     */
    public static List<QName> getStateChildLists(SchemaRegistry schemaRegistry, SchemaPath nodeSchemaPath) {
        List<QName> stateListChildren = new ArrayList<>();
        DataSchemaNode node = schemaRegistry.getDataSchemaNode(nodeSchemaPath);
        if (node instanceof DataNodeContainer) {
            DataNodeContainer container = (DataNodeContainer) node;
            for (DataSchemaNode child : container.getChildNodes()) {
                if (child instanceof ListSchemaNode && !child.isConfiguration()) {
                    stateListChildren.add(child.getQName());
                }
            }
        }
        return stateListChildren;
    }

    public static List<QName> getStateChildCases(SchemaRegistry schemaRegistry, ChoiceSchemaNode choiceSchemaNode) {
        List<QName> stateContainerCasesChildren = new ArrayList<>();
        for (ChoiceCaseNode choiceNode : choiceSchemaNode.getCases()) {
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
                for (ChoiceCaseNode caseNode : ((ChoiceSchemaNode) node).getCases()) {
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
            if (lastComponent.getNamespace().toString().equals(element.getNamespaceURI()) && lastComponent
                    .getLocalName().equals(element.getLocalName())) {
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
            Module module = schemaRegistry.getModule(moduleId.getName(), moduleId.getRevision());
            identifiers.addAll(module.getIdentities());
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


    public static List<SchemaPath> getModuleSubtreeRoots(SchemaRegistry schemaRegistry, String moduleName, String
            moduleRevision) {
        Module module = schemaRegistry.getModule(moduleName, moduleRevision);
        List<SchemaPath> subtreeRoots = new ArrayList<>();
        if (module != null) {
            for (DataSchemaNode root : module.getChildNodes()) {
                subtreeRoots.add(root.getPath());
            }
            for (AugmentationSchema augmentationSchema : module.getAugmentations()) {
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
        while (parentSchemaNode instanceof ChoiceCaseNode || parentSchemaNode instanceof ChoiceSchemaNode) {
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
        Module module = schemaRegistry.getModule(moduleName);
        if (module != null) {
            if (module.getPrefix().equals(prefix)) {
                return module.getNamespace().toString();
            }

            Iterator<ModuleImport> importedModules = YangParserUtil.getAllModuleImports(module).iterator();
            while (importedModules.hasNext()) {
                ModuleImport moduleImport = importedModules.next();
                if (moduleImport.getPrefix().equals(prefix)) {
                    Module importedModule = schemaRegistry.getModule(moduleImport.getModuleName(), moduleImport
                            .getRevision());
                    if (importedModule != null) {
                        return importedModule.getNamespace().toString();
                    }
                }
            }
        }
        return null;
    }

    public static ChoiceSchemaNode getChoiceParentSchemaNode(SchemaPath schemaPath, SchemaRegistry schemaRegistry) {
        SchemaPath parentSchemaPath = schemaPath.getParent();
        while (parentSchemaPath != null) {
            DataSchemaNode parentSchemaNode = schemaRegistry.getDataSchemaNode(parentSchemaPath);
            if (parentSchemaNode instanceof ChoiceSchemaNode) {
                return (ChoiceSchemaNode) parentSchemaNode;
            }
            parentSchemaPath = parentSchemaPath.getParent();
        }
        return null;
    }

    public static SchemaRegistry createSchemaRegistry(Collection<String> resourcesDirs, boolean
            isYangLibrarySupportedInHelloMessage, ReadWriteLockService readWriteLockService) throws
            SchemaBuildException {
        Set<File> files = new HashSet<>();
        for (String resourcesDir : resourcesDirs) {
            File folder = new File(resourcesDir);
            listFilesForFolder(folder, files, YANG_EXTENSION);
        }
        List<YangTextSchemaSource> list = getYtss(files);
        return new SchemaRegistryImpl(list, isYangLibrarySupportedInHelloMessage, readWriteLockService);
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
            ConstraintDefinition constraint = child.getConstraints();
            if (constraint != null && constraint.getWhenCondition() != null) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasDefaults(DataSchemaNode node) {
        if (node instanceof LeafSchemaNode && ((LeafSchemaNode) node).getDefault() != null && !AnvExtensions
                .IGNORE_DEFAULT.isExtensionIn(node)) {
            return true;
        }
        return false;
    }

    /**
     * Checks if the give node is a choice/case and returns the next proper DataSchemaNode which is not a case/choice
     * If the input is not a choice/case, returns the same
     */
    public static DataSchemaNode getEffectiveParentNode(DataSchemaNode schemaNode, SchemaRegistry schemaRegistry) {

        DataSchemaNode returnNode = schemaRegistry.getDataSchemaNode(schemaNode.getPath().getParent());
        if (returnNode instanceof ChoiceEffectiveStatementImpl || returnNode instanceof CaseEffectiveStatementImpl) {
            returnNode = getEffectiveParentNode(returnNode, schemaRegistry);
        }
        return returnNode;
    }

    public static void resetCache() {
        RequestScope scope = RequestScope.getCurrentScope();
        scope.putInCache(SchemaRegistry.CHILD_NODE_CACHE, null);
        scope.putInCache(SchemaRegistry.CHILD_NODE_INDEX_CACHE, null);
        scope.putInCache(SchemaRegistry.SCHEMAPATH_SCHEMANODE_CACHE, null);
    }


    public static DataSchemaNode getSchemaNode(SchemaRegistry registry, ModelNodeId nodeId) {
        List<ModelNodeRdn> rdns = nodeId.getRdns();

        if (rdns.isEmpty()) {
            return null;
        }

        Iterator<ModelNodeRdn> rdnIterator = rdns.iterator();
        Collection<DataSchemaNode> rootDataSchemaNodes = registry.getRootDataSchemaNodes();

        DataSchemaNode matchingNode = getMatchingSchemaNode(rootDataSchemaNodes, rdnIterator.next());
        if (matchingNode != null) {
            while (rdnIterator.hasNext()) {
                if (matchingNode instanceof ListSchemaNode) {
                    //skip list keys
                    int size = ((ListSchemaNode) matchingNode).getKeyDefinition().size();
                    for (int i = 0; i < size; i++) {
                        if (rdnIterator.hasNext()) {
                            rdnIterator.next();
                        } else {
                            return matchingNode;
                        }
                    }
                }
                if (rdnIterator.hasNext()) {
                    ModelNodeRdn containerRdn = rdnIterator.next();
                    matchingNode = getMatchingSchemaNode(registry.getNonChoiceChildren(matchingNode.getPath()),
                            containerRdn);
                }
            }
        }

        return matchingNode;
    }

    public static SchemaPath getSchemaPath(SchemaRegistry registry, ModelNodeId nodeId) {
        DataSchemaNode schemaNode = getSchemaNode(registry, nodeId);
        if (schemaNode == null) {
            return SchemaPath.ROOT;
        }
        return schemaNode.getPath();
    }

    private static DataSchemaNode getMatchingSchemaNode(Collection<DataSchemaNode> dataSchemaNodes, ModelNodeRdn rdn) {
        for (DataSchemaNode node : dataSchemaNodes) {
            if (nodesMatch(node.getQName(), rdn)) {
                return node;
            }
        }
        return null;
    }

    private static boolean nodesMatch(QName qname, ModelNodeRdn rdn) {
        return qname.getNamespace().toString().equals(rdn.getNamespace()) && qname.getLocalName().equals(rdn.getRdnValue());
    }
}
