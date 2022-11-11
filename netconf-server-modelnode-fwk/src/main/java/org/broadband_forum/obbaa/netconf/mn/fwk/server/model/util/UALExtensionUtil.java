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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.ActionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcRequest;
import org.broadband_forum.obbaa.netconf.api.messages.RpcName;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.utils.SystemPropertyUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.AnvExtensions;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.OperationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.ModelStatement;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class UALExtensionUtil {

    public static final Integer c_ncUALMaxDepth;
    public static final Integer c_ncUALMaxAppNodes;
    private static final String EMPTY_STRING = "";
    private static final int UAL_MAX_ARGUMENTS_COUNT = 3;
    private static final int UAL_MAX_ALLOWED_VALUE_COUNT = 3;
    public static final Integer c_ncUALPayloadLimit;

    static {
        c_ncUALMaxDepth = Integer.valueOf(SystemPropertyUtils.getInstance().getFromEnvOrSysProperty("NC_UAL_MAX_DEPTH", "5"));
        c_ncUALMaxAppNodes = Integer.valueOf(SystemPropertyUtils.getInstance().getFromEnvOrSysProperty("NC_UAL_MAX_APP_NODES", "5"));
        c_ncUALPayloadLimit = Integer.valueOf(SystemPropertyUtils.getInstance().getFromEnvOrSysProperty("NC_UAL_PAYLOAD_LIMIT", "20000"));
    }

    private static String getUALApplicationName(SchemaNode schemaNode) {
        UnknownSchemaNode ualApplicationNode = AnvExtensions.UAL_APPLICATION.getExtensionDefinition(schemaNode);
        if (ualApplicationNode != null && ualApplicationNode instanceof ModelStatement) {
            return ((ModelStatement) ualApplicationNode).argument().toString();
        }
        return null;
    }

    private static boolean isUALDisabledExtension(SchemaNode schemaNode) {
        UnknownSchemaNode ualApplicationNode = AnvExtensions.UAL_DISABLED.getExtensionDefinition(schemaNode);
        return ualApplicationNode != null && ualApplicationNode instanceof ModelStatement;
    }

    private static boolean isExceedUALMaxDepth(int depth) {
        return c_ncUALMaxDepth < depth;
    }

    private static boolean isExceedUALMaxAppNodes(int appCount) {
        return c_ncUALMaxAppNodes < appCount;
    }

    /**
     * Find ual-applications and ual-arguments from config XML element.
     *
     * 1) Iterate all top level xml child elements
     *       Find equivalent yang schema node of top level xml element
     *       IF SchemaNode is Container
     *          IF find ual-application,
     *              add ual application-name into ualApplications
     *              add name of the container as ual argument to ualArguments until reach UAL_MAX_ARGUMENTS_COUNT
     *              iterate it's children until reach UALMaxDepth (or) UALMaxAppNodes
     *          Else
     *              iterate it's children until reach UALMaxDepth (or) UALMaxAppNodes
     *       Else-IF SchemaNode is LIST
     *          IF find ual-application
     *              add ual application-name to ualApplications
     *              add key nodes of LIST as arguments to ualArguments until reach UAL_MAX_ARGUMENTS_COUNT
     *          Else
     *              iterate it's children until reach UALMaxDepth (or) UALMaxAppNodes
     *       Else
     *          Skip leaf/leaf-list and other nodes
     *
     */
    public static void getUALValuesForEditConfig(SchemaRegistry schemaRegistry, Element configXml, Set<String> ualApplications,
                                                 List<String> ualArguments) {
        List<Element> rootElements = DocumentUtils.getChildElements(configXml);
        Collection<DataSchemaNode> rootSchemaNodes = schemaRegistry.getRootDataSchemaNodes();
        if (!rootElements.isEmpty() && !rootSchemaNodes.isEmpty()) {
            findUalExtensions(rootElements, rootSchemaNodes, schemaRegistry, ualApplications, ualArguments, 1);
        }
    }

    private static void findUalExtensions(List<Element> elementList, Collection<DataSchemaNode> schemaNodeList, SchemaRegistry schemaRegistry,
                                          Set<String> ualApplications, List<String> ualArguments, int depth){
        for (Element element : elementList) {
            DataSchemaNode rootSchemaNode = DataStoreValidationUtil.findSchemaNode(schemaRegistry, schemaNodeList, element);
            if (rootSchemaNode != null && rootSchemaNode.isConfiguration()) {
                if (rootSchemaNode instanceof ListSchemaNode || rootSchemaNode instanceof ContainerSchemaNode) {
                    String applicationName = getUALApplicationName(rootSchemaNode);
                    if (applicationName != null) {
                        // found the ual-application name
                        ualApplications.add(applicationName);
                        if (rootSchemaNode instanceof ContainerSchemaNode) {
                            if (ualArguments.size() <= UAL_MAX_ARGUMENTS_COUNT) {
                                ualArguments.add("container_node=" + rootSchemaNode.getQName().getLocalName());
                            }
                            // Need to iterate container node's children
                            findUalExtensionInChildrenNodes(schemaRegistry, rootSchemaNode, element, ualApplications, ualArguments,
                                    depth + 1);

                        } else {
                            getUALArgumentsForListNode(schemaRegistry, element, rootSchemaNode, ualArguments);
                        }
                    } else {
                        //iterate children (one more depth) to find application name
                        findUalExtensionInChildrenNodes(schemaRegistry, rootSchemaNode, element, ualApplications, ualArguments,
                                depth + 1);
                    }
                }
            }
            if (isExceedUALMaxAppNodes(ualApplications.size())) {
                return;
            }
        }
    }

    private static void findUalExtensionInChildrenNodes(SchemaRegistry schemaRegistry, DataSchemaNode parentSchemaNode, Node parentElement,
                                                        Set<String> ualApplications, List<String> ualArguments, int currentDepth) {
        // if we reached max depth level (or) max app-size, then don't need to iterate further
        if (isExceedUALMaxDepth(currentDepth) || isExceedUALMaxAppNodes(ualApplications.size())) {
            return;
        }
        List<Element> childNodes = DocumentUtils.getChildElements(parentElement);
        Collection<DataSchemaNode> childSchemaNodes = schemaRegistry.getNonChoiceChildren(parentSchemaNode.getPath());
        findUalExtensions(childNodes, childSchemaNodes, schemaRegistry, ualApplications, ualArguments, currentDepth);
    }

    private static void getUALArgumentsForListNode(SchemaRegistry schemaRegistry, Element listElement,
                                                   DataSchemaNode listDataSchemaNode, List<String> ualArguments) {
        if (ualArguments.size() > UAL_MAX_ARGUMENTS_COUNT) {
            return;
        }
        NodeList listNodes = listElement.getChildNodes();
        List<QName> yangKeys = ((ListSchemaNode) listDataSchemaNode).getKeyDefinition();
        List<QName> nodeKeys = new ArrayList<>();
        for (int i = 0; i < listNodes.getLength(); i++) {
            Node child = listNodes.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                QName qName = SchemaRegistryUtil.getChildQname(child, listDataSchemaNode, schemaRegistry);
                if (yangKeys.contains(qName)) {
                    ualArguments.add(qName.getLocalName() + "=" + child.getTextContent());
                    nodeKeys.add(qName);
                }
                if (nodeKeys.size() == yangKeys.size()) {
                    break;
                }
            }
        }
    }

    public static String getUalApplicationNameForActionOrRpcRequest(SchemaRegistry schemaRegistry, AbstractNetconfRequest request) {
        if (request instanceof NetconfRpcRequest) {
            RpcName rpcName =
                    ((NetconfRpcRequest) request).getRpcName();
            RpcDefinition rpcDefinition = UALExtensionUtil.getRpcDefinition(schemaRegistry, rpcName.getName(),
                    rpcName.getNamespace());
            return rpcDefinition != null ? getUALApplicationName(rpcDefinition) : EMPTY_STRING;
        } else if (request instanceof ActionRequest) {
            ActionDefinition actionDef = ((ActionRequest) request).getActionDefinition();
            return actionDef != null ? getUALApplicationName(actionDef) : EMPTY_STRING;
        }
        return null;
    }

    public static boolean hasUALDisabledExtension(SchemaRegistry schemaRegistry, AbstractNetconfRequest request) {
        OperationDefinition operationDefinition = null;
        if (request instanceof NetconfRpcRequest) {
            RpcName rpcName = ((NetconfRpcRequest) request).getRpcName();
            operationDefinition = UALExtensionUtil.getRpcDefinition(schemaRegistry, rpcName.getName(),
                    rpcName.getNamespace());

        } else if (request instanceof ActionRequest) {
            operationDefinition = ((ActionRequest) request).getActionDefinition();
        }
        return operationDefinition != null && isUALDisabledExtension(operationDefinition);
    }

    private static RpcDefinition getRpcDefinition(SchemaRegistry schemaRegistry, String rpcName, String namespace) {
        Collection<RpcDefinition> rpcDefinitions = schemaRegistry.getRpcDefinitions();
        for (RpcDefinition rpcDefinition : rpcDefinitions) {
            QName qName = rpcDefinition.getQName();
            if (qName.getLocalName().equals(rpcName) && qName.getNamespace().toString().equals(namespace)) {
                return rpcDefinition;
            }
        }
        return null;
    }

    public static String valuesToString(Collection<String> ualAnnotation) {
		if (ualAnnotation != null) {
			ualAnnotation = ualAnnotation.stream().filter(val -> StringUtils.isNotEmpty(val))
					.collect(Collectors.toList());
		}
    	if(ualAnnotation.isEmpty()){
            return EMPTY_STRING;
        } else if (ualAnnotation.size() == 1 ){
            return ualAnnotation.iterator().next();
        } else if (ualAnnotation.size() > UAL_MAX_ALLOWED_VALUE_COUNT) {
            StringBuilder appStr = new StringBuilder().append("[");
            int index = 0;
            for (String app : ualAnnotation) {
                index++;
                appStr.append(app).append(", ");
                if (index == UAL_MAX_ALLOWED_VALUE_COUNT) {
                    appStr.append("...");
                    break;
                }
            }
            return appStr.append("]").toString();
        }
        return ualAnnotation.toString();
    }
}
