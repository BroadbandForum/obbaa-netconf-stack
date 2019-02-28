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

import org.apache.commons.jxpath.ri.compiler.LocationPath;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.Step;
import org.broadband_forum.obbaa.netconf.api.parser.FileYangSource;
import org.broadband_forum.obbaa.netconf.api.parser.YangParserUtil;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.ModuleIdentifier;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaMountRegistryProvider;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.AnvExtensions;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.MountProviderInfo;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DSValidationMountContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.ReadWriteLockService;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.dom4j.dom.DOMNodeHelper;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
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
    
    public static final String ENABLE_MOUNT_POINT = "ENABLE_MOUNT_POINT";
    public static final String MOUNT_CONTEXT_SCHEMA_REGISTRY = "MOUNT_CONTEXT_SCHEMA_REGISTRY";
    public static final String MOUNT_CONTEXT_SUBSYSTEM_REGISTRY = "MOUNT_CONTEXT_SUBSYSTEM_REGISTRY";
    public static final String MOUNT_CONTEXT_PROVIDER = "MOUNT_CONTEXT_PROVIDER";
    public static final String MOUNT_PATH = "MOUNT_PATH";

    public static boolean isMountPointEnabled() {
        Boolean returnValue = (Boolean) RequestScope.getCurrentScope().getFromCache(ENABLE_MOUNT_POINT);
        if (returnValue == null) {
            returnValue = System.getenv().containsKey(ENABLE_MOUNT_POINT) ? Boolean.parseBoolean(System.getenv(ENABLE_MOUNT_POINT))
                    : System.getProperties().containsKey(ENABLE_MOUNT_POINT) ? Boolean.parseBoolean(System.getProperty(ENABLE_MOUNT_POINT))
                            : true;
            RequestScope.getCurrentScope().putInCache(ENABLE_MOUNT_POINT, returnValue);
        }
        return returnValue;
    }

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
     * @param schemaRegistry
     * @param nodeSchemaPath
     * @return
     */
    public static List<QName> getStateChildContainers(SchemaRegistry schemaRegistry, SchemaPath nodeSchemaPath) {
    	List<QName> stateContainerChildren = new ArrayList<>();
    	Set<DataSchemaNode> childNodes = new HashSet<>();
    	DataSchemaNode node = schemaRegistry.getDataSchemaNode(nodeSchemaPath);
    	if (node instanceof DataNodeContainer) {
    		DataNodeContainer container = (DataNodeContainer) node;
    		childNodes.addAll(container.getChildNodes());
    		childNodes.addAll(getMountPointChildNodes(nodeSchemaPath, node));
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
     * @param schemaRegistry
     * @param nodeSchemaPath
     * @return
     */
    public static List<QName> getStateChildLists(SchemaRegistry schemaRegistry, SchemaPath nodeSchemaPath) {
    	List<QName> stateListChildren = new ArrayList<>();
    	Set<DataSchemaNode> childNodes = new HashSet<>();
    	DataSchemaNode node = schemaRegistry.getDataSchemaNode(nodeSchemaPath);
    	if (node instanceof DataNodeContainer) {
    		DataNodeContainer container = (DataNodeContainer) node;
    		childNodes.addAll(container.getChildNodes());
    		childNodes.addAll(getMountPointChildNodes(nodeSchemaPath, node));
    		for (DataSchemaNode child : childNodes) {
    			if (child instanceof ListSchemaNode && !child.isConfiguration()) {
    				stateListChildren.add(child.getQName());
    			}
    		}
    	}
    	return stateListChildren;
    }

    private static Collection<DataSchemaNode> getMountPointChildNodes(SchemaPath nodeSchemaPath, DataSchemaNode node) {
    	Collection<DataSchemaNode> children = new HashSet<>();
    	if (AnvExtensions.MOUNT_POINT.isExtensionIn(node)) {
    		SchemaRegistry mountRegistry = getMountRegistry();
    		if (mountRegistry != null) {
    			children = mountRegistry.getChildren(nodeSchemaPath);
    		}
    	}
    	return children;
    }

    public static Set<QName> getStateAttributes(SchemaPath nodeSchemaPath, SchemaRegistry registry) {
        Set<QName> stateAttrs = new TreeSet<>();
        Collection<DataSchemaNode> children = getSchemaRegistry(registry).getChildren(nodeSchemaPath);
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
            if (!child.isConfiguration() && !(child instanceof DataNodeContainer)) {
                stateAttrs.add(child.getQName());
            }
        }
        return stateAttrs;
    }

	public static Set<QName> getStateLeafListAttributes(SchemaPath nodeSchemaPath, SchemaRegistry registry) {
		Set<QName> stateAttrs = new TreeSet<>();
		Collection<DataSchemaNode> children = getSchemaRegistry(registry).getChildren(nodeSchemaPath);
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

	public static SchemaRegistry createSchemaRegistry(Collection<String> resourcesDirs, Set<QName> supportedFeatures, Map<QName, Set<QName>> supportedDeviations, boolean isYangLibrarySupportedInHelloMessage, ReadWriteLockService readWriteLockService) throws SchemaBuildException {
		Set<File> files = new HashSet<>();
		for (String resourcesDir : resourcesDirs) {
			File folder = new File(resourcesDir);
			listFilesForFolder(folder, files, YANG_EXTENSION);
		}
		List<YangTextSchemaSource> list = getYtss(files);
		return new SchemaRegistryImpl(list, supportedFeatures, supportedDeviations, isYangLibrarySupportedInHelloMessage, readWriteLockService);
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

    public static boolean hasDefaults(DataSchemaNode node) {
        if (node instanceof LeafSchemaNode && ((LeafSchemaNode) node).getType().getDefaultValue().isPresent() && !AnvExtensions.IGNORE_DEFAULT.isExtensionIn(node)) {
            return true;
        }
        return false;
    }

    /**
     * Checks if the give node is a choice/case and returns the next proper DataSchemaNode which is not a case/choice
     * If the input is not a choice/case, returns the same
     */
    public static DataSchemaNode getEffectiveParentNode(DataSchemaNode schemaNode, SchemaRegistry schemaRegistry) {

        DataSchemaNode returnNode = SchemaRegistryUtil.getDataSchemaNode(schemaRegistry, schemaNode.getPath().getParent()).getDataSchemaNode();
        if (returnNode == null && schemaNode.getPath().getParent().getLastComponent() == null) {
            SchemaRegistry mountRegistry = getMountRegistry();
            SchemaPath mountPath = mountRegistry == null ? null : mountRegistry.getMountPath();
            if (mountPath != null) {
                DataSchemaNode rootNode = mountRegistry.getChild(mountPath, schemaNode.getQName());
                if (rootNode != null && rootNode.equals(schemaNode)) {
                    return schemaRegistry.getDataSchemaNode(mountPath);
                }
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
        List<ModelNodeRdn> rdns = nodeId.getRdns();

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
    
    public static SchemaRegistry getSchemaRegistry(SchemaRegistry globalSchemaRegistry) {
        SchemaRegistry mountRegistry = getMountRegistry();
        if (mountRegistry != null) {
            return mountRegistry;
        }
        return globalSchemaRegistry;
    }
    
    public static DSValidationMountContext getDataSchemaNode(SchemaRegistry globalRegistry, SchemaPath schemaPath) {
        DataSchemaNode schemaNode = globalRegistry.getDataSchemaNode(schemaPath);
        DSValidationMountContext context = new DSValidationMountContext();
        SchemaRegistry mountRegistry = null;
        if (schemaNode == null) {
            mountRegistry = getMountRegistry();
            schemaNode = mountRegistry == null ? null : mountRegistry.getDataSchemaNode(schemaPath);
            
        }
        context.setSchemaRegistry(mountRegistry != null ? mountRegistry : globalRegistry);
        context.setDataSchemaNode(schemaNode);
        context.setSchemaPath(schemaNode != null ? schemaNode.getPath() : null);
        return context;
    }
    
    public static SchemaRegistry getMountRegistry() {
        SchemaRegistry mountRegistry = (SchemaRegistry) RequestScope.getCurrentScope().getFromCache(MOUNT_CONTEXT_SCHEMA_REGISTRY);
        return mountRegistry;
    }
    
    public static void resetSchemaRegistryCache(){
    	RequestScope.getCurrentScope().putInCache(SchemaMountRegistryProvider.PLUG_CONTEXT, null);
    	RequestScope.getCurrentScope().putInCache(MOUNT_CONTEXT_SCHEMA_REGISTRY, null);
    }
    
    public static void setMountRegistry(SchemaRegistry registry) {
    	if ( registry != null && registry.getMountPath() != null){
    		RequestScope.getCurrentScope().putInCache(MOUNT_CONTEXT_SCHEMA_REGISTRY, registry);
    	}
    }
    
    public static void setMountCurrentScope(Map<String, Object> mountCurrentScope){
    	RequestScope.getCurrentScope().putInCache(SchemaMountRegistryProvider.MOUNT_CURRENT_SCOPE, mountCurrentScope);
    	setMountRegistry( (SchemaRegistry) mountCurrentScope.get(SchemaMountRegistryProvider.MOUNT_SCHEMA_REGISTRY));
    }
    
    @SuppressWarnings("unchecked")
	public static Map<String, Object> getMountCurrentScope(){
    	return (Map<String, Object>) RequestScope.getCurrentScope().getFromCache(SchemaMountRegistryProvider.MOUNT_CURRENT_SCOPE);
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
                            if ( childDataSchemaNode.getQName().getNamespace().toString().equals(childNode.getNamespaceURI().toString()) &&
                                    childDataSchemaNode.getQName().getLocalName().equals(childNode.getLocalName())){
                                mountRegistryProvider = globalRegistry.getMountRegistry().getProvider(childDataSchemaNode.getPath());
                                if ( mountRegistryProvider != null){
                                    return new MountProviderInfo(mountRegistryProvider, childNode, childDataSchemaNode);
                                } else {
                                    return getMountRegistryProviderRecursively(childNode, mountRegistryProvider, childDataSchemaNode, globalRegistry);
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
