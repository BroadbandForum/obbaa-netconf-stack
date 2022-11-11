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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.PojoToDocumentTransformer;
import org.broadband_forum.obbaa.netconf.api.parser.YangParserUtil;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.type.builtin.IdentityRefTypeConstraintParser;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.BitsTypeUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.NamespaceUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.dom.EncryptDecryptUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.IanaCryptHashUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.TransformerUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.EditTreeTransformer;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcValidationException;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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
        ConfigLeafAttribute configLeafAttribute;
        TypeDefinition<?> typeDefinition = child.getType();
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
            configLeafAttribute = getGenericConfigLeafAttribute(schemaRegistry, typeDefinition, value, namespace, localName, child);
        }
        return configLeafAttribute;
    }

    public static ConfigLeafAttribute getConfigAttribute(SchemaRegistry schemaRegistry, SchemaPath parentSchemaPath,
                                                         QName childQName, Node childNode) throws InvalidIdentityRefException {

        SchemaPath childSchemaPath = schemaRegistry.getDescendantSchemaPath(parentSchemaPath,childQName);
        DataSchemaNode childSchemaNode = schemaRegistry.getDataSchemaNode(childSchemaPath);
        ConfigLeafAttribute configLeafAttribute = null;
        try {
            configLeafAttribute = ConfigAttributeFactory.getConfigAttribute(schemaRegistry,childSchemaNode, childNode);
        } catch (NetconfMessageBuilderException e) {
            LOGGER.error("Fail to convert document to string {}", e);
            throw new RuntimeException(e);
        }
        return configLeafAttribute;
    }

    public static ConfigLeafAttribute getConfigAttribute(SchemaRegistry schemaRegistry, DataSchemaNode schemaNode, Node node) throws InvalidIdentityRefException, NetconfMessageBuilderException {
        return getConfigAttribute(schemaRegistry, schemaNode, node, node.getTextContent(), true);
    }

    public static ConfigLeafAttribute getConfigAttribute(SchemaRegistry schemaRegistry, DataSchemaNode schemaNode, Node node, String value, boolean useNodeInfoForLocalNameAndNs) throws InvalidIdentityRefException, NetconfMessageBuilderException {
        ConfigLeafAttribute configLeafAttribute;
        TypeDefinition<?> typeDefinition = getTypeDefinition(schemaNode);
        if (typeDefinition instanceof UnionTypeDefinition) {
            List<TypeDefinition<?>> typeDefinitions = ((UnionTypeDefinition) typeDefinition).getTypes();
            TypeDefinition<?> childTypeDefinition = TransformerUtil.findUnionType(typeDefinitions, node, value);
            if (childTypeDefinition == null && !value.isEmpty()) {
                throw new RpcValidationException(NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.INVALID_VALUE,
                        "Invalid value " + value + " for " + node.getLocalName()));
            }
            value = BitsTypeUtil.orderBitsValue(childTypeDefinition, value);
            configLeafAttribute = createConfigAttributeInstance(schemaRegistry, schemaNode, node, childTypeDefinition, useNodeInfoForLocalNameAndNs, value);
        } else if (typeDefinition instanceof BitsTypeDefinition) {
            value = BitsTypeUtil.orderBitsValue(typeDefinition, value);
            configLeafAttribute = createConfigAttributeInstance(schemaRegistry, schemaNode, node, typeDefinition, useNodeInfoForLocalNameAndNs, value);
        } else {
            configLeafAttribute = createConfigAttributeInstance(schemaRegistry, schemaNode, node, typeDefinition, useNodeInfoForLocalNameAndNs, value);
        }
        return configLeafAttribute;
    }

    public static ConfigLeafAttributeWithInsertOp getConfigLeafListAttribute(SchemaRegistry schemaRegistry, DataSchemaNode schemaNode, Node node) throws InvalidIdentityRefException, NetconfMessageBuilderException {
        ConfigLeafAttribute configLeafAttribute = getConfigAttribute(schemaRegistry, schemaNode, node) ;
        return new ConfigLeafAttributeWithInsertOp(configLeafAttribute);
    }

    private static ConfigLeafAttribute createConfigAttributeInstance(SchemaRegistry schemaRegistry, DataSchemaNode schemaNode, Node node, TypeDefinition<?> typeDefinition, boolean useNodeInfoForLocalNameAndNs, String value)
            throws InvalidIdentityRefException {
        ConfigLeafAttribute configLeafAttribute;
        if (typeDefinition instanceof IdentityrefTypeDefinition) {
            configLeafAttribute = createIdentityRefConfigAttr(schemaRegistry, schemaNode, node, value, useNodeInfoForLocalNameAndNs);
        } else if (typeDefinition instanceof InstanceIdentifierTypeDefinition) {
            configLeafAttribute = createInstanceIdentifierConfigAttr(schemaRegistry, node);
        } else {
            String namespace;
            String localName;
            if(useNodeInfoForLocalNameAndNs) {
                namespace = node.getNamespaceURI();
                localName = EditTreeTransformer.resolveLocalName(schemaRegistry, namespace, node.getLocalName());
            } else {
                namespace = schemaNode.getQName().getNamespace().toString();
                localName = EditTreeTransformer.resolveLocalName(schemaRegistry, namespace, schemaNode.getQName().getLocalName());
            }
            if (!value.isEmpty()) {
                try {
                    value = String.valueOf(TransformerUtil.convert(typeDefinition, value));
                } catch (TransformerException e) {
                    LOGGER.error("Invalid value " + value + " for " + node.getLocalName(), e);
                    throw new RuntimeException(e);
                }
            }
            configLeafAttribute = getGenericConfigLeafAttribute(schemaRegistry, typeDefinition, value, namespace, localName, schemaNode);
        }
        return configLeafAttribute;
    }

    private static ConfigLeafAttribute getGenericConfigLeafAttribute(SchemaRegistry schemaRegistry, TypeDefinition<?> typeDefinition,
                                                                     String value, String nodeNamespace, String localName, DataSchemaNode leafSN) {
        value = IanaCryptHashUtils.generateHashedValueIfTypeDefCryptHash(schemaRegistry, typeDefinition, value);
        GenericConfigAttribute configAttribute = new GenericConfigAttribute(localName, nodeNamespace, value);
        if(EncryptDecryptUtil.isPassword(leafSN, schemaRegistry)){
            configAttribute.setIsPassword(true);
        }
        return configAttribute;
    }

    private static ConfigLeafAttribute createInstanceIdentifierConfigAttr(SchemaRegistry schemaRegistry, Node node) {
        ConfigLeafAttribute configLeafAttribute;Map<String, String> NsPrefixMap = resolveInstanceIdentifierNSPrefix(schemaRegistry,node);
        String localName = EditTreeTransformer.resolveLocalName(schemaRegistry,node.getNamespaceURI(),node.getLocalName());
        configLeafAttribute = new InstanceIdentifierConfigAttribute(NsPrefixMap,node.getNamespaceURI(),localName,node.getTextContent());
        return configLeafAttribute;
    }

    private static ConfigLeafAttribute createIdentityRefConfigAttr(SchemaRegistry schemaRegistry, SchemaNode schemaNode,
                                                                   Node node, String value, boolean useNodeInfoForLocalNameAndNs)
            throws InvalidIdentityRefException {
        ConfigLeafAttribute configLeafAttribute;
        Pair<String,String> prefixNamespacePair = resolveIdentityRefNamespace(schemaNode, node, value);
        String identityRefNs = prefixNamespacePair.getFirst();
        String identityRefPrefix = schemaRegistry.getPrefix(identityRefNs);
        if(identityRefPrefix==null){
            LOGGER.error("Cannot get prefix for namespace {}", identityRefNs);
            NetconfRpcError error = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.INVALID_VALUE,
                    "Cannot get prefix for namespace " + identityRefNs
                            + ". Value \"" + value + "\" is not a valid identityref value.");
            throw new InvalidIdentityRefException(error);
        }
        if (useNodeInfoForLocalNameAndNs) {
            setRightPrefixInValue(identityRefPrefix, node, identityRefNs, prefixNamespacePair.getSecond());
            configLeafAttribute = getIdentityRefConfigAttribute(schemaRegistry, identityRefNs, identityRefPrefix, node.getNamespaceURI(), node.getLocalName(), node.getTextContent());
        } else {
            value = identityRefPrefix + ":" + value.substring(value.indexOf(":") + 1);
            configLeafAttribute = getIdentityRefConfigAttribute(schemaRegistry, identityRefNs, identityRefPrefix, schemaNode.getQName().getNamespace().toString(), schemaNode.getQName().getLocalName(), value);
        }
        return configLeafAttribute;
    }

    private static ConfigLeafAttribute getIdentityRefConfigAttribute(SchemaRegistry schemaRegistry, String identityRefNs, String identityRefPrefix, String namespace, String localName1, String value) {
        ConfigLeafAttribute configLeafAttribute;
        String localName = EditTreeTransformer.resolveLocalName(schemaRegistry, namespace, localName1);
        configLeafAttribute = new IdentityRefConfigAttribute(identityRefNs,identityRefPrefix, localName, value, namespace);
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

    private static void setRightPrefixInValue(String identityRefPrefix, Node element, String identityRefNs, String unmodifiedPrefix) {
        String elementValue = element.getTextContent();
        element.setTextContent(identityRefPrefix + ":" + elementValue.substring(elementValue.indexOf(":")+1));
        Element nodeElement = (Element) element;
        if(unmodifiedPrefix != null) {
            nodeElement.setAttributeNS(PojoToDocumentTransformer.XMLNS_NAMESPACE, PojoToDocumentTransformer.XMLNS + identityRefPrefix, identityRefNs);
        }
    }

    private static String documentToPrettyString(Node element){
        String elementStr = null;
        try {
            elementStr = DocumentUtils.documentToPrettyString(element);
        } catch (NetconfMessageBuilderException e) {}
        return elementStr;
    }

    private static Pair<String, String> resolveIdentityRefNamespace(SchemaNode schemaNode, Node element, String identityRefValue) throws InvalidIdentityRefException {
        String attributeNS;
        String prefix = null;
        if (identityRefValue.contains(":")){
            int prefixIndex = identityRefValue.indexOf(':');
            prefix = identityRefValue.substring(0, prefixIndex);
            attributeNS = NamespaceUtil.getAttributeNameSpace(element, prefix);

            if(attributeNS==null){
                String elementStr = documentToPrettyString(element);
                LOGGER.error("Cannot get the namespace for the prefix {} from node element {}", prefix, elementStr);
                NetconfRpcError error = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.INVALID_VALUE,
                        "Cannot get the namespace for the prefix " + prefix
                                + ". Value \"" + identityRefValue + "\" is not a valid identityref value.");
                throw new InvalidIdentityRefException(error);
            }
        } else {
            attributeNS = NamespaceUtil.getAttributeNameSpace(element, null);
            if(attributeNS==null || attributeNS.equals(IdentityRefTypeConstraintParser.DEFAULT_NC_NS)){
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("attrbuteNS is {} for schema {} and element value {}. Will use default NS of the schemaNode {}",
                            attributeNS, schemaNode.getPath(), identityRefValue, schemaNode.getQName().getNamespace());
                }
                if (schemaNode != null && schemaNode.getQName() != null && schemaNode.getQName().getNamespace() != null) {
                    attributeNS = schemaNode.getQName().getNamespace().toString();
                }

                if(attributeNS==null){
                    LOGGER.error("Default namespace is null");
                    NetconfRpcError error = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.INVALID_VALUE,
                            "Default namespace is null. Value \"" + identityRefValue + "\" is not a valid identityref value.");
                    throw new InvalidIdentityRefException(error);
                }

            }
        }

        return new Pair<String, String>(attributeNS, prefix) {
        };
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
        DataSchemaNode childSchemaNode = schemaRegistry.getDataSchemaNode(childSchemaPath);

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
            configLeafAttribute = getGenericConfigLeafAttribute(schemaRegistry, typeDefinition, attributeValue, attributeNamespace, localName, childSchemaNode);
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
