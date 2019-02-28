package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.jxpath.JXPathContext;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.parser.YangParserUtil;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.type.builtin.IdentityRefTypeConstraintParser;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.IdentityRefUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.NamespaceUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.EditTreeTransformer;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcValidationException;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.w3c.dom.Document;
import org.w3c.dom.Node;


public class ConfigAttributeFactory {

    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(ConfigAttributeFactory.class, LogAppNames.NETCONF_STACK);
    public static final String XMLNS = "xmlns";
    public static final String DELIMITER = ",";
    public static final String SPACE = " ";
    private static final String COLON = ":";

    public static ConfigLeafAttribute getConfigAttributeFromDefaultValue(SchemaRegistry schemaRegistry, LeafSchemaNode child) {
        String defaultValue = LeafDefaultValueUtility.getDefaultValue(child);
        return getConfigLeafAttribute(schemaRegistry, child, defaultValue);
    }

    public static ConfigLeafAttribute getConfigLeafAttribute(SchemaRegistry schemaRegistry, LeafSchemaNode child, String value) {
        ConfigLeafAttribute configLeafAttribute;TypeDefinition<?> typeDefinition = child.getType();
        QName childQName = child.getQName();
        if (typeDefinition instanceof IdentityrefTypeDefinition) {
            IdentityrefTypeDefinition identityRefType = (IdentityrefTypeDefinition)child.getType();
            IdentitySchemaNode identity = identityRefType.getIdentities().iterator().next();
            QName identityRefQName = identity.getQName();
            String attributeNamespace = childQName.getNamespace().toString();
            String identityRefPrefix = null;
            String identityRefNs = identityRefQName.getNamespace().toString();
            if (value.contains(COLON)) {
                identityRefPrefix = value.split(COLON)[0];
            } else {
                identityRefPrefix = schemaRegistry.getPrefix(identityRefNs);
                value = identityRefPrefix + COLON + value;
            }
            String attributeLocalName = childQName.getLocalName();
            configLeafAttribute = new IdentityRefConfigAttribute(identityRefNs,
                    identityRefPrefix, attributeLocalName, value, attributeNamespace);
        } else if (typeDefinition instanceof InstanceIdentifierTypeDefinition) {
            String attributeNamespace = childQName.getNamespace().toString();
            String attributeLocalName = childQName.getLocalName();
            Map<String, String> nsPrefixMap = resolveInstanceIdentifierNSPrefix(schemaRegistry , child);
            configLeafAttribute = new InstanceIdentifierConfigAttribute(nsPrefixMap, attributeNamespace, attributeLocalName, value);
        } else {
            String namespace = childQName.getNamespace().toString();
            String localName = EditTreeTransformer.resolveLocalName(schemaRegistry, namespace, childQName.getLocalName());
            configLeafAttribute = new GenericConfigAttribute(localName, namespace, value);
        }
        return configLeafAttribute;
    }

    public static ConfigLeafAttribute getConfigAttribute(SchemaRegistry schemaRegistry, SchemaPath parentSchemaPath,
                                                         QName childQName, Node childNode) throws InvalidIdentityRefException {

        SchemaPath childSchemaPath = schemaRegistry.getDescendantSchemaPath(parentSchemaPath,childQName);
        SchemaNode childSchemaNode = schemaRegistry.getDataSchemaNode(childSchemaPath);
        ConfigLeafAttribute configLeafAttribute = ConfigAttributeFactory.getConfigAttribute(schemaRegistry,childSchemaNode, childNode
        );
        return configLeafAttribute;
    }

    private static ConfigLeafAttribute getConfigAttribute(SchemaRegistry schemaRegistry, SchemaNode schemaNode, Node node) throws InvalidIdentityRefException {
        ConfigLeafAttribute configLeafAttribute;
        TypeDefinition<?> typeDefinition = getTypeDefinition(schemaNode);
        if(typeDefinition instanceof UnionTypeDefinition){
            List<TypeDefinition<?>> typeDefinitions = ((UnionTypeDefinition)typeDefinition).getTypes();
            TypeDefinition<?> childTypeDefinition = findUnionType(typeDefinitions, node);
            if(childTypeDefinition == null) {
                throw new RpcValidationException(NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.INVALID_VALUE,
                        "Invalid value " + node.getTextContent() + " for " +node.getLocalName()));
            }
            configLeafAttribute = createConfigAttributeInstance(schemaRegistry, schemaNode, node, childTypeDefinition);
        }
        else {
            configLeafAttribute = createConfigAttributeInstance(schemaRegistry, schemaNode, node, typeDefinition);
        }
        return configLeafAttribute;
    }

    private static ConfigLeafAttribute createConfigAttributeInstance(SchemaRegistry schemaRegistry, SchemaNode schemaNode, Node node, TypeDefinition<?> typeDefinition) throws InvalidIdentityRefException {
        ConfigLeafAttribute configLeafAttribute;
        if(typeDefinition instanceof IdentityrefTypeDefinition){
            configLeafAttribute = createIdentityRefConfigAttr(schemaRegistry, schemaNode, node);
        }else if(typeDefinition instanceof InstanceIdentifierTypeDefinition){
            configLeafAttribute = createInstanceIdentifierConfigAttr(schemaRegistry, node);
        }else{
            String nodeNamespace = node.getNamespaceURI();
            String localName = EditTreeTransformer.resolveLocalName(schemaRegistry, nodeNamespace, node.getLocalName());
            configLeafAttribute = new GenericConfigAttribute(localName, nodeNamespace, node.getTextContent());
        }
        return configLeafAttribute;
    }

    private static TypeDefinition<?> findUnionType(List<TypeDefinition<?>> typeDefinitions, Node node) {
        for(TypeDefinition td : typeDefinitions){
            if(td instanceof IdentityrefTypeDefinition){
                String stringValue = node.getTextContent();
                String namespace;
                String identityValue;

                try{
                    namespace = IdentityRefUtil.getNamespace(node, stringValue);
                    identityValue = IdentityRefUtil.getIdentityValue(stringValue);
                } catch (Exception ex) {
                    continue;
                }
                boolean isValid;
                for (IdentitySchemaNode identitySchemaNode : obtainDerivedIdentities(td)) {
                    isValid = IdentityRefUtil.identityMatches(identitySchemaNode, namespace, identityValue);
                    if(!isValid){
                        isValid = IdentityRefUtil.checkDerivedIdentities(identitySchemaNode, namespace, identityValue);
                    }
                    if(isValid){
                        return td;
                    }
                }
            } else if(td instanceof InstanceIdentifierTypeDefinition) {
                if(checkIfInstanceIdentifier(node.getTextContent())) {
                    return td;
                }
            }
            else {
                return td;
            }
        }
        return null;
    }

    private static boolean checkIfInstanceIdentifier(String xpath) {
        try{
            JXPathContext.compile(xpath);
            return true;
        } catch (Exception ex) {
            LOGGER.info("The instance identifier is not a valid Xpath");
            return false;
        }
    }

    private static Set<IdentitySchemaNode> obtainDerivedIdentities(TypeDefinition type) {
        IdentityrefTypeDefinition baseType;
        Set<IdentitySchemaNode> derivedIdentities = new HashSet<>();
        baseType = (IdentityrefTypeDefinition)type;
        Set<IdentitySchemaNode> identitySchemaNodes = baseType.getIdentities();
        for(IdentitySchemaNode identitySchemaNode : identitySchemaNodes){
            derivedIdentities.addAll(identitySchemaNode.getDerivedIdentities());
        }
        return derivedIdentities;
    }


    private static ConfigLeafAttribute createInstanceIdentifierConfigAttr(SchemaRegistry schemaRegistry, Node node) {
        ConfigLeafAttribute configLeafAttribute;Map<String, String> NsPrefixMap = resolveInstanceIdentifierNSPrefix(schemaRegistry,node);
        String localName = EditTreeTransformer.resolveLocalName(schemaRegistry,node.getNamespaceURI(),node.getLocalName());
        configLeafAttribute = new InstanceIdentifierConfigAttribute(NsPrefixMap,node.getNamespaceURI(),localName,node.getTextContent());
        return configLeafAttribute;
    }

    private static ConfigLeafAttribute createIdentityRefConfigAttr(SchemaRegistry schemaRegistry, SchemaNode schemaNode, Node node) throws InvalidIdentityRefException {
        ConfigLeafAttribute configLeafAttribute;
        String identityRefNs = resolveIdentityRefNamespace(schemaNode, node);
        String identityRefPrefix = schemaRegistry.getPrefix(identityRefNs);
        if(identityRefPrefix==null){
            LOGGER.error("Cannot get prefix for namespace {}", identityRefNs);
            NetconfRpcError error = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.INVALID_VALUE,
                    "Cannot get prefix for namespace " + identityRefNs
                            + ". Value \"" + node.getTextContent() + "\" is not a valid identityref value.");
            throw new InvalidIdentityRefException(error);
        }
        setRightPrefixInValue(identityRefPrefix,node);
        String localName = EditTreeTransformer.resolveLocalName(schemaRegistry,node.getNamespaceURI(),node.getLocalName());
        configLeafAttribute = new IdentityRefConfigAttribute(identityRefNs,identityRefPrefix,localName, node.getTextContent(),node
                .getNamespaceURI());
        return configLeafAttribute;
    }

    private static TypeDefinition<?> getTypeDefinition(SchemaNode schemaNode) {
        TypeDefinition<?> typeDefinition = null;
        if(schemaNode instanceof LeafSchemaNode) {
            typeDefinition = ((LeafSchemaNode) schemaNode).getType();
        }else if(schemaNode instanceof LeafListSchemaNode){
            typeDefinition = ((LeafListSchemaNode)schemaNode).getType();
        }
        return typeDefinition;
    }

    private static void setRightPrefixInValue(String identityRefPrefix, Node element) {
        String elementValue = element.getTextContent();
        element.setTextContent(identityRefPrefix + ":" + elementValue.substring(elementValue.indexOf(":")+1));
    }

    private static String resolveIdentityRefNamespace(SchemaNode schemaNode, Node element) throws InvalidIdentityRefException {
        String identityRefValue = element.getTextContent();
        String attributeNS;
        if (identityRefValue.contains(":")){
            int prefixIndex = identityRefValue.indexOf(':');
            String prefix = identityRefValue.substring(0, prefixIndex);
            attributeNS = NamespaceUtil.getAttributeNameSpace(element, prefix);

            if(attributeNS==null){
                LOGGER.error("Cannot get the namespace for the prefix {}", prefix);
                NetconfRpcError error = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.INVALID_VALUE,
                        "Cannot get the namespace for the prefix " + prefix
                                + ". Value \"" + element.getTextContent() + "\" is not a valid identityref value.");
                throw new InvalidIdentityRefException(error);
            }
        } else {
            attributeNS = NamespaceUtil.getAttributeNameSpace(element, null);
            if(attributeNS==null || attributeNS.equals(IdentityRefTypeConstraintParser.DEFAULT_NC_NS)){
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("attrbuteNS is {} for schema {} and element value {}. Will use default NS of the schemaNode {}",
                            attributeNS, schemaNode.getPath(), element.getTextContent(), schemaNode.getQName().getNamespace());
                }
                if (schemaNode != null && schemaNode.getQName() != null && schemaNode.getQName().getNamespace() != null) {
                    attributeNS = schemaNode.getQName().getNamespace().toString();
                }
                
                if(attributeNS==null){
                    LOGGER.error("Default namespace is null");
                    NetconfRpcError error = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.INVALID_VALUE,
                            "Default namespace is null. Value \"" + element.getTextContent() + "\" is not a valid identityref value.");
                    throw new InvalidIdentityRefException(error);
                }

            }
        }

        return attributeNS;
    }

    private static Map<String, String> resolveInstanceIdentifierNSPrefix(SchemaRegistry schemaRegistry, Node child) {
        Map<String, String> nsPrefixMap = new LinkedHashMap<>();
        String[] xpathSteps = child.getTextContent().split("/");
        for (String xpathStep : xpathSteps){
            updateNsPrefixMap(nsPrefixMap,child,xpathStep);
            updateXPathStepWithRegistryPrefix(schemaRegistry,nsPrefixMap,child,xpathStep);
        }
        return nsPrefixMap;
    }

    private static Map<String, String> resolveInstanceIdentifierNSPrefix(SchemaRegistry schemaRegistry, LeafSchemaNode child) {
        Map<String, String> nsPrefixMap = new LinkedHashMap<>();
        Optional<? extends Object> defaultValue = child.getType().getDefaultValue();
        if (defaultValue.isPresent()) {
            String[] xpathSteps = defaultValue.toString().split("/");
            for (String xpathStep : xpathSteps) {
                updateNsPrefixMap(nsPrefixMap, xpathStep, child,schemaRegistry);
            }
        }
        return nsPrefixMap;
    }

    private static void updateNsPrefixMap(Map<String, String> nSPrefixMap, Node child,
                                          String originalNodeValue) {
        if (originalNodeValue.contains("[")){
            String[] values = originalNodeValue.split("\\[");
            for (String value:values){
                updateNsPrefixMap(nSPrefixMap, child, value);
            }

        }
        else if (originalNodeValue.contains(":")){
            String prefix = originalNodeValue.substring(0, originalNodeValue.indexOf(':'));
            String attributeNS = NamespaceUtil.getAttributeNameSpace(child, prefix);
            nSPrefixMap.put(attributeNS,prefix);
        }
    }

    private static void updateNsPrefixMap(Map<String, String> nSPrefixMap,
                                          String strWithPefix, LeafSchemaNode child, SchemaRegistry schemaRegistry) {
        if (strWithPefix.contains("[")) {
            String[] values = strWithPefix.split("\\[");
            for (String value : values) {
                updateNsPrefixMap(nSPrefixMap, value, child, schemaRegistry);
            }

        } else if (strWithPefix.contains(":")) {
            String prefix = strWithPefix.split(COLON)[0];
            Optional<Module> optChildModule = schemaRegistry.findModuleByNamespaceAndRevision(child.getQName().getModule().getNamespace(), child.getQName().getModule().getRevision().orElse(null));
            if (optChildModule.isPresent()) {
                Module childModule = optChildModule.get();
                Set<ModuleImport> imprtedModules = YangParserUtil.getAllModuleImports(childModule);
                Iterator<ModuleImport> moduleIterator = imprtedModules.iterator();
                while (moduleIterator.hasNext()) {
                    ModuleImport importedModule = moduleIterator.next();
                    if (importedModule.getPrefix().equalsIgnoreCase(prefix)) {
                        Optional<Module> requiredModule = schemaRegistry.getModule(importedModule.getModuleName(), importedModule.getRevision().orElse(null));
                        String attributeNs = requiredModule.get().getNamespace().toString();
                        nSPrefixMap.put(attributeNs, prefix);
                    }
                }
            }
        }
        //else there is no prefix defined, so no need to add to the map.
    }

    private static void updateXPathStepWithRegistryPrefix(SchemaRegistry schemaRegistry, Map<String, String> nSPrefixMap, Node child,
                                                          String xpathStep) {
        if (xpathStep.contains("[")){
            String[] values = xpathStep.split("\\[");
            for (String value:values){
                updateXPathStepWithRegistryPrefix(schemaRegistry, nSPrefixMap,child,value);
            }

        }
        else if (xpathStep.contains(":")){
            String prefix = xpathStep.substring(0, xpathStep.indexOf(':'));
            String attributeNs = NamespaceUtil.getAttributeNameSpace(child, prefix);
            String registryPrefix = schemaRegistry.getPrefix(attributeNs);

            nSPrefixMap.put(attributeNs,registryPrefix);
            String oldValue = child.getTextContent();
            String updatedValue = oldValue.replace(prefix, registryPrefix);
            child.setTextContent(updatedValue);
        }
    }

    public static ConfigLeafAttribute getConfigAttributeFromEntity(SchemaRegistry schemaRegistry, SchemaPath parentSchemaPath,
                                                                   String attributeNs,QName childQName, String attributeValue) {

        SchemaPath childSchemaPath = schemaRegistry.getDescendantSchemaPath(parentSchemaPath,childQName);
        SchemaNode childSchemaNode = schemaRegistry.getDataSchemaNode(childSchemaPath);

        ConfigLeafAttribute configLeafAttribute = null;
        TypeDefinition<?> typeDefinition = getTypeDefinition(childSchemaNode);
        String attributeNamespace = childQName.getNamespace().toString();
        String attributeLocalName = childQName.getLocalName();
        if(typeDefinition instanceof IdentityrefTypeDefinition){
            String identityRefPrefix = schemaRegistry.getPrefix(attributeNs);
            configLeafAttribute = new IdentityRefConfigAttribute(attributeNs,identityRefPrefix, attributeLocalName,attributeValue,
                    attributeNamespace);
        }else if(typeDefinition instanceof InstanceIdentifierTypeDefinition){
            Map<String, String> nsPrefixMap = formNsPrefixMap(attributeNs); //prefix stored in DB would be same as in schema registry
            configLeafAttribute = new InstanceIdentifierConfigAttribute(nsPrefixMap, attributeNamespace, attributeLocalName,attributeValue);
        }else{
            String localName = EditTreeTransformer.resolveLocalName(schemaRegistry, attributeNamespace, attributeLocalName);
            configLeafAttribute = new GenericConfigAttribute(localName, attributeNamespace, attributeValue);
        }

        return configLeafAttribute;
    }

    /**
     * nsPrefixValue is of format p1 ns1,p2 ns2... and so on.
     * @param nsPrefixValue
     * @return
     */
    private static Map<String, String> formNsPrefixMap(String nsPrefixValue) {
        Map<String, String> nsPrefixMap = new HashMap<>();
        String [] namespacePrefixValues = nsPrefixValue.split(DELIMITER);
        for(String nsp : namespacePrefixValues){
            String []  pair = nsp.split(SPACE);
            nsPrefixMap.put(pair[1], pair[0]); // key is the namespace, value is prefix
        }
        return nsPrefixMap;
    }

    public static Document getDocument(){
        Document document = (Document) RequestScope.getCurrentScope().getFromCache("CONFIG-ATTR-DOC");
        if(document==null){
            document = DocumentUtils.createDocument();
            RequestScope.getCurrentScope().putInCache("CONFIG-ATTR-DOC", document);
        }
        return document;
    }
}
